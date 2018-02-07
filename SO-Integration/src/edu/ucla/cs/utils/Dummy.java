package edu.ucla.cs.utils;

import java.net.URLEncoder;

public class Dummy {
	  /**
	   * Encodes the passed String as UTF-8 using an algorithm that's compatible
	   * with JavaScript's <code>encodeURIComponent</code> function. Returns
	   * <code>null</code> if the String is <code>null</code>.
	   * 
	   * @param s The String to be encoded
	   * @return the encoded String
	   */
	  public static String encodeURIComponent(String s)
	  {
	    String result = null;

	    try
	    {
	      result = URLEncoder.encode(s, "UTF-8")
	                         .replaceAll("\\+", "%20")
	                         .replaceAll("\\%21", "!")
	                         .replaceAll("\\%27", "'")
	                         .replaceAll("\\%28", "(")
	                         .replaceAll("\\%29", ")")
	                         .replaceAll("\\%7E", "~");
	    }

	    // This exception should never occur.
	    catch (Throwable e)
	    {
	      result = s;
	    }

	    return result;
	  }
}
