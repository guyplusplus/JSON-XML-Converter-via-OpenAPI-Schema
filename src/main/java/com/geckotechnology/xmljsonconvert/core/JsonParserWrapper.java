package com.geckotechnology.xmljsonconvert.core;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * This class is a wrapper for JsonParser. It simply helps catching exceptions
 * related to this package and map it to a MapException with JSON path
 * information
 * @author Guy D.
 *
 */
public class JsonParserWrapper {
	
	private JsonParser parser;
	
	public JsonParserWrapper(JsonParser parser) {
		this.parser = parser;
	}
	
	public boolean hasNext(SimplePath jpath) throws MapException {
		try {
			return parser.hasNext();
		} catch (Exception e) {
			throw new MapException("Failed to parse JSON input", jpath);
		}
	}

	public Event next(SimplePath jpath) throws MapException {
		try {
			return parser.next();
		} catch (Exception e) {
			throw new MapException("Failed to parse JSON input", jpath);
		}
	}
	
	public String getString(SimplePath jpath) throws MapException {
		try {
			return parser.getString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MapException("Failed to parse JSON input", jpath);
		}
	}
	
	public String getPrimitiveValueAsString(SimplePath jpath) throws MapException {
		try {
			JsonValue jsonValue = parser.getValue();
			if(jsonValue.getValueType() == ValueType.STRING || jsonValue.getValueType() == ValueType.NUMBER)
				return parser.getString();
			if(jsonValue.getValueType() == ValueType.FALSE)
				return "false";
			if(jsonValue.getValueType() == ValueType.TRUE)
				return "true";
			if(jsonValue.getValueType() == ValueType.NULL)
				return "";
			throw new MapException("Invalid primitive type", jpath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MapException("Failed to parse JSON input", jpath);
		}
	}

}
