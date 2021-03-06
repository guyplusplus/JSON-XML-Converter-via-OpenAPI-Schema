package com.geckotechnology.xmljsonconvert.core;

public class PropertyXMLNodeSpec {
	
	private String propertyName;
	private String ref;
	private XMLNodeSpec xmlNodeSpec;

	public PropertyXMLNodeSpec(String propertyName, String ref, XMLNodeSpec xmlNodeSpec) {
		this.propertyName = propertyName;
		this.ref = ref;
		this.xmlNodeSpec = xmlNodeSpec;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public XMLNodeSpec getXmlNodeSpec() {
		return xmlNodeSpec;
	}
	
	public String getRef() {
		return ref;
	}
	
	public void setPropertyXMLNodeSpec(XMLNodeSpec xmlNodeSpec) {
		this.xmlNodeSpec = xmlNodeSpec;
	}

}
