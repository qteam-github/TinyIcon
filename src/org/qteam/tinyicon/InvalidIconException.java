package org.qteam.tinyicon;

/**
	Thrown to indicate that the icon file is an invalid icon (e.g. a corrupted file or if you try to load a different file instead of a {@code .ico} one).
*/
public final class InvalidIconException extends Exception
{
	/**
		Constructs an {@code InvalidIconException} with the specified detail message.
		@param message The detail message (which is saved for later retrieval by the {@link #getMessage getMessage} method).
	*/
	public InvalidIconException (String message)
	{
		super (message);
	}

	/**
		Constructs an {@code InvalidIconException} with the specified detail message using the given format string and arguments.

		@param format
		A printf style {@link java.util.Formatter format string}.

		@param args
		Arguments referenced by the format specifiers in the format	string.
		If there are more arguments than format specifiers, the	extra arguments are ignored.
		The number of arguments is variable and may be zero.
		The maximum number of arguments is limited by the maximum dimension of a Java array as defined by
		<cite>The Java&trade; Virtual Machine Specification</cite>.
		The behaviour on a {@code null} argument depends on the conversion.

		@throws java.util.IllegalFormatException
		If a format string contains an illegal syntax, a format	specifier that is incompatible with the given arguments,
		insufficient arguments given the format string, or other illegal conditions.
		For specification of all possible formatting errors, see the {@link java.util.Formatter Details} section of the formatter class specification.
	*/
	public InvalidIconException (String format, Object... args)
	{
		super (String.format (format, args));
	}
}
