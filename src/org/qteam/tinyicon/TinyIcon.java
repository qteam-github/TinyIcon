package org.qteam.tinyicon;

import org.qteam.tinyicon.structs.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.jar.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;

/**
	The main class to load and convert icons/favicons to {@link BufferedImage} or {@code PNG} bytes.
	{@code TinyIcon} allows to load and decode {@code .ico} files very easily.<br>

	A single icon file usually contains multiple images (e.g. with different {@code resolution} or {@code color depth}), so several methods
	are available to get a particular {@code icon} or {@code image} (See {@link #getIcon getIcon} or
	{@link #getImage(int) getImage} methods).<br>
	Additionally {@code icons} can also be sorted to get, for example, the smallest available {@code icon} (See {@link #sortIcons(IconSort, IconSort) sortIcons} method).

	<p><b><u><a name="basic_usage">Basic usage</a></u></b><b>:</b>

	<p>1) Construct a {@link #TinyIcon(String) TinyIcon}:
	<blockquote>
		// Local icon<br>
		{@code final TinyIcon ti = new TinyIcon ("c:/icons/myicon.ico");}<br>
		<i>or</i><br>
		// Favicon<br>
		{@code final TinyIcon ti = new TinyIcon ("https://www.github.com");}
	</blockquote>

	<p>2) Get a specific {@link Icon}:
	<blockquote>
		// Get the third {@link Icon} (Order depends in how the icons are stored in the {@code .ico} file)<br>
		{@code final Icon ico = ti.getIcon (2);}<br>
		<i>or</i><br>
		// Get a specific {@link Icon} based on a {@code condition}:<br>
		{@code final Icon ico = ti.getIcon (i -> ((i.getWidth () == 48) && (i.getHeight () == 48)));}
	</blockquote>

	<p>3) Work with the {@link Icon}:
	<blockquote>
		// Get the associated {@link BufferedImage}<br>
		{@code final BufferedImage img = ico.getImage ();}<br>
		<i>or</i><br>
		// Query some information<br>
		{@code System.out.printf ("img width = %d, img height = %d\n", ico.getWidth (), ico.getHeight ());}
	</blockquote>

	<p><b><u>Advanced usage</u></b><b>:</b>

	<p>1) Query library information:
	<blockquote>
		{@code final String bd = TinyIcon.getLibraryBuildDate ();}<br>
		{@code System.out.printf ("version = %s, build date = %s, library path = %s\n\n",
			TinyIcon.getLibraryVersion (), ((bd == null) ? "Unknown" : bd), TinyIcon.getLibraryPath ());}
	</blockquote>

	<p>2) Sort icons:
	<blockquote>
		{@code final TinyIcon ti = new TinyIcon ("myicon.ico");}

		<p>// Sort icons from bigger to smaller (first by resolution, then by bits per pixel)<br>
		{@code ti.sortIcons (IconSort.BY_RESOLUTION_DESCENDING, IconSort.BY_BPP_DESCENDING);}
	</blockquote>

	<p>3) Extract a subset of icons:
	<blockquote>
		{@code final TinyIcon ti = new TinyIcon ("myicon.ico");}

		<p>{@code // Get all icons that have width > 16 and bpp < 32}<br>
		{@code final ArrayList <Icon> icons = ti.extractIcons (i -> ((i.getWidth () > 16) && (i.getBpp () < 32)));}
	</blockquote>

	<p>4) Extract a subset of images:
	<blockquote>
		{@code final TinyIcon ti = new TinyIcon ("myicon.ico");}

		<p>{@code // Get all images that have width > 16 and bpp < 32}<br>
		{@code final ArrayList <BufferedImage> images = ti.extractImages (i -> ((i.getWidth () > 16) && (i.getBpp () < 32)));}
	</blockquote>

	<p>GitHub page: <a href="https://github.com/qteam-/TinyIcon">https://github.com/qteam-github/TinyIcon</a>
	@author darq
*/
public final class TinyIcon
{
	/** Current version of the TinyIcon library. */
	private final static String LIB_VERSION = "0.90";

	private ArrayList <Icon> icons;
	private final byte buf [];

	private IconDir icondir;
	private String icon_name;

	/**
		Constructs a new {@code TinyIcon} and loads / processes the specified icon file.<br>
		See the <a href="#basic_usage">basic usage description</a> for some examples.

		@param filename_or_url The local path of the {@code .ico} file or a {@code remote address} to load {@code favicons}.
		In case of {@code remote addresses} is not necessary to specify any icon name; a '{@code favicon.ico}' is automatically
		appended to the {@link URL}. Accepted protocols are: {@code https}, {@code http} and {@code ftp}.

		@throws IOException              if an I/O exception occurs during loading the {@code .ico} file (e.g. a non existent file or {@link URL}).
		@throws InvalidIconException     if the file is an invalid icon (e.g. a corrupted file or if you try to load a different file instead of a {@code .ico} one).
		@throws InvalidIconDataException if the icon has wrong data inside (e.g. invalid {@code image header} or unsupported {@code bitCount} value).
	*/
	public TinyIcon (String filename_or_url) throws IOException, InvalidIconException, InvalidIconDataException
	{
		if (IconUtils.isURL (filename_or_url))
			buf = loadIconFromUrl (filename_or_url);
		else
			buf = loadIconFromLocalFile (filename_or_url);

		// Process all icons inside 'buf'
		processIcons ();
	}

	private byte [] loadIconFromUrl (String url_string) throws IOException
	{
		icon_name = "favicon.ico";
		final URL url = new URL (url_string + "/" + icon_name);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		InputStream is = null;

		try
		{
			// Some websites give HTTP 403 error without user agent...
			final URLConnection uc = url.openConnection ();
			uc.addRequestProperty ("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");

			is = uc.getInputStream ();

			final byte tmpBuf [] = new byte [4096]; 
			int nBytes;

			while ((nBytes = is.read (tmpBuf)) > 0)
				baos.write (tmpBuf, 0, nBytes);

			return (baos.toByteArray ());
		}
		finally
		{
			IconUtils.closeInputStream (is);
		}
	}

	private byte [] loadIconFromLocalFile (String file) throws IOException
	{
		final File f = new File (file);
		icon_name = f.getName ();

		try (final FileInputStream fp = new FileInputStream (f))
		{
			final byte tmp [] = new byte [fp.available ()];
			fp.read (tmp);

			return (tmp);
		}
	}

	private void applyAndTable (BitmapInfoHeader bih, int img_buf [], int andMaskOffset)
	{
		final int stride = IconUtils.dwordPad (bih.width);

		// Pad width to the nearest byte (Round up)
		final int pwidth = IconUtils.bytePad (bih.width);

		for (int y = 0; y < bih.height; y ++)
		{
			final int buf_yofs = andMaskOffset + stride * y;
			final int img_yofs = bih.width * (bih.height - 1 - y);

			for (int x = 0; x < pwidth; x ++)
			{
				final int val = IconUtils.read_byte (buf, buf_yofs + x);

				for (int z = 0; z < 8; z ++)
				{
					if ((val & (1 << z)) == 0)
					{
						final int xofs = (x << 3) | (7 - z);

						// Apply 'full alpha' to the XOR map
						if (xofs < bih.width)
							img_buf [img_yofs + xofs] |= (0xFF << 24);
					}
				}
			}
		}
	}

	private void processIcons () throws IOException, InvalidIconException, InvalidIconDataException
	{
		// Create the 'main entry'
		icondir = new IconDir (icon_name, buf);

		// Allocate the list of icons
		icons = new ArrayList (icondir.count);

		// Parse Icon(s)
		for (int n = 0; n < icondir.count; n ++)
		{
			final IconDirEntry ide = icondir.entries [n];
			final IconImage icoimg = ide.iconimage;

			// Icon is a bitmap
			if (icoimg != null)
			{
				final BitmapInfoHeader bih = icoimg.header;
				final int pal [] = icoimg.pal;

				final int width         = bih.width;
				final int height        = bih.height;
				final int stride        = bih.stride;
				final int colorCount    = bih.colorCount;
				final int colorMapOfs   = icoimg.colorMapOffset;
				final int xorMaskOffset = icoimg.xorMaskOffset;
				final int andMaskOffset = icoimg.andMaskOffset;

				// Create an ARGB BufferedImage
				final int img_buf [] = new int [width * height];
				final BufferedImage img = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);

				switch (bih.bitCount)
				{
					// 2 colors
					case 1:
					{
						// Pad width to the nearest byte (Round up)
						final int pwidth = IconUtils.bytePad (width);

						// Xor table
						for (int y = 0; y < height; y ++)
						{
							final int buf_yofs = xorMaskOffset + stride * y;
							final int img_yofs = width * (height - 1 - y);

							for (int x = 0; x < pwidth; x ++)
							{
								final int val = IconUtils.read_byte (buf, buf_yofs + x);

								for (int k = 0; k < 8; k ++)
								{
									final int xofs = (x << 3) | k;

									if (xofs < width)
										img_buf [img_yofs + xofs] = ((val & (1 << (7 - k))) != 0) ? pal [1] : pal [0];
								}
							}
						}

						// And table
						applyAndTable (bih, img_buf, andMaskOffset);
						break;
					}

					// 16 colors
					case 4:
					{
						// Pad width to the nearest nibble (Round up)
						final int pwidth = (width + 1) >> 1;

						// Xor table
						for (int y = 0; y < height; y ++)
						{
							final int buf_yofs = xorMaskOffset + stride * y;
							final int img_yofs = width * (height - 1 - y);

							for (int x = 0; x < pwidth; x ++)
							{
								final int val = IconUtils.read_byte (buf, buf_yofs + x);

								final int xofs1 = x << 1;
								final int xofs2 = xofs1 | 1;

								// High nibble
								if (xofs1 < width)
									img_buf [img_yofs + xofs1] = pal [val >> 4];

								// Low nibble
								if (xofs2 < width)
									img_buf [img_yofs + xofs2] = pal [val & 0x0F];
							}
						}

						// And table
						applyAndTable (bih, img_buf, andMaskOffset);
						break;
					}

					// 256 colors
					case 8:
					{
						// Xor table
						for (int y = 0; y < height; y ++)
						{
							final int buf_yofs = xorMaskOffset + stride * y;
							final int img_yofs = width * (height - 1 - y);

							for (int x = 0; x < width; x ++)
							{
								final int val = IconUtils.read_byte (buf, buf_yofs + x);
								img_buf [img_yofs + x] = pal [val];
							}
						}

						// And table
						applyAndTable (bih, img_buf, andMaskOffset);
						break;
					}

					// 16777216 colors (True colors)
					case 24:
					{
						// Xor table
						for (int y = 0; y < height; y ++)
						{
							final int buf_yofs = colorMapOfs + stride * y;
							final int img_yofs = width * (height - 1 - y);

							for (int x = 0; x < width; x ++)
							{
								final int buf_ofs = buf_yofs + x * 3;

								final int b = IconUtils.read_byte (buf, buf_ofs);
								final int g = IconUtils.read_byte (buf, buf_ofs + 1);
								final int r = IconUtils.read_byte (buf, buf_ofs + 2);

								img_buf [img_yofs + x] = IconUtils.MAKE_RGB (r, g, b);
							}
						}

						// And table
						applyAndTable (bih, img_buf, andMaskOffset);
						break;
					}

					// 16777216 colors (True colors) + alpha
					case 32:
					{
						for (int y = 0; y < height; y ++)
						{
							final int buf_yofs = colorMapOfs + (width << 2) * y;
							final int img_yofs = width * (height - 1 - y);

							for (int x = 0; x < width; x ++)
							{
								final int buf_ofs = buf_yofs + (x << 2);

								final int b = IconUtils.read_byte (buf, buf_ofs);
								final int g = IconUtils.read_byte (buf, buf_ofs + 1);
								final int r = IconUtils.read_byte (buf, buf_ofs + 2);
								final int a = IconUtils.read_byte (buf, buf_ofs + 3);

								img_buf [img_yofs + x] = IconUtils.MAKE_ARGB (a, r, g, b);
							}
						}

						break;
					}

					default:
						throw new InvalidIconDataException ("%s -> Unsupported color format! (bitCount = %d, colorCount = %d)\n", icon_name, bih.bitCount, colorCount);
				}

				// Set all image pixels
				img.setRGB (0, 0, width, height, img_buf, 0, width);

				// Create and add a new icon entry
				icons.add (new Icon (width, height, bih.bitCount, false, img));
			}

			// Icon is a PNG
			else
			{
				// Decode the png via ImageIO
				final ByteArrayInputStream bais = new ByteArrayInputStream (buf, ide.imageOffset, ide.bytesInRes);
				final BufferedImage img = ImageIO.read (bais);

				// Create and add a new icon entry
				icons.add (new Icon (img.getWidth (), img.getHeight (), ide.bitCount, true, img));
			}
		}
	}

	// Get the appropriate comparator (null indicates: No sort)
	private Comparator <Icon> getComparator (IconSort sort_type) throws IllegalArgumentException
	{
		Comparator <Icon> c;

		switch (sort_type)
		{
			case NONE:
			{
				c = null;
				break;
			}

			case BY_RESOLUTION_ASCENDING:
			{
				c = Comparator.comparing (Icon :: getArea);
				break;
			}

			case BY_RESOLUTION_DESCENDING:
			{
				c = Comparator.comparing (Icon :: getArea).reversed ();
				break;
			}

			case BY_BPP_ASCENDING:
			{
				c = Comparator.comparing (Icon :: getBpp);
				break;
			}

			case BY_BPP_DESCENDING:
			{
				c = Comparator.comparing (Icon :: getBpp).reversed ();
				break;
			}

			case BY_COMPRESSION_ASCENDING:
			{
				c = Comparator.comparing (Icon :: isPng);
				break;
			}

			case BY_COMPRESSION_DESCENDING:
			{
				c = Comparator.comparing (Icon :: isPng).reversed ();
				break;
			}

			default:
				throw new IllegalArgumentException ("Unknown sort type: " + sort_type);
		}

		return (c);
	}

	/**
		Sorts all icons using the specified {@code primary} and {@code secondary} keys.<br>
		This method allows up to two sort keys;	first all icons are sorted using the {@code primary} key, then the {@code secondary} key.<br>
		The icons can be sorted by {@code image resolution}, {@code bits per pixel} and {@code image compression} and both
		{@code ascending} or {@code descending} order can be used.

		<p>Valid values for {@code sort_type} are:
		<ul>
			<li>{@link IconSort#NONE NONE}</li>
			<li>{@link IconSort#BY_RESOLUTION_ASCENDING BY_RESOLUTION_ASCENDING}</li>
			<li>{@link IconSort#BY_RESOLUTION_DESCENDING BY_RESOLUTION_DESCENDING}</li>
			<li>{@link IconSort#BY_BPP_ASCENDING BY_BPP_ASCENDING}</li>
			<li>{@link IconSort#BY_BPP_DESCENDING BY_BPP_DESCENDING}</li>
			<li>{@link IconSort#BY_COMPRESSION_ASCENDING BY_COMPRESSION_ASCENDING}</li>
			<li>{@link IconSort#BY_COMPRESSION_DESCENDING BY_COMPRESSION_DESCENDING}</li>
		</ul>

		<p>Example:<br>To sort icons first by {@code image area (ascending)}, then by {@code bit per pixel (descending)} use:
		{@code ti.sortIcons (IconSort.BY_RESOLUTION_ASCENDING, IconSort.BY_BPP_DESCENDING);}<br>
		where {@code ti} is a constructed {@code TinyIcon}.

		<p>Note that the sorting only occurs in memory (in an internal {@link ArrayList}); the original {@code .ico} file is not modified.

		@param sort_type_1 The {@code primary} key for sorting.
		@param sort_type_2 The {@code secondary} key for sorting.
	*/
	public void sortIcons (IconSort sort_type_1, IconSort sort_type_2)
	{
		// No sort: Do nothing
		if ((sort_type_1 == IconSort.NONE) && (sort_type_2 == IconSort.NONE))
			return;

		// Get the two comparators
		final Comparator <Icon> c1 = getComparator (sort_type_1);
		final Comparator <Icon> c2 = getComparator (sort_type_2);

		// Apply sort (handling special cases)
		if ((c1 != null) && (c2 == null))
			icons.sort (c1);

		else if ((c1 == null) && (c2 != null))
			icons.sort (c2);

		else if ((c1 != null) && (c2 != null))
			icons.sort (c1.thenComparing (c2));
	}

	/**
		Sorts all icons using the specified key.

		<p>This method is a shortcut for:<br>
		{@code sortIcons (sort_type, IconSort.NONE);}

		@param sort_type The {@code primary} key for sorting.
		@see #sortIcons(IconSort, IconSort) sortIcons (IconSort, IconSort)
	*/
	public void sortIcons (IconSort sort_type)
	{
		sortIcons (sort_type, IconSort.NONE);
	}

	/**
		Returns how many images the {@code icon} has.
		@return the number of images that the {@code icon} file contains.
	*/
	public int getNumOfIcons ()
	{
		return (icondir.count);
	}

	/**
		Returns the {@link Icon} at the specified position in the internal icons list.<br>
		Initial order of the icons (unless you sort them) depends in how they are stored in the {@code .ico} file (e.g. by an icon editor program).

		@param index Position of the element to return.
		@return the {@link Icon} at the specified position in the list.
		@throws IndexOutOfBoundsException if the {@code index} is out of range [({@code index} {@literal <} 0) || ({@code index} {@literal >=} getNumOfIcons ())].
	*/
	public Icon getIcon (int index)
	{
		if ((index < 0) || (index >= icondir.count))
			throw new IndexOutOfBoundsException (String.valueOf (index));

		return (icons.get (index));
	}

	/**
		Returns the first occurence of the {@link Icon} that matches the given {@code condition}.<br>
		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get the first icon with width and height > 32}<br>
		{@code final Icon ico1 = ti.getIcon (i -> ((i.getWidth () > 32) && (i.getHeight () > 32)));}

		<p>{@code // Get the first icon with bpp = 8}<br>
		{@code final Icon ico2 = ti.getIcon (i -> (i.getBpp () == 8));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return the first {@link Icon} that matches the specified {@code condition} or {@code null} if the {@code condition} is not satisfied.
	*/
	public Icon getIcon (Predicate <Icon> condition)
	{
		final Optional <Icon> icon = icons.stream ().filter (condition).findFirst ();
		return (icon.orElse (null));
	}

	/**
		Returns the last {@link Icon} in the internal icons list.

		<p>This method is a shortcut for:<br>
		{@code getIcon (getNumOfIcons () - 1);}

		@return the last {@link Icon} in the list.
		@see #getIcon(int) getIcon (int)
	*/
	public Icon getLastIcon ()
	{
		return (getIcon (icondir.count - 1));
	}

	/**
		Returns the {@link BufferedImage} at the specified position in the internal icons list.<br>
		Initial order of the images (unless you sort them) depends in how they are stored in the {@code .ico} file (e.g. by an icon editor program).

		@param index Position of the element to return.
		@return the {@link BufferedImage} at the specified position in the list.
		@throws IndexOutOfBoundsException if the {@code index} is out of range [({@code index} {@literal <} 0) || ({@code index} {@literal >=} getNumOfIcons ())].
	*/
	public BufferedImage getImage (int index)
	{
		return (getIcon (index).getImage ());
	}

	/**
		Returns the first occurence of the {@link BufferedImage} that matches the given {@code condition}.<br>
		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get the first compressed image with height >= 256 and bpp = 32}<br>
		{@code final Icon ico1 = ti.getIcon (i -> (i.isPng () && (i.getHeight () >= 256) && (i.getBpp () == 32)));}

		<p>{@code // Get the first image with width >= 128 or bpp < 8}<br>
		{@code final Icon ico2 = ti.getIcon (i -> (i.getWidth () >= 128) || (i.getBpp () < 8)));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return the first {@link BufferedImage} that matches the specified {@code condition} or
		{@code null} if the {@code condition} is not satisfied.
	*/
	public BufferedImage getImage (Predicate <Icon> condition)
	{
		final Icon icon = getIcon (condition);
		return ((icon == null) ? null : icon.getImage ());
	}

	/**
		Returns the last {@link BufferedImage} in the internal icons list.

		<p>This method is a shortcut for:<br>
		{@code getImage (getNumOfIcons () - 1);}

		@return the last {@link BufferedImage} in the list.
		@see #getImage(int) getImage (int)
	*/
	public BufferedImage getLastImage ()
	{
		return (getIcon (icondir.count - 1).getImage ());
	}

	/**
		Returns the {@code image} (as {@code png byte array}) at the specified position in the internal icons list.<br>
		This method is similar to {@link #getImage(int) getImage (int)} but the returned {@link BufferedImage} is converted
		to {@code png byte array} using the {@link ImageIO#write ImageIO.write} method.<br>
		Initial order of the images (unless you sort them) depends in how they are stored in the {@code .ico} file (e.g. by an icon editor program).

		@param index Position of the element to return.
		@return the {@code byte array} with the {@code png} data at the specified position in the list or {@code null} if an error occurs.
		@throws IndexOutOfBoundsException if the {@code index} is out of range [({@code index} {@literal <} 0) || ({@code index} {@literal >=} getNumOfIcons ())].
		@see #getImage(int) getImage (int)
	*/
	public byte [] getImageAsPng (int index)
	{
		if ((index < 0) || (index >= icondir.count))
			throw new IndexOutOfBoundsException (String.valueOf (index));

		return (IconUtils.imgToPngBytes (getImage (index)));
	}

	/**
		Returns the first occurence of the {@code image} (as {@code png byte array}) that matches the given {@code condition}.<br>
		This method is similar to {@link #getImage(Predicate) getImage (Predicate &lt;Icon&gt;)} but the returned
		{@link BufferedImage} is converted	to {@code png byte array} using the
		{@link ImageIO#write ImageIO.write} method.<br>
		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get the first image data with height = 512}<br>
		{@code final byte ico1 [] = ti.getIcon (i -> (i.getHeight () == 512));}

		<p>{@code // Get the first compressed image data with bpp >= 24}<br>
		{@code final byte ico2 [] = ti.getIcon (i -> (i.isPng () && (i.getBpp () >= 24)));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return the first {@code image} (as {@code png byte array}) that matches the specified {@code condition} or
		{@code null} if an error occurs or if the {@code condition} is not satisfied.

		@see #getImage(Predicate) getImage (Predicate &lt;Icon&gt;)
	*/
	public byte [] getImageAsPng (Predicate <Icon> condition)
	{
		final BufferedImage img = getImage (condition);
		return ((img == null) ? null : IconUtils.imgToPngBytes (img));
	}

	/**
		Extracts all icons that match the given {@code condition}.<br>
		When extracting icons, a new {@link ArrayList} is created as subset of the original list.<br>
		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get all icons that have width and height > 32}<br>
		{@code final ArrayList <Icon> list1 = ti.extractIcons (i -> ((i.getWidth () > 32) && (i.getHeight () > 32)));}

		<p>{@code // Get all icons that have bpp = 8}<br>
		{@code final ArrayList <Icon> list2 = ti.extractIcons (i -> (i.getBpp () == 8));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return a new {@link ArrayList} containing all icons that match the specified {@code condition} or an empty {@link ArrayList} if
		the {@code condition} is not satisfied.
	*/
	public ArrayList <Icon> extractIcons (Predicate <Icon> condition)
	{
		return (icons.stream ().filter (condition).collect (Collectors.toCollection (ArrayList :: new)));
	}

	/**
		Extracts all images (as {@link BufferedImage}) that match the given {@code condition}.<br>
		When extracting images, a new {@link ArrayList} is created as subset of the original list.<br>
		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get all compressed images that have height >= 256 and bpp = 32}<br>
		{@code final ArrayList <BufferedImage> list1 = ti.extractImages (i -> (i.isPng () && (i.getHeight () >= 256) && (i.getBpp () == 32)));}

		<p>{@code // Get all images that have width >= 128 or bpp < 8}<br>
		{@code final ArrayList <BufferedImage> list2 = ti.extractImages (i -> (i.getWidth () >= 128) || (i.getBpp () < 8)));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return a new {@link ArrayList} containing all images that match the specified {@code condition} or an empty
		{@link ArrayList} if the {@code condition} is not satisfied.
	*/
	public ArrayList <BufferedImage> extractImages (Predicate <Icon> condition)
	{
		final ArrayList <Icon> flt_icons = extractIcons (condition);
		final ArrayList <BufferedImage> dest = new ArrayList (flt_icons.size ());

		flt_icons.stream ().forEach
		(
			(icon) ->
			{
				dest.add (icon.getImage ());
			}
		);

		return (dest);
	}

	/**
		Extracts all images (as {@link BufferedImage}) from the internal icons list.<br>
		When extracting images, a new {@link ArrayList} is created as subset of the original list.

		<p>This method is a shortcut for:<br>
		{@code extractImages (i -> true);}

		@return a new {@link ArrayList} containing all images from the internal list.
		@see #extractImages(Predicate) extractImages (Predicate &lt;Icon&gt;)
	*/
	public ArrayList <BufferedImage> extractImages ()
	{
		return (extractImages (i -> true));
	}

	/**
		Extracts all images (as {@code png byte array}) that match the given {@code condition}.<br>
		This method is similar to {@link #extractImages(Predicate) extractImages (Predicate &lt;Icon&gt;)} but the returned
		{@link BufferedImage} is converted	to {@code png byte array} using the
		{@link ImageIO#write ImageIO.write} method, however, if an error occurs during png image conversion,
		the (missing/{@code null}) image data is not added to the final {@link ArrayList}.
		<p>When extracting images, a new {@link ArrayList} is created as subset of the original list.<br>

		To build conditions, the methods of the class {@link Icon} can be used.

		<p>Here are some examples:

		<p>{@code // Get all images that have height = 512}<br>
		{@code final ArrayList <byte []> list1 = ti.extractImagesAsPng (i -> (i.getHeight () == 512));}

		<p>{@code // Get all compressed images that have bpp >= 24}<br>
		{@code final ArrayList <byte []> list2 = ti.extractImagesAsPng (i -> (i.isPng () && (i.getBpp () >= 24)));}

		<p>where {@code ti} is a constructed {@code TinyIcon}.

		@param condition a {@link Predicate} expression to be evaluated.
		@return a new {@link ArrayList} containing all images (as {@code png byte array}) that match the specified
		{@code condition} or an empty {@link ArrayList} if the {@code condition} is not satisfied.<br>
	*/
	public ArrayList <byte []> extractImagesAsPng (Predicate <Icon> condition)
	{
		final ArrayList <Icon> flt_icons = extractIcons (condition);
		final ArrayList <byte []> dest = new ArrayList (flt_icons.size ());

		flt_icons.stream ().forEach
		(
			(icon) ->
			{
				final byte tmp [] = IconUtils.imgToPngBytes (icon.getImage ());

				if (tmp != null)
					dest.add (tmp);
			}
		);

		return (dest);
	}

	/**
		Extracts all images (as {@code png byte array}) from the internal icons list.<br>
		When extracting images, a new {@link ArrayList} is created as subset of the original list.

		<p>This method is a shortcut for:<br>
		{@code extractImagesAsPng (i -> true);}

		@return a new {@link ArrayList} containing all images (as {@code png byte array}) from the internal list.
		@see #extractImagesAsPng(Predicate) extractImagesAsPng (Predicate &lt;Icon&gt;)
	*/
	public ArrayList <byte []> extractImagesAsPng ()
	{
		return (extractImagesAsPng (i -> true));
	}

	/**
		Returns the name of the {@code icon} file.
		@return the name of this {@code icon}.
	*/
	public String getIconName ()
	{
		return (icon_name);
	}

	/**
		Returns the version of the {@code TinyIcon} library.
		@return the current library version.
	*/
	public static String getLibraryVersion ()
	{
		return (LIB_VERSION);
	}

	/**
		Returns the path of the {@code TinyIcon} library.
		@return the library jar path or {@code null} if an error occurs.
	*/
	public static String getLibraryPath ()
	{
		try
		{
			return (URLDecoder.decode (TinyIcon.class.getProtectionDomain ().getCodeSource ().getLocation ().getPath (), "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			// This should not happen
			Logger.getLogger (TinyIcon.class.getName()).log (Level.SEVERE, null, e);
			return (null);
		}
	}

	/**
		Returns the build date of the {@code TinyIcon} library.
		@return the current build date or {@code null} if an error occurs.
	*/
	public static String getLibraryBuildDate ()
	{
		JarFile jar = null;
		long last_mod;

		try
		{
			final String path = getLibraryPath ();
			final File f = new File (path);

			// The app was launched 'outside' IDE; get last_mod from the manifest attribute
			if (path.endsWith (".jar"))
			{
				jar = new JarFile (f);

				final Manifest manifest = jar.getManifest ();
				final Attributes attributes = manifest.getMainAttributes ();

				last_mod = Long.parseLong (attributes.getValue ("Build-Millis"));
			}

			// We are inside an IDE; just read 'lastModified' of the build directory
			else
				last_mod = f.lastModified ();

			// Get the final build date
			final DateFormat df = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
			return (df.format (new Date (last_mod)));
		}
		catch (IOException | NumberFormatException e)
		{
			Logger.getLogger (TinyIcon.class.getName ()).log (Level.WARNING, null, e);
			return (null);
		}
		finally
		{
			IconUtils.closeJarFile (jar);
		}
	}

	/**
		Returns a string summarizing the state of the icon(s).<br>
		@return a summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append ("\n");
		sb.append ("IconDir:\n");
		sb.append ("-------\n");
		sb.append (icondir);

		return (sb.toString ());
	}
}
