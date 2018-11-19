package com.geckotechnology.xmljsonconvert.core;

import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser.Event;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JSONToXMLConverter {

	private static final String XMLNS_COLUMN = "xmlns:";
	private static final String XMLNS_ATTRIBUTE_NS_URI = "http://www.w3.org/2000/xmlns/";

	private String inputJSON;
	private XMLNodeSpecObject rootContainerXMLNodeSpec;
	private Document output;

	public JSONToXMLConverter(String inputJSON, XMLNodeSpecObject rootContainerXMLNodeSpec) throws MapException {
		if(inputJSON == null)
			throw new MapException("JSON input is null");
		if(rootContainerXMLNodeSpec == null)
			throw new MapException("rootXMLNodeSpec is null");
		this.inputJSON = inputJSON;
		this.rootContainerXMLNodeSpec = rootContainerXMLNodeSpec;
	}
	
	public void process() throws MapException {
		//System.out.println("***********");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			//should never happen
			throw new MapException("Internal problem while instanciating DocumentBuilder: " + e1);
		}
		output = docBuilder.newDocument();
		output.setXmlStandalone(true);
		SimplePath jpath = new SimplePath(SimplePath.JSON_PATH);
		Element rootElement = createObjectElement(output, rootContainerXMLNodeSpec, JSONSchemaForXML.DEFAULT_XML_ROOT_ELEMENT_NAME, jpath);
		output.appendChild(rootElement);
		JsonParserWrapper parser = null;
		try {
			parser = new JsonParserWrapper(Json.createParser(new StringReader(inputJSON)));
		} catch (Exception ex) {
			throw new MapException("Failed to parse JSON input", jpath);
		}
		Event e = parser.next(jpath);
		if(e != Event.START_OBJECT)
			throw new MapException("JSON input type must an object, starting with {", jpath);
		parseObject(parser, rootElement, rootContainerXMLNodeSpec, "", jpath);
	}
	
	public Document getOutput() {
		return output;
	}
	
	private static void parseArray(JsonParserWrapper parser, Element objectElement, XMLNodeSpecArray xmlNodeSpecArray, String currentPropertyName, SimplePath jpath)
		throws MapException {
		Element targetElement = objectElement; //current objectElement is array is not wrapped or new element to be created bellow
		String arrayFQElementName = currentPropertyName;
		if(xmlNodeSpecArray.isXMLWrapped()) {
			Element newArrayElement = createObjectElement(objectElement, xmlNodeSpecArray, currentPropertyName, jpath);
			arrayFQElementName = newArrayElement.getNodeName();
			objectElement.appendChild(newArrayElement);
			targetElement = newArrayElement;
		}
		int arrayIndex = 0;
		while (parser.hasNext(jpath)) {
			Event e = parser.next(jpath);
			if(e == Event.START_ARRAY) {
				jpath.pushIndex(arrayIndex++);
				if(!(xmlNodeSpecArray.getItemsXMLNodeSpec() instanceof XMLNodeSpecArray))
					throw new MapException("Value should not be a JSON array", jpath);
				parseArray(parser, targetElement, (XMLNodeSpecArray)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath);
				jpath.pop();
			}
			else if(e == Event.END_ARRAY)
				return;
			else if(e == Event.START_OBJECT) {
				jpath.pushIndex(arrayIndex++);
				if(!(xmlNodeSpecArray.getItemsXMLNodeSpec() instanceof XMLNodeSpecObject))
					throw new MapException("Value should not be a JSON object", jpath);
				Element childObjectElement = createObjectElement(targetElement, xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath); 
				targetElement.appendChild(childObjectElement);
				parseObject(parser, childObjectElement, (XMLNodeSpecObject)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath);
				jpath.pop();
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_NUMBER ||
					e == Event.VALUE_NULL || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE) {
				jpath.pushIndex(arrayIndex++);
				String memberValue = getAndValidateValue(parser, e, xmlNodeSpecArray.getItemsXMLNodeSpec(), jpath);
				addKeyValueLeafElement(targetElement, (XMLNodeSpecPrimitiveType)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, memberValue, jpath);
				jpath.pop();
			}
			else
				throw new MapException("Invalid event type", jpath);
		}
	}
	
	private static void parseObject(JsonParserWrapper parser, Element objectElement, XMLNodeSpecObject xmlNodeSpecObject, String currentPropertyName, SimplePath jpath)
			throws MapException {
		String memberKey = null;
		XMLNodeSpec childXmlNodeSpec = null;
		while (parser.hasNext(jpath)) {
			Event e = parser.next(jpath);
			if(e == Event.START_ARRAY) {
				if(!(childXmlNodeSpec instanceof XMLNodeSpecArray))
					throw new MapException("Value should not be a JSON array", jpath);
				parseArray(parser, objectElement, (XMLNodeSpecArray)childXmlNodeSpec, memberKey, jpath);
				jpath.pop();
			}
			else if(e == Event.START_OBJECT) {
				if(!(childXmlNodeSpec instanceof XMLNodeSpecObject))
					throw new MapException("Value should not be a JSON object", jpath);
				Element childObjectElement = createObjectElement(objectElement, childXmlNodeSpec, memberKey, jpath); 
				objectElement.appendChild(childObjectElement);
				parseObject(parser, childObjectElement, (XMLNodeSpecObject)childXmlNodeSpec, memberKey, jpath);
				jpath.pop();
			}
			else if(e == Event.END_OBJECT) {
				return;
			}
			else if(e == Event.KEY_NAME) {
				memberKey = parser.getString(jpath);
				jpath.pushElement(memberKey);
				childXmlNodeSpec = xmlNodeSpecObject.getJSONPropertyByName(memberKey);
				if(childXmlNodeSpec == null) {
					if(xmlNodeSpecObject.isAdditionalProperties()) {
						addAdditionalProperty(parser, objectElement, memberKey, jpath);
					}
					else
						throw new MapException("Property is not defined in JSON schema and additionalProperties is false", jpath);
				}
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE || e == Event.VALUE_NUMBER || e == Event.VALUE_NULL) {
				String memberValue = getAndValidateValue(parser, e, childXmlNodeSpec, jpath);
				addKeyValueLeafElement(objectElement, (XMLNodeSpecPrimitiveType)childXmlNodeSpec, memberKey, memberValue, jpath);
				jpath.pop();
			}
			else
				throw new MapException("Invalid event type", jpath);
		}		
	}
	
	private static String getAndValidateValue(JsonParserWrapper parser, Event currentEvent,  XMLNodeSpec xmlNodeSpec, SimplePath jpath) throws MapException {
		int nodeType = xmlNodeSpec.getNodeType();
		if(currentEvent == Event.VALUE_STRING) {
			if(nodeType != XMLNodeSpec.TYPE_STRING)
				throw new MapException("Value should not be a string", jpath);
			return parser.getString(jpath);
		}
		else if(currentEvent == Event.VALUE_NUMBER) {
			if(nodeType != XMLNodeSpec.TYPE_NUMBER && nodeType != XMLNodeSpec.TYPE_INTEGER)
				throw new MapException("Value should not be a number or an integer", jpath);
			return parser.getString(jpath);
		}
		else if(currentEvent == Event.VALUE_TRUE) {
			if(nodeType != XMLNodeSpec.TYPE_BOOLEAN)
				throw new MapException("Value should not be a boolean", jpath);
			return "true";
		}
		else if(currentEvent == Event.VALUE_FALSE) {
			if(nodeType != XMLNodeSpec.TYPE_BOOLEAN)
				throw new MapException("Value should not be a boolean", jpath);
			return "false";
		}
		else if(currentEvent == Event.VALUE_NULL) {
			//check if null object is allowed
			if(!xmlNodeSpec.isNullable())
				throw new MapException("Property is not nullable", jpath);
			return "null";
		}
		throw new MapException("Internal error", jpath);
	}

	private static void addKeyValueLeafElement(Element objectElement, XMLNodeSpecPrimitiveType propertyXMLNodeSpec, String objectKey, String objectValue, SimplePath jpath)
			throws MapException {
		//System.out.println("addKeyValueLeafElement objectKey=" + objectKey + ", objectElement.getNodeName()=" + objectElement.getNodeName());
		String targetFQELementName = propertyXMLNodeSpec.calculateTargetFullXMLName(objectKey);
		if(propertyXMLNodeSpec.isXMLAttribute()) {
			//System.out.println("is attribute targetFQELementName:" + targetFQELementName);
			objectElement.setAttribute(targetFQELementName, objectValue);
			if(propertyXMLNodeSpec.getXmlPrefix() != null && propertyXMLNodeSpec.getXmlNamespace() != null) {
				objectElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix(), propertyXMLNodeSpec.getXmlNamespace());
				//System.out.println("adding " + XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix() + "=" + propertyXMLNodeSpec.getXmlNamespace());
			}
			return;
		}
		Element childElement = createObjectElement(objectElement, propertyXMLNodeSpec, objectKey, jpath);
		if(propertyXMLNodeSpec.getXmlPrefix() != null && propertyXMLNodeSpec.getXmlNamespace() != null)
			childElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix(), propertyXMLNodeSpec.getXmlNamespace());
		objectElement.appendChild(childElement);
		childElement.appendChild(objectElement.getOwnerDocument().createTextNode(objectValue));
	}
	

	private static Element createObjectElement(Node node, XMLNodeSpec xmlNodeSpec, String currentPropertyName, SimplePath jpath) {
		String targetFQELementName = xmlNodeSpec.calculateTargetFullXMLName(currentPropertyName);
		Element element = null;
		if(node instanceof Document)
			element = ((Document)node).createElement(targetFQELementName);
		else
			element = node.getOwnerDocument().createElement(targetFQELementName);
		if(xmlNodeSpec.getXmlPrefix() != null && xmlNodeSpec.getXmlNamespace() != null)
			element.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + xmlNodeSpec.getXmlPrefix(), xmlNodeSpec.getXmlNamespace());
		return element;
	}
	
	private static void addAdditionalProperty(JsonParserWrapper parser, Element objectElement, String memberKey, SimplePath jpath) throws MapException {
		Event e = parser.next(jpath);
		if(e == Event.START_ARRAY) {
			//level 1 array
			addArrayWithoutSchema(parser, objectElement, memberKey, jpath);
			jpath.pop(); //previous index of array
		}		
		else if(e == Event.START_OBJECT) {
			Element propertyElement = objectElement.getOwnerDocument().createElement(memberKey);
			objectElement.appendChild(propertyElement);
			addObjectWithoutSchema(parser, propertyElement, memberKey, jpath);
		}
		else if(e == Event.VALUE_STRING || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE || e == Event.VALUE_NUMBER || e == Event.VALUE_NULL) {
			Element propertyElement = objectElement.getOwnerDocument().createElement(memberKey);
			objectElement.appendChild(propertyElement);
			propertyElement.appendChild(propertyElement.getOwnerDocument().createTextNode(parser.getPrimitiveValueAsString(jpath)));
		}
		else
			throw new MapException("Invalid property value structure", jpath);
		jpath.pop();
	}
	
	private static void addArrayWithoutSchema(JsonParserWrapper parser, Element propertyElement, String propertyName, SimplePath jpath) throws MapException {
		int arrayIndex = 0;
		jpath.pushIndex(arrayIndex);
		while (parser.hasNext(jpath)) {
			Event e = parser.next(jpath);
			if(e == Event.START_ARRAY) {
				//level 2 or more array
				Element childPropertyElement = propertyElement.getOwnerDocument().createElement(propertyName);
				propertyElement.appendChild(childPropertyElement);
				addArrayWithoutSchema(parser, childPropertyElement, propertyName, jpath); //may use "item" instead of propertyName
				jpath.pop(); //previous index of previous array
				jpath.pop(); //previous index of array
				jpath.pushIndex(++arrayIndex);
			}
			else if(e == Event.START_OBJECT) {
				Element childPropertyElement = propertyElement.getOwnerDocument().createElement(propertyName);
				propertyElement.appendChild(childPropertyElement);
				addObjectWithoutSchema(parser, childPropertyElement, propertyName, jpath);
				jpath.pop(); //previous index of array
				jpath.pushIndex(++arrayIndex);
			}
			else if(e == Event.END_ARRAY) {
				return;
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE || e == Event.VALUE_NUMBER || e == Event.VALUE_NULL) {
				Element childPropertyElement = propertyElement.getOwnerDocument().createElement(propertyName);
				propertyElement.appendChild(childPropertyElement);
				childPropertyElement.appendChild(propertyElement.getOwnerDocument().createTextNode(parser.getPrimitiveValueAsString(jpath)));
				jpath.pop(); //previous index of array
				jpath.pushIndex(++arrayIndex);
			}
			else
				throw new MapException("Invalid event type", jpath);
		}				
	}
	
	private static void addObjectWithoutSchema(JsonParserWrapper parser, Element propertyElement, String propertyName, SimplePath jpath) throws MapException {
		String memberKey = null;
		while (parser.hasNext(jpath)) {
			Event e = parser.next(jpath);
			if(e == Event.START_ARRAY) {
				//level 1 array
				addArrayWithoutSchema(parser, propertyElement, memberKey, jpath);
				jpath.pop(); //previous index of array
				jpath.pop(); //previous memberKey
			}
			else if(e == Event.START_OBJECT) {
				Element childPropertyElement = propertyElement.getOwnerDocument().createElement(memberKey);
				propertyElement.appendChild(childPropertyElement);
				addObjectWithoutSchema(parser, childPropertyElement, memberKey, jpath);
				jpath.pop(); //previous memberKey
			}
			else if(e == Event.END_OBJECT) {
				return;
			}
			else if(e == Event.KEY_NAME) {
				memberKey = parser.getString(jpath);
				jpath.pushElement(memberKey);
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE || e == Event.VALUE_NUMBER || e == Event.VALUE_NULL) {
				Element childPropertyElement = propertyElement.getOwnerDocument().createElement(memberKey);
				propertyElement.appendChild(childPropertyElement);
				childPropertyElement.appendChild(childPropertyElement.getOwnerDocument().createTextNode(parser.getPrimitiveValueAsString(jpath)));
				jpath.pop(); //previous memberKey
			}
			else
				throw new MapException("Invalid event type", jpath);
		}		
	}
}