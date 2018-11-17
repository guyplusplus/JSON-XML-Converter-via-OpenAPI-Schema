package com.geckotechnology.xmljsonconvert.core;

import java.util.Iterator;
import java.util.Stack;

public class SimplePath {
	
	public static final int JSON_PATH = 1;
	public static final int XML_PATH = 2;
	
	
	private static final String ROOT_XML_PATH = "/";
	private static final String ROOT_JSON_PATH = "$";
	
	private Stack<Object> path = new Stack<Object>();
	private int pathType;
	
	public SimplePath(int pathType) {
		if(pathType != JSON_PATH && pathType != XML_PATH)
			throw new RuntimeException("Invalid pathType");
		this.pathType = pathType;
	}
	
	public SimplePath pushElement(String elementName) {
		if(elementName == null)
			throw new NullPointerException("Element is null");
		path.push(elementName);
		return this;
	}

	public SimplePath pushXMLAttribute(String attributeName) {
		//should throw exception in case of JSON_PATH
		if(attributeName == null)
			throw new NullPointerException("Attribute is null");
		path.push("@" + attributeName);
		return this;
	}

	public SimplePath pushIndex(int index) {
		if(index < 0)
			throw new IndexOutOfBoundsException("Index can not be negative");
		path.push(index);
		return this;
	}
	
	/**
	 * May throw EmptyStackException
	 */
	public SimplePath pop() {
		path.pop();
		return this;
	}
	
	/**
	 * 
	 * @return String such as /root/children[3]/@id
	 */
	private String getFullXMLPath() {
		if(path.isEmpty())
			return ROOT_XML_PATH;
		StringBuilder sb = new StringBuilder();
		Iterator<Object> pathIterator = path.iterator();
		while(pathIterator.hasNext()) {
			Object o = pathIterator.next();
			if(o instanceof String)
				sb.append('/').append(o);
			else if(o instanceof Integer)
				sb.append('[').append(o).append(']');
			//else error
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return String such as $.children[3].firstname
	 */
	private String getFullJSONPath() {
		if(path.isEmpty())
			return ROOT_JSON_PATH;
		StringBuilder sb = new StringBuilder("$");
		Iterator<Object> pathIterator = path.iterator();
		while(pathIterator.hasNext()) {
			Object o = pathIterator.next();
			if(o instanceof String)
				sb.append('.').append(o);
			else if(o instanceof Integer)
				sb.append('[').append(o).append(']');
			//else error
		}
		return sb.toString();
	}
	
	public String toString() {
		if(pathType == JSON_PATH)
			return getFullJSONPath();
		return getFullXMLPath();
	}
	
	public SimplePath clone() {
		SimplePath sp = new SimplePath(this.pathType);
		sp.path.addAll(path);
		return sp;
	}
}
