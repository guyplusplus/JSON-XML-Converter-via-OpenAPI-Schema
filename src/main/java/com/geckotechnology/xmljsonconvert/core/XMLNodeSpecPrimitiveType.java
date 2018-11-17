package com.geckotechnology.xmljsonconvert.core;

public class XMLNodeSpecPrimitiveType extends XMLNodeSpec {
	
	public XMLNodeSpecPrimitiveType(int nodeType) {
		super(nodeType);
	}

	public boolean isXMLAttribute() {
		return isXMLAttribute;
	}
	@Override
	public boolean isPrimitiveType() {
		return true;
	}
	
}
