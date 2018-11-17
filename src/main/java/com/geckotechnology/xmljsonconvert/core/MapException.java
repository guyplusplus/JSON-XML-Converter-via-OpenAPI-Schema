package com.geckotechnology.xmljsonconvert.core;

/**
 * @author Guy D.
 * October 2018
 */
public class MapException extends Exception {
	
	private static final long serialVersionUID = -6453639024565418113L;
	private SimplePath path;
	
	public MapException(String message, SimplePath path) {
		super(message);
		this.path = path;
	}
	
	public MapException(String message) {
		super(message);
		path = null;
	}
	
	public String toString() {
		if(path == null)
			return super.getMessage();
		return super.getMessage() + " (path: " + path + ")";
	}

}
