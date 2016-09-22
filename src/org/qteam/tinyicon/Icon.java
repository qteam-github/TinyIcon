package org.qteam.tinyicon;

import java.awt.image.*;

/**
	The concrete icon entry. The {@code Icon} class represents a single icon instance and it is the base entry to work with.
	It summarize (and simplify) most of the internal icon structures (See {@link org.qteam.tinyicon.structs structs} package).<br>
	When a {@code .ico} file is decoded an internal {@link java.util.ArrayList ArrayList &lt;Icon&gt;} is built; after that, several methods of
	the {@link TinyIcon} class can be used to get {@code Icon} instances.<br>

	The most common usage is just to get the associated {@link BufferedImage} ({@link #getImage getImage} method), but
	some information (such as {@code width}, {@code height}, {@code bpp}, etc.) can also be queried.

	@see TinyIcon#getIcon(int) getIcon (int)
	@see TinyIcon#getIcon(Predicate) getIcon (Predicate &lt;Icon&gt;)
	@see TinyIcon#getLastIcon() getLastIcon ()
	@see TinyIcon#extractIcons(Predicate) extractIcons (Predicate &lt;Icon&gt;)
*/
public final class Icon
{
	private final int width;
	private final int height;
	private final int bpp;

	private final boolean isPng;
	private final BufferedImage image;

	/**
		Constructs a new {@code Icon} with the given parameters.
		This constructor is intended to be called only by {@link TinyIcon} class; there should no need to call it explicitly.

		@param width  The width of the icon in pixels.
		@param height The height of the icon in pixels.
		@param bpp    Bits per pixel (Valid values: 1, 4, 8, 24, 32).
		@param isPng  True if the icon was compressed, false otherwise.
		@param image  The actual image pixels.
	*/
	public Icon (int width, int height, int bpp, boolean isPng, BufferedImage image)
	{
		this.width  = width;
		this.height = height;
		this.bpp    = bpp;
		this.image  = image;
		this.isPng  = isPng;
	}

	/**
		Returns the width of the {@code Icon}.
		@return the width of this {@code Icon}.
	*/
	public int getWidth ()
	{
		return (width);
	}

	/**
		Returns the height of the {@code Icon}.
		@return the height of this {@code Icon}.
	*/
	public int getHeight ()
	{
		return (height);
	}

	/**
		Returns the area of the {@code Icon}.
		@return the area (<em>width * height</em>) of this {@code Icon}.
	*/
	protected int getArea ()
	{
		return (width * height);
	}

	/**
		Returns the bits per pixel of the {@code Icon}.
		@return the bits per pixel of this {@code Icon}.
	*/
	public int getBpp ()
	{
		return (bpp);
	}

	/**
		Returns the associated {@link BufferedImage} of the {@code Icon}.
		@return the associated {@link BufferedImage} of this {@code Icon}.
	*/
	public BufferedImage getImage ()
	{
		return (image);
	}

	/**
		Returns the compression of the {@code Icon}.
		@return {@code true} if the icon was compressed, {@code false} otherwise.
	*/
	public boolean isPng ()
	{
		return (isPng);
	}

	/**
		Returns a string summarizing the state of this {@code Icon}.
		@return A summary string.
	*/
	@Override
	public String toString ()
	{
		final StringBuilder sb = new StringBuilder ();

		sb.append ("width  = ").append (width ).append ("\n");
		sb.append ("height = ").append (height).append ("\n");
		sb.append ("bpp    = ").append (bpp   ).append ("\n");
		sb.append ("isPng  = ").append (isPng ).append ("\n");
		sb.append ("image  = ").append (image ).append ("\n");

		return (sb.toString ());
	}
}
