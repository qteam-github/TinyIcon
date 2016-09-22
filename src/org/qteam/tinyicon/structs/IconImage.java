package org.qteam.tinyicon.structs;

import org.qteam.tinyicon.*;

/** The equivalent class of Microsoft's {@code ICONIMAGE} structure. */
public final class IconImage
{
	/** Image header. */
	public final BitmapInfoHeader header;

	/** Offset of the color palette. */
	public final int colorMapOffset;

	/** Offset of the {@code XOR} bitmap. */
	public final int xorMaskOffset;

	/** Offset of the {@code AND} bitmap or 0 if the {@code AND} bitmap is not necessary (e.g. bpp = 32). */
	public final int andMaskOffset;

	/** The actual color palette in {@code RGB} format or {@code null} if palette is not necessary (e.g. bpp = 24 or bpp = 32). */
	public final int pal [];

	/**
		Constructs a new {@code IconImage}.

		@param icon_name The name of the icon.
		@param buf       The buffer that contains the icon data.
		@param image_ofs The offset in the buffer where the image data starts.

		@throws InvalidIconDataException if the icon has wrong data inside (e.g. invalid {@code bitCount} value).
	*/
	public IconImage (String icon_name, byte buf [], int image_ofs) throws InvalidIconDataException
	{
		// Create a new BitmapInfoHeader
		header = new BitmapInfoHeader (icon_name, buf, image_ofs);

		final boolean true_col = (header.bitCount == 24) || (header.bitCount == 32);

		// Get offsets
		colorMapOffset = image_ofs + 40;
		xorMaskOffset  = true_col ? 0 : (colorMapOffset + (header.colorCount << 2));
		andMaskOffset  = getAndMapOffset (icon_name);

		// Create the palette (if necessary)
		pal = true_col ? null : makePalette (buf);
	}

	private int getAndMapOffset (String icon_name) throws InvalidIconDataException
	{
		switch (header.bitCount)
		{
			// 2 colors, 16 colors and 256 colors
			case 1:
			case 4:
			case 8:
				return (xorMaskOffset + header.stride * header.height);

			// 16777216 colors (True colors)
			case 24:
				return (colorMapOffset + header.stride * header.height);

			// 16777216 colors (True colors) + alpha (No need for AND mask)
			case 32:
				return (0);

			default:
				throw new InvalidIconDataException ("%s -> Unsupported bitCount value! (bitCount = %d)\n", icon_name, header.bitCount);
		}
	}

	private int [] makePalette (byte buf [])
	{
		final int colorCount = header.colorCount;
		final int palette [] = new int [colorCount];

		for (int x = 0; x < colorCount; x ++)
		{
			final int colorOfs = colorMapOffset + (x << 2);

			final int b = IconUtils.read_byte (buf, colorOfs);
			final int g = IconUtils.read_byte (buf, colorOfs + 1);
			final int r = IconUtils.read_byte (buf, colorOfs + 2);

			palette [x] = IconUtils.MAKE_RGB (r, g, b);
		}

		return (palette);
	}

	/**
		Returns a string summarizing the state of this {@code IconImage}.<br>
		Note that the {@link BitmapInfoHeader}'s {@code toString} method is called to print {@code the header info}.

		@return a summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append (header);
		sb.append ("\n");

		sb.append ("colorMapOffset = ").append (colorMapOffset).append ("\n");
		sb.append ("xorMaskOffset  = ").append (xorMaskOffset ).append ("\n");
		sb.append ("andMaskOffset  = ").append (andMaskOffset ).append ("\n");

		return (sb.toString ());
	}
}
