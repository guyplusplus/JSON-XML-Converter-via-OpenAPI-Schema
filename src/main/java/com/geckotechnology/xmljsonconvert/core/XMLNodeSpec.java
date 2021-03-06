package com.geckotechnology.xmljsonconvert.core;

import org.json.JSONObject;

public abstract class XMLNodeSpec {
	
	private static final int TYPE_UNDEFINED = 0;
	static final int TYPE_OBJECT = 1;
	static final int TYPE_ARRAY = 2;
	static final int TYPE_STRING = 3;
	static final int TYPE_NUMBER = 4;
	static final int TYPE_INTEGER = 5;
	static final int TYPE_BOOLEAN = 6;
	
	protected int nodeType = TYPE_UNDEFINED;
	protected boolean isNullable = false;
	protected String xmlName = null;
	protected String xmlPrefix = null;
	protected String xmlNamespace = null;
	protected boolean isXMLAttribute = false;
	protected boolean isXMLWrapped = false;
	
	protected static int typeStringToTYPE(String type) throws JSONSchemaLoadException {
		if(type == null)
			throw new JSONSchemaLoadException("JSON schema type is null / undefined");
		if(type.equals("object"))
			return TYPE_OBJECT;
		if(type.equals("array"))
			return TYPE_ARRAY;
		if(type.equals("string"))
			return TYPE_STRING;
		if(type.equals("number"))
			return TYPE_NUMBER;
		if(type.equals("integer"))
			return TYPE_INTEGER;
		if(type.equals("boolean"))
			return TYPE_BOOLEAN;
		throw new JSONSchemaLoadException("Unknow JSON schema type '" + type + "'");
	}
	
	public boolean isPrimitiveType() {
		return false;
	}
	
	public boolean isNullable() {
		return isNullable;
	}
	
	public XMLNodeSpec(int nodeType) {
		this.nodeType = nodeType;
	}
	
	public int getNodeType() {
		return nodeType;
	}
	
	public String calculateTargetFullXMLName(String objectName) {
		String name = (xmlName == null ? objectName : xmlName);
		String fullXMLName = (xmlPrefix == null ? name : xmlPrefix + ":" + name);
		return fullXMLName;
	}
	
	public String getXmlName() {
		return xmlName;
	}
	
	public String getXmlPrefix() {
		return xmlPrefix;
	}
	
	public String getXmlNamespace() {
		return xmlNamespace;
	}

	private void loadXML(JSONObject xml) {
		if(xml == null)
			return;
		xmlName = xml.optString("name", null);
		xmlPrefix = xml.optString("prefix", null);
		xmlNamespace = xml.optString("namespace", null);
		isXMLAttribute = xml.optBoolean("attribute", false);
		isXMLWrapped = xml.optBoolean("wrapped", false);
	}
	
	public void loadJSONValue(JSONObject schema, String valueDesriptionForException) throws JSONSchemaLoadException {
		isNullable = schema.optBoolean("nullable");
		loadXML(schema.optJSONObject("xml"));
	}

}
