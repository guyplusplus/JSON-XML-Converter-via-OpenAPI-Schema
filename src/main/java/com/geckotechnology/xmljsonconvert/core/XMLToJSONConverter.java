package com.geckotechnology.xmljsonconvert.core;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class XMLToJSONConverter {

	private String inputXML;
	private XMLNodeSpecObject rootContainerXMLNodeSpec;
	private JSONObject output;

	public XMLToJSONConverter(String inputXML, XMLNodeSpecObject rootContainerXMLNodeSpec) throws MapException {
		if(inputXML == null)
			throw new MapException("Input XML is null");
		if(rootContainerXMLNodeSpec == null)
			throw new MapException("rootXMLNodeSpec is null");
		this.inputXML = inputXML;
		this.rootContainerXMLNodeSpec = rootContainerXMLNodeSpec;
	}
	
	public void process() throws MapException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = null;
		try {
			reader = factory.createXMLStreamReader(new StringReader(inputXML));
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error");
		}
		//reader carries root element
		moveReaderToFirstStartElement(reader);
		//ensure root name is correct
		String fqElementName = calculateFQElementName(reader);
		String expectedFQELementName = rootContainerXMLNodeSpec.calculateTargetFullXMLName(JSONSchemaForXML.DEFAULT_XML_ROOT_ELEMENT_NAME);
		SimplePath xpath = new SimplePath(SimplePath.XML_PATH);
		xpath.pushElement(fqElementName);
		if(!fqElementName.equals(expectedFQELementName))
			throw new MapException("XML root node name is invalid", xpath);
		output = parseJSONObject(reader, rootContainerXMLNodeSpec, xpath);		
	}
	
	public JSONObject getOutput() {
		return output;
	}
	
	static private void moveReaderToFirstStartElement(XMLStreamReader reader) throws MapException {
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(reader.isWhiteSpace()) {
						//System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					throw new MapException("Characters at this location are not allowed", new SimplePath(SimplePath.XML_PATH));
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					//System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					throw new MapException("End element not allowed", new SimplePath(SimplePath.XML_PATH));
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					//System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					break;
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", new SimplePath(SimplePath.XML_PATH));
				}
			}
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error", new SimplePath(SimplePath.XML_PATH));
		}		
	}
	
	static private Object parseJSONValue(XMLStreamReader reader, XMLNodeSpec xmlNodeSpec, SimplePath xpath, String fqElementName) throws MapException {
		if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_OBJECT)
			return parseJSONObject(reader, (XMLNodeSpecObject)xmlNodeSpec, xpath);
		if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_ARRAY) {
			XMLNodeSpecArray xmlNodeSpecArray = (XMLNodeSpecArray)xmlNodeSpec;
			if(!xmlNodeSpecArray.isXMLWrapped())
				return parseJSONValue(reader, xmlNodeSpecArray.getItemsXMLNodeSpec(), xpath, fqElementName);
			return parseJSONArrayWrapped(reader, xmlNodeSpecArray, xpath, fqElementName);
		}
		return parseJSONPrimitiveType(reader, (XMLNodeSpecPrimitiveType)xmlNodeSpec, xpath);
	}
	
	static private JSONArray parseJSONArrayWrapped(XMLStreamReader reader, XMLNodeSpecArray xmlNodeSpecArray, SimplePath xpath, String wrapperFQElementName) throws MapException {
		JSONArray array = new JSONArray();
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(reader.isWhiteSpace()) {
						//System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					throw new MapException("Characters at this location are not allowed", xpath);
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					//System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					//System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					String fqElementName = calculateFQElementName(reader);
					xpath.pushElement(fqElementName);
					//check fqElementName is correct
					XMLNodeSpec itemsXMLNodeSpec = xmlNodeSpecArray.getItemsXMLNodeSpec();
					String expectedFQElementName = itemsXMLNodeSpec.calculateTargetFullXMLName(wrapperFQElementName);
					if(!fqElementName.equals(expectedFQElementName))
						throw new MapException("Wrapped element name is not matching specifications", xpath);
					if(itemsXMLNodeSpec.getXmlPrefix() != null && itemsXMLNodeSpec.getXmlNamespace() != null)
						if(!itemsXMLNodeSpec.getXmlNamespace().equals(reader.getNamespaceURI()))
							throw new MapException("Invalid namespace", xpath);
					xpath.pushIndex(array.length() + 1);
					Object childElement = parseJSONValue(reader, itemsXMLNodeSpec, xpath, fqElementName);
					array.put(childElement);
					xpath.pop().pop();
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
			return array;
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error", xpath);
		}		
	}
	
	static private Object parseJSONPrimitiveType(XMLStreamReader reader, XMLNodeSpecPrimitiveType xmlNodeSpecPrimitiveType, SimplePath xpath) throws MapException {
		String characters = ""; //Purposely not use StringBuilder as concatenation for this variable happens rarely
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(characters.length() == 0 && reader.isWhiteSpace()) {
						//System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					characters += reader.getText();
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					//System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					//System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					xpath.pushElement(reader.getLocalName());
					throw new MapException("New XML element not allowed for a primitive type property (string, boolean, integer, number, null)", xpath);
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
			return createObjectFromPrimitiveTypeProperty(characters, xmlNodeSpecPrimitiveType, xpath);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error", xpath);
		}
	}

	static private JSONObject parseJSONObject(XMLStreamReader reader, XMLNodeSpecObject xmlNodeSpecObject, SimplePath xpath) throws MapException {
		JSONObject o = new JSONObject();
		//add non wrapped arrays. When a wrapped array comes in, then the json array is added
		for(String jsonArrayName:xmlNodeSpecObject.getNonWrappedJSONArrayNames()) {
			o.put(jsonArrayName, new JSONArray());
			//System.out.println("--- proactive add array: " + jsonArrayName + " for xpath: " + xpath);
		}
		try {
			//handle attributes
			int attributeCount = reader.getAttributeCount();
			for(int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
				String prefixName = reader.getAttributePrefix(attributeIndex);
				String fqAttributeName = reader.getAttributeLocalName(attributeIndex);
				if(prefixName != null && prefixName.length() > 0)
					fqAttributeName = prefixName + ":" + fqAttributeName;
				xpath.pushXMLAttribute(fqAttributeName);
				//now attributeName is prefix:localName
				PropertyXMLNodeSpec attributePropertyXMLNodeSpec = xmlNodeSpecObject.getAttributePropertyXMLNodeSpecByName(fqAttributeName);
				if(attributePropertyXMLNodeSpec == null) {
					if(!xmlNodeSpecObject.isAdditionalProperties())
						throw new MapException("Attribute is not defined in JSON schema and additionalProperties is set to false", xpath);
					o.accumulate(fqAttributeName, reader.getAttributeValue(attributeIndex).trim());
					xpath.pop();
					continue;
				}
				if(!(attributePropertyXMLNodeSpec.getXmlNodeSpec().isPrimitiveType()))
					throw new MapException("Attribute should be a primitive type (string, boolean, integer, number, null)", xpath);
				if(prefixName != null && attributePropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace() != null)
					if(!attributePropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace().equals(reader.getAttributeNamespace(attributeIndex)))
						throw new MapException("Invalid namespace", xpath);
				Object attributeValue = createObjectFromPrimitiveTypeProperty(reader.getAttributeValue(attributeIndex), attributePropertyXMLNodeSpec.getXmlNodeSpec(), xpath); 
				addKeyValueToJSONObject(attributePropertyXMLNodeSpec.getPropertyName(), attributeValue, o, xpath, false);
				xpath.pop();
			}
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(reader.isWhiteSpace()) {
						//System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					throw new MapException("Characters at this location are not allowed", xpath);
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					//System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					String fqElementName = calculateFQElementName(reader);
					xpath.pushElement(fqElementName);
					//System.out.println(xpath + " START_ELEMENT: " + fqElementName);
					PropertyXMLNodeSpec childElementPropertyXMLNodeSpec = xmlNodeSpecObject.getChildElementPropertyXMLNodeSpecByName(fqElementName);
					if(childElementPropertyXMLNodeSpec == null) {
						if(!xmlNodeSpecObject.isAdditionalProperties())
							throw new MapException("Element is not defined in JSON schema and additionalProperties is set to false", xpath);
						Object propertyValue = parsePropertyValueWithoutSchema(reader, fqElementName, xpath);
						o.accumulate(fqElementName, propertyValue);
						xpath.pop();
						continue;
					}
					//adjust startElementXPath in case of a non wrapped array
					boolean isAppendKeyToArray = (childElementPropertyXMLNodeSpec.getXmlNodeSpec().getNodeType() == XMLNodeSpec.TYPE_ARRAY ?
							!((XMLNodeSpecArray)childElementPropertyXMLNodeSpec.getXmlNodeSpec()).isXMLWrapped() : false);
					if(isAppendKeyToArray)
						//startElementXPath += "[" + (o.getJSONArray(childElementPropertyXMLNodeSpec.getPropertyName()).length() + 1 )+ "]";
						xpath.pushIndex(o.getJSONArray(childElementPropertyXMLNodeSpec.getPropertyName()).length() + 1);
					//check namespace is correct
					if(childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlPrefix() != null && childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace() != null)
						if(!childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace().equals(reader.getNamespaceURI()))
							throw new MapException("Invalid namespace", xpath);
					//add child element
					Object childElement = parseJSONValue(reader, childElementPropertyXMLNodeSpec.getXmlNodeSpec(), xpath, fqElementName);
					addKeyValueToJSONObject(childElementPropertyXMLNodeSpec.getPropertyName(), childElement, o, xpath,
							isAppendKeyToArray);
					if(isAppendKeyToArray)
						xpath.pop();
					xpath.pop();
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
			return o;
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error", xpath);
		}
	}
	
	static private String calculateFQElementName(XMLStreamReader reader) {
		String fqElementName = reader.getLocalName(); //fully qualified (prefix:name, or name if no prefix)
		//add prefix is defined
		String prefix = reader.getPrefix();
		if(prefix != null && prefix.length() > 0)
			fqElementName = prefix + ":" + fqElementName;
		return fqElementName;
	}

	static private void addKeyValueToJSONObject(String key, Object value, JSONObject o, SimplePath xpath, boolean isAppendKeyToArray) throws MapException {
		if(isAppendKeyToArray) {
			o.append(key, value);
			return;
		}
		//check no duplicate key first
		if(o.opt(key) != null)
			throw new MapException("Duplicated element", xpath);
		//then add
		o.put(key, value);
	}

	static private Object createObjectFromPrimitiveTypeProperty(String characters, XMLNodeSpec xmlNodeSpec, SimplePath xpath) throws MapException {
		int nodeType = xmlNodeSpec.getNodeType();
		String charactersTrimmed = characters.trim();
		if(nodeType == XMLNodeSpec.TYPE_STRING) {
			return charactersTrimmed;
		}
		else if(characters.length() == 0) {
			if(!xmlNodeSpec.isNullable())
				throw new MapException("Property is not nullable", xpath);
			return JSONObject.NULL;
		}
		else if(nodeType == XMLNodeSpec.TYPE_BOOLEAN) {
			if(charactersTrimmed.equalsIgnoreCase("true"))
				 return Boolean.TRUE;
			else if(charactersTrimmed.equalsIgnoreCase("false"))
				return Boolean.FALSE;
			else
				throw new MapException("Failure to convert text to a boolean", xpath);
		}
		else if(nodeType == XMLNodeSpec.TYPE_INTEGER) {
			try {
				if(charactersTrimmed.length() > 9)
					//we use this trick to guess it could be a very large integer (int max value is 10 chars)
					//we keep Integer case for performance optimization
					return new BigInteger(charactersTrimmed);
				else
					return Integer.parseInt(charactersTrimmed);
			} catch (NumberFormatException e) {
				throw new MapException("Failure to convert text to an integer", xpath);
			}
		}
		else if(nodeType == XMLNodeSpec.TYPE_NUMBER) {
			try {
				return new BigDecimal(charactersTrimmed);
			} catch (NumberFormatException e) {
				throw new MapException("Failure to convert text to a number", xpath);
			}
		}
		//never happens
		throw new MapException("Unknown XMLNodeSpec type " + nodeType, xpath);
	}
	
	static private Object parsePropertyValueWithoutSchema(XMLStreamReader reader, String propertyName, SimplePath xpath) throws MapException {
		String characters = ""; //Purposely not use StringBuilder as concatenation for this variable happens rarely
		JSONObject o = null;
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(characters.length() == 0 && reader.isWhiteSpace()) {
						//System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					if(o != null)
						throw new MapException("Mix XML elements and text is not allowed", xpath);
					characters += reader.getText();
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					//System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					//System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					String fqElementName = calculateFQElementName(reader);
					xpath.pushElement(fqElementName);
					if(characters.length() != 0)
						throw new MapException("Mix XML elements and text is not allowed", xpath);
					int attributeCount = reader.getAttributeCount();
					for(int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
						String prefixName = reader.getAttributePrefix(attributeIndex);
						String fqAttributeName = reader.getAttributeLocalName(attributeIndex);
						if(prefixName != null && prefixName.length() > 0)
							fqAttributeName = prefixName + ":" + fqAttributeName;
						o.accumulate(fqAttributeName, reader.getAttributeValue(attributeIndex).trim());
					}
					Object innerObject = parsePropertyValueWithoutSchema(reader, fqElementName, xpath);
					if(o == null)
						o = new JSONObject();
					o.accumulate(fqElementName, innerObject);
					xpath.pop();
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
			if(o != null)
				return o;
			return characters.trim();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error", xpath);
		}
	}

}
