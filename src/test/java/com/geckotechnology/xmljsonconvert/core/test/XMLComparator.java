package com.geckotechnology.xmljsonconvert.core.test;

import org.w3c.dom.Document;

import com.geckotechnology.xmljsonconvert.core.JSONSchemaForXML;
import com.geckotechnology.xmljsonconvert.core.MapException;

public class XMLComparator {

	public static boolean areObjectsEqual(Document d, String xml) throws MapException {
		String s = JSONSchemaForXML.xmlDocumentToString(d, false);
		return s.equals(xml);
	}
	
	public static void printAscii(String s) {
		int l = s.length();
		for(int i = 0; i<l; System.out.print(" " + (int)s.charAt(i++)));
		System.out.println();
	}

}
