package org.qteam.tinyicon.structs;

import org.qteam.tinyicon.*;

/** The equivalent class of Microsoft's {@code BITMAPINFOHEADER} structure. */
public final class BitmapInfoHeader
{
	/** Ofs 0 (UINT32) - Size of the {@code BitmapInfoHeader} structure (Should be 40). */
	public int size;

	/** Ofs 4 (INT32) - Icon width in pixels. */
	public int width;

	/** Ofs 8 (INT32) - Icon height int pixels (Actually it's the sum of heights of {@code 'XOR'} and {@code 'AND'} bitmaps; real height is halved). */
	public int height;

	/** Ofs 12 (UINT16) - Color planes (Unused, should be 1). */
	public int planes;

	/** Ofs 14 (UINT16) - Bits per pixel (Valid values: 1, 4, 8, 24, 32). */
	public int bitCount;

	/** Ofs 16 (UINT32) - Compression type (Unused, should be 0 = {@code BI_RGB}). */
	public int compression;

	/** Ofs 20 (UINT32) - Image size in bytes. */
	public int imageSize;

	/** Ofs 24 (INT32) - The horizontal resolution, in pixels-per-meter, of the target device for the bitmap (Unused, should be 0). */
	public int xPelsPerMeter;

	/** Ofs 28 (INT32) - The vertical resolution, in pixels-per-meter, of the target device for the bitmap (Unused, should be 0). */
	public int yPelsPerMeter;

	/** Ofs 32 (UINT32) - The number of color indexes in the color table that are actually used by the bitmap (Unused, should be 0). */
	public int clrUsed;

	/** Ofs 36 (UINT32) - The number of color indexes that are required for displaying the bitmap (Unused, should be 0). */
	public int clrImportant;

	/**
		Number of colors in the image.<br>
		Note that this field is set to true colors (16777216 colors) if {@code bitCount} is either 24 or 32 bits.<br><br>
		-- This is just a convenience field, it isn't a member of the {@code BitmapInfoHeader} struct.
	*/
	public final int colorCount;

	/**
		The sum of image width and padding (if present).<br><br>
		-- This is just a convenience field, it isn't a member of the {@code BitmapInfoHeader} struct.
	*/
	public final int stride;

	/**
		Constructs a new {@code BitmapInfoHeader}.

		@param icon_name The name of the icon.
		@param buf       The buffer that contains the icon data.
		@param image_ofs The offset in the buffer where the image data starts.

		@throws InvalidIconDataException if the icon has wrong {@code bitCount} value (e.g. {@code bitCount} = 0).
	*/
	protected BitmapInfoHeader (String icon_name, byte buf [], int image_ofs) throws InvalidIconDataException
	{
		size          = IconUtils.read_dword_le (buf, image_ofs);
		width         = IconUtils.read_dword_le (buf, image_ofs + 4);
		height        = IconUtils.read_dword_le (buf, image_ofs + 8) >> 1;
		planes        = IconUtils.read_word_le  (buf, image_ofs + 12);
		bitCount      = IconUtils.read_word_le  (buf, image_ofs + 14);
		compression   = IconUtils.read_dword_le (buf, image_ofs + 16);
		imageSize     = IconUtils.read_dword_le (buf, image_ofs + 20);
		xPelsPerMeter = IconUtils.read_dword_le (buf, image_ofs + 24);
		yPelsPerMeter = IconUtils.read_dword_le (buf, image_ofs + 28);
		clrUsed       = IconUtils.read_dword_le (buf, image_ofs + 32);
		clrImportant  = IconUtils.read_dword_le (buf, image_ofs + 36);

		// Try to get a real colorCount value
		colorCount = getColorCountValue (icon_name);

		// Calculate image stride
		stride = getStride (icon_name);
	}

	private int getColorCountValue (String icon_name) throws InvalidIconDataException
	{
		// Note: 32 bit is handled as true color
		final int bcnt = (bitCount == 32) ? 24 : bitCount;

		if (bcnt == 0)
			throw new InvalidIconDataException ("%s -> Incorrect data found in the image header! (bitCount = 0)", icon_name);			

		return (1 << bcnt);
	}

	// Pad width to nearest dword (round up)
	private int getStride (String icon_name) throws InvalidIconDataException
	{
		switch (bitCount)
		{
			// 2 colors
			case 1:
				return (IconUtils.dwordPad (width));

			// 16 colors
			case 4:
				return (IconUtils.dwordPad (width << 2));

			// 256 colors
			case 8:
				return (IconUtils.dwordPad (width << 3));

			// 16777216 colors (True colors)
			case 24:
				return (IconUtils.dwordPad (width * 24));

			// 16777216 colors (True colors) + alpha (Already aligned, no need to pad)
			case 32:
				return (0);

			default:
				throw new InvalidIconDataException ("%s -> Unsupported bitCount value! (bitCount = %d)\n", icon_name, bitCount);
		}
	}

	/**
		Returns a string summarizing the state of this {@code BitmapInfoHeader}.
		@return a summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append ("size          = ").append (size         ).append ("\n");
		sb.append ("width         = ").append (width        ).append ("\n");
		sb.append ("height        = ").append (height       ).append ("\n");
		sb.append ("planes        = ").append (planes       ).append ("\n");
		sb.append ("bitCount      = ").append (bitCount     ).append ("\n");
		sb.append ("colorCount    = ").append (colorCount   ).append ("\n");
		sb.append ("compression   = ").append (compression  ).append ("\n");
		sb.append ("imageSize     = ").append (imageSize    ).append ("\n");
		sb.append ("xPelsPerMeter = ").append (xPelsPerMeter).append ("\n");
		sb.append ("yPelsPerMeter = ").append (yPelsPerMeter).append ("\n");
		sb.append ("clrUsed       = ").append (clrUsed      ).append ("\n");
		sb.append ("clrImportant  = ").append (clrImportant ).append ("\n");

		return (sb.toString ());
	}
}
