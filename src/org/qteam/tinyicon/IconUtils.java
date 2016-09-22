package org.qteam.tinyicon;

import java.util.logging.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.jar.*;
import java.io.*;

/** Utility methods used by {@code TinyIcon} class. */
public final class IconUtils
{
	// Constructor
	private IconUtils ()
	{
	}

	/**
		Reads a byte from the passed buffer and at the given offset.

		@param buf The buffer that contains the icon data.
		@param ofs The offset in the buffer where start to read.
		@return an unsigned byte.
	*/
	public static int read_byte (byte buf [], int ofs)
	{
		return (buf [ofs] & 0xFF);
	}

	/**
		Reads two bytes from the passed buffer and at the given offset.

		@param buf The buffer that contains the icon data.
		@param ofs The offset in the buffer where start to read.
		@return a little-endian unsigned word (2 bytes).
	*/
	public static int read_word_le (byte buf [], int ofs)
	{
		return (((buf [ofs + 1] & 0xFF) << 8) | (buf [ofs] & 0xFF));
	}

	/**
		Reads four bytes from the passed buffer and at the given offset.

		@param buf The buffer that contains the icon data.
		@param ofs The offset in the buffer where start to read.
		@return a little-endian unsigned dword (4 bytes).
	*/
	public static int read_dword_le (byte buf [], int ofs)
	{
		return (((buf [ofs + 3] & 0xFF) << 24) | ((buf [ofs + 2] & 0xFF) << 16) | ((buf [ofs + 1] & 0xFF) << 8) | (buf [ofs] & 0xFF));
	}

	/**
		Pads the given value to the nearest dword (Round up).

		@param val the value to be padded.
		@return the padded value. 
	*/
	public static int dwordPad (int val)
	{
		// Taken from Microsoft's BITMAPINFOHEADER documentation (Scanlines need to be dword aligned)
		return (((val + 31) & ~0x1F) >> 3);
	}

	/**
		Pads the given value to the nearest byte (Round up).

		@param val the value to be padded.
		@return the padded value. 
	*/
	public static int bytePad (int val)
	{
		return ((val + 7) >> 3);
	}

	/**
		Converts the given {@link BufferedImage} to a {@code byte array} containing {@code png} data.

		@param img the {@link BufferedImage} to be converted.
		@return the {@code png} data or {@code null} if an error occurs.
	*/
	public static byte [] imgToPngBytes (BufferedImage img)
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

		try
		{
			ImageIO.write (img, "png", baos);
		}
		catch (IOException e)
		{
			Logger.getLogger (IconUtils.class.getName ()).log (Level.SEVERE, null, e);
			return (null);
		}

		return (baos.toByteArray ());
	}

	/**
		Closes the passed {@link InputStream}.

		@param is the {@link InputStream} to be closed.
	*/
	public static void closeInputStream (InputStream is)
	{
		try
		{
			if (is != null)
				is.close ();
		}
		catch (Exception e)
		{
		}
	}

	/**
		Closes the passed {@link JarFile}.

		@param jf the {@link JarFile} to be closed.
	*/
	public static void closeJarFile (JarFile jf)
	{
		try
		{
			if (jf != null)
				jf.close ();
		}
		catch (Exception e)
		{
		}
	}

	/**
		Merges the {@code ARGB} components into a single integer.

		@param a Alpha value.
		@param r Red value.
		@param g Green value.
		@param b Blue value.
		@return the final color.
	*/
	public static int MAKE_ARGB (int a, int r, int g, int b)
	{
		return ((a << 24) | (r << 16) | (g << 8) | b);
	}

	/**
		Merges the {@code RGB} components into a single integer (Alpha component is 0).

		@param r Red value.
		@param g Green value.
		@param b Blue value.
		@return the final color.
	*/
	public static int MAKE_RGB (int r, int g, int b)
	{
		return ((r << 16) | (g << 8) | b);
	}

	/**
		Tries to detect if the given argument is a local file or an {@link java.net.URL}.<br>
		Note that, in case of URLs, this method only does a basic protocol check; it doesn't test if the URL is valid.<br>
		Accepted protocols are: {@code https}, {@code http} and {@code ftp}.

		@param arg the argument to be tested.
		@return true if the specified argument is an {@link java.net.URL}, false otherwise.
	*/
	public static boolean isURL (String arg)
	{
		arg = arg.toLowerCase ();
		return (arg.startsWith ("https://") || arg.startsWith ("http://") || arg.startsWith ("ftp://"));
	}
}
