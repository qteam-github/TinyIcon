package org.qteam.tinyicon.structs;

import org.qteam.tinyicon.*;

/** The equivalent class of Microsoft's {@code ICONDIR} structure. */
public final class IconDir
{
	// Constants
	private final static int ICONDIRENTRY_START_OFS = 6;
	private final static int ICONDIRENTRY_SIZE      = 16;

	/*
		Minimum icon contains: num_icons = 1, width = 1, height = 1, bpp = 1

		78 = size of:
		------------
		-> IconHeader        (= 6 bytes)  +
		-> IconDirEntry      (= 16 bytes) +
		-> BitmapInfoHeader  (= 40 bytes) +
		-> 2 B/W pal entries (= 8 bytes)  +
		-> Xor map           (= 4 bytes)  +
		-> And map           (= 4 bytes)
	*/
	private final static int MIN_ICON_FILE_SIZE = 78;

	/** Ofs 0 (UINT16) - Reserved (Must be 0). */
	public final int reserved;

	/** Ofs 2 (UINT16) - Resource Type (1 for icons, 2 for cursors). */
	public final int type;

	/** Ofs 4 (UINT16) - How many images? */
	public final int count;

	/** Ofs 6 - An entry for each image (16 * {@code count} bytes). 16 bytes is the size of {@link IconDirEntry}. */
	public final IconDirEntry entries [];

	/**
		Constructs a new {@code IconDir}.

		@param icon_name The name of the icon.
		@param buf       The buffer that contains the icon data.

		@throws InvalidIconException     if the file is an invalid icon (e.g. a corrupted file or if you try to load a different file instead of a {@code .ico} one).
		@throws InvalidIconDataException if the icon has wrong data inside (e.g. invalid {@code image header} or unsupported {@code bitCount} value).
	*/
	public IconDir (String icon_name, byte buf []) throws InvalidIconException, InvalidIconDataException
	{
		reserved = IconUtils.read_word_le (buf, 0);
		type     = IconUtils.read_word_le (buf, 2);
		count    = IconUtils.read_word_le (buf, 4);

		// Check if it is a valid icon
		if ((buf.length < MIN_ICON_FILE_SIZE) || (reserved != 0x00) || (type != 0x01) || (count <= 0))
			throw new InvalidIconException ("%s -> Invalid icon file", icon_name);

		// Allocate 'count' IconDirEntries
		entries = new IconDirEntry [count];

		// Create all IconDirEntries (The IconDirEntry's constructor reads all image data)
		for (int x = 0; x < count; x ++)
			entries [x] = new IconDirEntry (icon_name, buf, ICONDIRENTRY_START_OFS + (x * ICONDIRENTRY_SIZE));
	}

	/**
		Returns a string summarizing the state of this {@code IconDir}.<br>
		Note that the {@link IconDirEntry}'s {@code toString} method is called for each icon entry.

		@return a summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append ("reserved = ").append (reserved).append ("\n");
		sb.append ("type     = ").append (type    ).append ("\n");
		sb.append ("count    = ").append (count   ).append ("\n");

		sb.append ("\n");
		sb.append ("IconDirEntries:\n--------------\n");

		int icon_no = 0; 

		for (IconDirEntry ide : entries)
		{
			if (icon_no > 0)
				sb.append ("\n");

			sb.append ("Icon ").append (icon_no).append (":\n");
			sb.append (ide);

			icon_no ++;
		}

		return (sb.toString ());
	}
}
