package org.maltparser.ml.lib;

import org.maltparser.core.exception.MaltChainedException;


/**
 *  LibException extends the MaltChainedException class and is thrown by classes
 *  within the lib package.
 *
 * @author Johan Hall
 * @since 1.0
**/
public class LibException extends MaltChainedException {
	public static final long serialVersionUID = 8045568022124816379L; 
	/**
	 * Creates a LibException object with a message
	 * 
	 * @param message	the message
	 */
	public LibException(String message) {
		super(message);
	}
	/**
	 * Creates a LibException object with a message and a cause to the exception.
	 * 
	 * @param message	the message
	 * @param cause		the cause to the exception
	 */
	public LibException(String message, Throwable cause) {
		super(message, cause);
	}
}
