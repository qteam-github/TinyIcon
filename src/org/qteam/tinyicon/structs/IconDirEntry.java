package org.qteam.tinyicon.structs;

import org.qteam.tinyicon.*;

/** The equivalent class of Microsoft's {@code ICONDIRENTRY} structure. */
public final class IconDirEntry
{
	/** Ofs 0 (UINT8) - Width, in pixels, of the image (0 if {@literal >=} 256 pixels). */
	public final int width;

	/** Ofs 1 (UINT8) - Height, in pixels, of the image (0 if {@literal >=} 256 pixels). */
	public final int height;

	/** Ofs 2 (UINT8) - Number of colors in image (0 if {@literal >=} 8 bpp). */
	public final int colorCount;

	/** Ofs 3 (UINT8) - Reserved (Must be 0). */
	public final int reserved;

	/** Ofs 4 (UINT16) - Color Planes (Unused, should be 1). */
	public final int planes;

	/** Ofs 6 (UINT16) - Bits per pixel. */
	public final int bitCount;

	/** Ofs 8 (UINT32) - How many bytes in this resource? */
	public final int bytesInRes;

	/** Ofs 12 (UINT32) - Where in the file is this image? */
	public final int imageOffset;

	/** It's {@code null} if the image is a PNG, otherwise it contains the {@code bitmap image info}. */
	public final IconImage iconimage;

	/**
		Constructs a new {@code IconDirEntry}.

		@param icon_name The name of the icon.
		@param buf       The buffer that contains the icon data.
		@param ofs       The offset in the buffer where this {@code IconDirEntry} starts.

		@throws InvalidIconDataException if the icon has wrong data inside (e.g. invalid {@code image header}).
	*/
	protected IconDirEntry (String icon_name, byte buf [], int ofs) throws InvalidIconDataException
	{
		width       = IconUtils.read_byte     (buf, ofs);
		height      = IconUtils.read_byte     (buf, ofs + 1);
		colorCount  = IconUtils.read_byte     (buf, ofs + 2);
		reserved    = IconUtils.read_byte     (buf, ofs + 3);
		planes      = IconUtils.read_word_le  (buf, ofs + 4);
		bitCount    = IconUtils.read_word_le  (buf, ofs + 6);
		bytesInRes  = IconUtils.read_dword_le (buf, ofs + 8);
		imageOffset = IconUtils.read_dword_le (buf, ofs + 12);

		// Try to detect the image type
		final boolean isPng = imageIsPng (icon_name, buf, imageOffset);

		// Allocate iconimage (if necessary)
		iconimage = isPng ? null : new IconImage (icon_name, buf, imageOffset);
	}

	// Try to detect if the current icon entry contains a compressed image
	private boolean imageIsPng (String icon_name, byte buf [], int img_ofs) throws InvalidIconDataException
	{
		final int header1 = IconUtils.read_dword_le (buf, img_ofs);

		// Check the header type
		switch (header1)
		{
			// BITMAPINFOHEADER (0x28 = 40 dec = BITMAPINFOHEADER size)
			case 0x00000028:
				return (false);

			// PNG header (First part of signature)
			case 0x474E5089:
			{
				// Second part of signature
				final int header2 = IconUtils.read_dword_le (buf, img_ofs + 4);

				// PNG signature detected!
				if (header2 == 0x0A1A0A0D)
					return (true);
				else
					throw new InvalidIconDataException ("%s -> Invalid PNG signature!", icon_name);
			}

			default:
				throw new InvalidIconDataException ("%s -> Invalid header format! Header must be 'PNG' or 'BITMAPINFOHEADER'.", icon_name);
		}
	}

	/**
		Returns a string summarizing the state of this {@code IconDirEntry}.<br>
		Note that the {@link IconImage}'s {@code toString} method is called to print the {@code image info}.

		@return a summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append ("width       = ").append (width      ).append ("\n");
		sb.append ("height      = ").append (height     ).append ("\n");
		sb.append ("colorCount  = ").append (colorCount ).append ("\n");
		sb.append ("reserved    = ").append (reserved   ).append ("\n");
		sb.append ("planes      = ").append (planes     ).append ("\n");
		sb.append ("bitCount    = ").append (bitCount   ).append ("\n");
		sb.append ("bytesInRes  = ").append (bytesInRes ).append ("\n");
		sb.append ("imageOffset = ").append (imageOffset).append ("\n");

		sb.append ("\n- Image header -\n");
		sb.append (iconimage);

		if (iconimage == null)
			sb.append (" (Image is a PNG)\n");

		return (sb.toString ());
	}
}
