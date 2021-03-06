package com.geckotechnology.xmljsonconvert.core.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;

import com.geckotechnology.xmljsonconvert.core.JSONSchemaForXML;
import com.geckotechnology.xmljsonconvert.core.JSONSchemaLoadException;
import com.geckotechnology.xmljsonconvert.core.MapException;

/**
 * This test of scripts test the scenarios without any XML schema attribute
 * Assumption : JSON schema is correct
 * @author Guy
 * TODO: cleanup of test cases description
 */
public class JSONSchemaForXMLTest {
	
	private static final String JSON_TEST_FILES_FOLDER = "com/geckotechnology/xmljsonconvert/core/test/";
	
	private String convertInputStreamToString(InputStream inputStream, Charset charset) throws IOException {
		 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		return stringBuilder.toString();
	}

	@Test
	public void testJSONSchema_InvalidSchemas() {
		try {
			new JSONSchemaForXML(null);
			fail("null should throw exception");
		}
		catch(JSONSchemaLoadException e) {
			assertTrue(e.getMessage().indexOf("Failed to parse JSON") != -1);
		}
		try {
			new JSONSchemaForXML("asldkjlaksd");
			fail("garbage JSON should throw exception");
		}
		catch(JSONSchemaLoadException e) {
			assertTrue(e.getMessage().indexOf("Failed to parse JSON") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_Simple() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;
		
		JSONSchemaForXML jsonSchemaForXML = null;		
		
		try {
			//check that json schema parse fails and shows exact location
			jsonSchemaForXML = new JSONSchemaForXML("{\n   \"a\":\"1\",\n   \"b\": 2,\n   \"c\";\"12\"\n }");
			fail("garbage JSON should throw exception");
		} catch(JSONSchemaLoadException e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON") != -1);
			assertTrue(e.toString().indexOf("character 7 line 4") != -1);
		}

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_simple.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
		
		try {
			jsonSchemaForXML.mapXMLToJSONObject(null);
			fail("null should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Input XML is null") != -1);
		}
		try {
			//XML with just spaces
			jsonSchemaForXML.mapXMLToJSONObject("    ");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
		}
		try {
			//XML with no elements
			jsonSchemaForXML.mapXMLToJSONObject("asdasd");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
		}
		try {
			//XML is start root but no end root
			jsonSchemaForXML.mapXMLToJSONObject("<root>\n</cd>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//XML is start root but no end root
			jsonSchemaForXML.mapXMLToJSONObject("<root>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//use of XML reference
			jsonSchemaForXML.mapXMLToJSONObject("<?xml version=\"1.0\" encoding=\"utf-8\"?> <!DOCTYPE sample [ <!NOTATION vrml PUBLIC \"VRML 1.0\"> <!ENTITY dotto \"Dottoro\"> ]> <entityTest>hello</entityTest>");
			fail("XML Reference should not be accepted");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Unknown StAX event type") != -1);
		}
		try {
			//wrong XML root node
			jsonSchemaForXML.mapXMLToJSONObject("<AA></AA>");
			fail("Wrong root node should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML root node name is invalid") != -1);
			assertTrue(e.toString().indexOf("path: /AA") != -1);
		}
		try {
			//wired element
			o = jsonSchemaForXML.mapXMLToJSONObject("<root>abc</root>");
			fail("Show throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//wired characters at the wrong location
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aString>aaa</aString>abc</root>");
			fail("Show throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSONObject("<root><aString>ccc</aString2></root>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSONObject("<root>< aString >ccc</ aString ></root>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSONObject("<root><aString>ccc<b></b></aString></root>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("New XML element not allowed for a primitive type property") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString/b") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSONObject("<root><aString>aaa</aString>asdad<aNumber>123.456</aNumber></root>");
			fail("garbage XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		//json to xml: null input
		try {
			jsonSchemaForXML.mapJSONToXMLDocument(null);
			fail("garbage JSON should throw exception");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("JSON input is null") != -1);
		}
		//json to xml: empty input
		try {
			jsonSchemaForXML.mapJSONToXMLDocument("");
			fail("garbage JSON should throw exception");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $") != -1);
		}
		//json to xml: garbage json input
		try {
			jsonSchemaForXML.mapJSONToXMLDocument("asdasd");
			fail("garbage JSON should throw exception");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $") != -1);
		}
		//json to xml: garbage json input after a while
		try {
			jsonSchemaForXML.mapJSONToXMLDocument("{\"aNumber\":-123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":two}}");
			fail("garbage JSON should throw exception");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.str2") != -1);
		}
		//json to xml: not an object
		try {
			jsonSchemaForXML.mapJSONToXMLDocument("123");
			fail("non object JSON should throw exception");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("JSON input type must an object, starting with {") != -1);
			assertTrue(e.toString().indexOf("path: $") != -1);
		}

		try {
			//empty content
			xml = "<root></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root/>"));
			//empty root with <?xml...
			xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><root></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root/>"));
			//empty string property
			xml = "<root><aString></aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString/></root>"));
			//empty string property
			xml = "<root>\n\t<aString>\n\t</aString>\n</root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString/></root>"));
			//empty string property
			xml = "<root><aString/></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString/></root>"));
			//simple string property
			xml = "<root><aString>aaa</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//simple string property with unicode (2 styles)
			xml = "<root><aString>a\u0e01a&#x0e02;a</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"a\u0e01a\u0e02a\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>a\u0e01a\u0e02a</aString></root>"));
			//simple string property
			xml = "<root>\n  <aString>aaa</aString>\n</root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//simple string property "null"
			xml = "<root>\n\t<aString>null</aString>\n</root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"null\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>null</aString></root>"));
			//simple string property
			xml = "<root>\n\t<aString>\n\t\taaa\n\t</aString>\n</root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));

			//Test XML comment (before element)
			xml = "<root><!-- This is a comment --><aString>aaa</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//Test XML comment (start of element)
			xml = "<root><aString><!-- This is a comment -->aaa</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//Test XML comment (middle of element)
			xml = "<root><aString>a<!-- This is a comment -->aa</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));

			//Test CDATA
			xml = "<root><aString><![CDATA[aaa]]></aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//Test multi lines
			xml = "<root>   \n   \t  \n    <aString>    \n    aaa      \n  \t  \n  </aString>  \n   \n   </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>aaa</aString></root>"));
			//Test escapes
			xml = "<root><aString>1\n2\t3&amp;4&quot;5/6\\7</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"1\\n2\\t3&4\\\"5/6\\\\7\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>1\r\n2\t3&amp;4\"5/6\\7</aString></root>"));
			
			//Test escapes with spaces
			xml = "<root><aString>1\n2\t3&amp;4 \n    \n &quot;5/6\\7</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"1\\n2\\t3&4 \\n    \\n \\\"5/6\\\\7\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>1\r\n2\t3&amp;4 \r\n    \r\n \"5/6\\7</aString></root>"));
			//Test spaces and new lines in JSON input have no impact
			d = jsonSchemaForXML.mapJSONToXMLDocument("    \n\n\t{\"aString\":\"1\\n2\\t3&4 \\n    \\n \\\"5/6\\\\7\"}\n\n\t\n");
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>1\r\n2\t3&amp;4 \r\n    \r\n \"5/6\\7</aString></root>"));			
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//simple aNumber property
			xml = "<root><aNumber>123.456</aNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple aNumber property with spaces and new line
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aNumber>   \n  \t  123.456 \t  \n \t  </aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber></root>"));
			//simple string + number property
			xml = "<root><aString>aaa</aString><aNumber>123.456</aNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString></root>"));			
			//simple string + number property
			xml = "<root><aString>aaa</aString>  \t  \n  \t   <aNumber>-123.456000</aNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":-123.456,\"aString\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>-123.456</aNumber><aString>aaa</aString></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//empty aNumber property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aNumber></aNumber></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNumber") != -1);
		}

		try {
			//duplicated aString property name
			jsonSchemaForXML.mapXMLToJSONObject("<root><aString>aaa</aString><aString>bbb</aString></root>");
			fail("duplicate element should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);
		}
		try {
			//simple anInteger property
			xml = "<root><anInteger>123</anInteger></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":123}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple anInteger property
			xml = "<root><anInteger>0</anInteger></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":0}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple anInteger property
			xml = "<root><anInteger>-66</anInteger></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":-66}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple anInteger property
			xml = "<root><anInteger>123456789012345678901234567890</anInteger></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":123456789012345678901234567890}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple anInteger property
			xml = "<root><anInteger>-123456789012345678901234567890</anInteger></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":-123456789012345678901234567890}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//empty anInteger property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><anInteger></anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//anInteger is a decimal property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><anInteger>123.456</anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//simple aBoolean property
			xml = "<root><aBoolean>TRue</aBoolean></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aBoolean\":true}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aBoolean>true</aBoolean></root>"));
			xml = "<root><aBoolean> \t \n FAlse     \t \n   \t   </aBoolean></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aBoolean\":false}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aBoolean>false</aBoolean></root>"));
			xml = "<root><aNullableNumber/></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNullableNumber\":null}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//empty aBoolean property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aBoolean></aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//invalid aBoolean property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aBoolean>truefalse</aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a boolean") != -1);
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//invalid aNull property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aNumber>null</aNumber></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a number") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNumber") != -1);
		}
		try {
			//invalid aNull property
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"aNumber\":null}");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.aNumber") != -1);
		}
		try {
			//unknown property aNumber2
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aNumber2>123</aNumber2></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Element is not defined in JSON schema") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNumber2") != -1);
		}
		try {
			//simple aNumber property
			xml = "<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber><aNullableNumber></aNullableNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"aNullableNumber\":null}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString><aNullableNumber/><anObject><str1>one</str1><str2>two</str2></anObject></root>"));
			xml = "<root><aString>aaa</aString><anObject2><str1>one</str1><str2>two</str2><nb1>777</nb1></anObject2><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\",\"nb1\":777}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><anObject2><str1>one</str1><str2>two</str2><nb1>777</nb1></anObject2></root>"));
			xml = "<root><anObject2><str1>one</str1><str2>two</str2></anObject2><anObject><str1>one</str1><str2>two</str2></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><anObject><str1>one</str1><str2>two</str2></anObject><anObject2><str1>one</str1><str2>two</str2></anObject2></root>"));
			xml = "<root><aString>aaa</aString><anObject></anObject><aNumber>123.456</aNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString><anObject/></root>"));
			//simple aNumber property
			xml = "<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber><aNullableNumber></aNullableNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"aNullableNumber\":null}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString><aNullableNumber/><anObject><str1>one</str1><str2>two</str2></anObject></root>"));
			//additionalProperty (both attribute and element)
			xml = "<root><aString>aaa</aString><anObject adprop2=\"bbb\"><str1>one</str1><str2>two</str2><adprop1>aaa</adprop1></anObject><aNumber>123.456</aNumber><aNullableNumber></aNullableNumber></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\",\"adprop1\":\"aaa\",\"adprop2\":\"bbb\"},\"aNullableNumber\":null}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aNumber>123.456</aNumber><aString>aaa</aString><aNullableNumber/><anObject><str1>one</str1><str2>two</str2><adprop1>aaa</adprop1><adprop2>bbb</adprop2></anObject></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//duplicated anObject object name
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber><anObject><str1>one</str1><str2>two</str2></anObject></root>");
			fail("duplicate element should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/anObject") != -1);
		}
		
		try {
			//unknown property aNumber2
			o = new JSONObject("{\"anObject\":123}");
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			fail("Should fail");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("Value should not be a number or an integer") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject") != -1);
		}

		try {
			//unknown property aNumber2
			o = new JSONObject("{\"aNumber\":{\"a\":\"b\"}}");
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			fail("Should fail");
		}
		catch(Exception e) {
			//ok
			assertTrue(e.toString().indexOf("Value should not be a JSON object") != -1);
			assertTrue(e.toString().indexOf("path: $.aNumber") != -1);
		}
}

	@Test
	public void testJSONSchema_PropertyXML() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_propertyXML.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			xml = "<root><str1>aaa</str1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty string property
			xml = "<root><str1></str1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><str1/></root>"));

			//simple string property
			xml = "<root><xmlstr2>aaa</xmlstr2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty string property
			xml = "<root><xmlstr2></xmlstr2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><xmlstr2/></root>"));

			//simple string property
			xml = "<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3>aaa</pref3:str3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><pref3:str3>aaa</pref3:str3></root>"));
			//empty string property
			xml = "<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3></pref3:str3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><pref3:str3/></root>"));

			//simple string property
			xml = "<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\">aaa</pref4:str4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty string property
			xml = "<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\"></pref4:str4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\"/></root>"));
			//namespace is declared above
			xml = "<root xmlns:pref4=\"http://example.com/schema\"><pref4:str4>aaa</pref4:str4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\">aaa</pref4:str4></root>"));

			//simple string property
			xml = "<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\">aaa</pref5:xmlstr5></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty string property
			xml = "<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\"></pref5:xmlstr5></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\"/></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//wrong namespace
			o = jsonSchemaForXML.mapXMLToJSONObject("<root xmlns:pref4=\"http://example.com/schema2\"><pref4:str4>aaa</pref4:str4></root>");
			fail("wrong name should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/pref4:str4") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_RootXML() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_rootxml.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			//simple string property
			xml = "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1>aaa</str1></soap:xmlroot>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1>aaa</str1></soap:xmlroot>"));
			//empty string property
			xml = "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1></str1></soap:xmlroot>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1/></soap:xmlroot>"));

			//simple string property
			xml = "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2>aaa</xmlstr2></soap:xmlroot>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty string property
			xml = "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2></xmlstr2></soap:xmlroot>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2/></soap:xmlroot>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSONObject("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\"><xmlstr2>aaa</xmlstr2></soap:xmlroot>");
			fail("soap:encodingStyle is unknown, should raise exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Attribute is not defined") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_Attributes() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_attribute.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			xml = "<root><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str0\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			
			//simple string property
			xml = "<root str1=\"bbb\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"bbb\",\"str0\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//simple string property
			xml = "<root str1=\"\tbbb\t\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"bbb\",\"str0\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root str1=\"bbb\"><str0>aaa</str0></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//duplicated property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root str1=\"bbb\" str1=\"ccc\"><str0>aaa</str0></root>");
			fail("duplicated property should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing") != -1);
		}
		
		try {
			//unknown string property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root str999=\"bbb\"><str0>aaa</str0></root>");
			fail("unknown string property should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Attribute is not defined") != -1);
			assertTrue(e.toString().indexOf("path: /root/@str999") != -1);			
		}
		
		try {
			//property is not mistaken with element
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><str0>aaa</str0><str1>bbb</str1></root>");
			fail("property is mistaken with element");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Element is not defined") != -1);
			assertTrue(e.toString().indexOf("path: /root/str1") != -1);			
		}
		
		try {
			//simple string property
			xml = "<root xmlstr2=\"bbb\" xmlint1=\"123\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":123,\"str2\":\"bbb\",\"str0\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root xmlint1=\"123\" xmlstr2=\"bbb\"><str0>aaa</str0></root>"));			
			//empty string properties
			xml = "<root xmlstr2=\"\" xmlint1=\"00012345678901234567890\"><str0></str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"\"}")));
			//string property with spaces, int property with spaces
			xml = "<root xmlstr2=\"     \" xmlint1=\"     12345678901234567890     \"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//invalid integer property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root xmlstr2=\"bbb\" xmlint1=\"abc\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//empty integer property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root xmlstr2=\"bbb\" xmlint1=\"\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//simple string property
			xml = "<root xmlns:pref3=\"http://example.com/schema\" pref3:str3=\"bbb\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"bbb\",\"str0\":\"aaa\"}")));
			//can not do JSON to XML as pref3 namespace is not defined
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			xml = "<root xmlns:pref4=\"http://example.com/schema\" pref4:str4=\"bbb\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"bbb\",\"str0\":\"aaa\"}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//missing namespace
			o = jsonSchemaForXML.mapXMLToJSONObject("<root pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			fail("missing namespace should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
		}
		
		try {
			//wrong namespace
			o = jsonSchemaForXML.mapXMLToJSONObject("<root xmlns:pref4=\"http://example.com/schema2\" pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			fail("wrong namespace should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/@pref4") != -1);			
		}
		
		try {
			//simple string property
			xml = "<root xmlns:pref5=\"http://example.com/schema\" pref5:xmlstr5=\"bbb\"><str0>aaa</str0></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"bbb\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
	}

	@Test
	public void testJSONSchema_Arrays() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_arrays.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
		
		//relationship is non wrapped array, no name change
		//phones1 is non wrapped array, name change none
		//phones2 is non wrapped array, name change items
		//phones3 is wrapped array,     name change wrapper and items
		//phones4 is wrapped array,     name change wrapper
		//
		//array_n are 3 layers of array
		//arrayOfArrays :	wrapped(renamed)		wrapped(renamed)		int(renamed)  
		//arrayOfArrays2 :	not wrapped				wrapped(renamed)		int(renamed)  

		//arrayOfArrays3 :	wrapped(renamed)		wrapped(renamed)		string(renamed)   
		//arrayOfArrays4 :	wrapped(renamed)		wrapped(renamed)		int(not renamed)  
		//arrayOfArrays5 :	wrapped(renamed)		wrapped(not renamed)	int(renamed)  
		//arrayOfArrays6 :	wrapped(renamed)		wrapped(not renamed)	int(not renamed)  
		//arrayOfArrays7 :	wrapped(not renamed)	wrapped(renamed)		int(renamed)  
		//arrayOfArrays8 :	wrapped(not renamed)	wrapped(renamed)		int(not renamed)  
		//arrayOfArrays9 :	wrapped(not renamed)	wrapped(not renamed)	int(renamed)  
		//arrayOfArrays10 :	wrapped(not renamed)	wrapped(not renamed)	int(not renamed)  

		try {
			//empty structure
			xml = "<root></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root/>"));
			//array of objects
			xml = "<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>17</age></relationships></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[{\"name\":\"name 1\"},{\"name\":\"name 2\",\"age\":17}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//array of objects, wrong 2nd object
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>aaa</age></relationships></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/relationships[2]/age") != -1);			
		}
		
		try {
			//array item is a simple string
			xml = "<root><phones1></phones1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><phones1/></root>"));
			
			//array item is a simple string
			xml = "<root><phones1>p1</phones1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple string
			xml = "<root><phones1>p1</phones1><phones1>p2</phones1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\",\"p2\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));

			//array item is a simple string
			xml = "<root><PHONE_2></PHONE_2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><PHONE_2/></root>"));

			//array item is a simple string
			xml = "<root><PHONE_2>p1</PHONE_2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"p1\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple string
			xml = "<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple string
			xml = "<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2><aString>abc</aString></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>abc</aString><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2></root>"));
			//array item is a simple string
			xml = "<root><PHONE_2>p1</PHONE_2><aString>abc</aString><PHONE_2>p2</PHONE_2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><aString>abc</aString><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple string with duplicated property
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONE_2>p1</PHONE_2><aString>abc</aString><PHONE_2>p2</PHONE_2><aString>abc</aString></root>");
			fail("duplicated property - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);			
		}
		
		try {
			//array item is a simple string
			xml = "<root><PHONES_3></PHONES_3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><PHONES_3/></root>"));

			//array item is a simple string
			xml = "<root><PHONES_3><PHONE_3></PHONE_3></PHONES_3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><PHONES_3><PHONE_3/></PHONES_3></root>"));
			//array item is a simple string
			xml = "<root><PHONES_3><PHONE_3>123</PHONE_3></PHONES_3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			xml = "<root><PHONES_3><PHONE_3>123</PHONE_3><PHONE_3>456</PHONE_3></PHONES_3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\",\"456\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			
			//array item is a simple string
			xml = "<root><PHONES_4></PHONES_4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><PHONES_4/></root>"));
			//array item is a simple string
			xml = "<root><PHONES_4><PHONES_4></PHONES_4></PHONES_4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[\"\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><PHONES_4><PHONES_4/></PHONES_4></root>"));
			//array item is a simple string
			xml = "<root><PHONES_4><PHONES_4>123</PHONES_4></PHONES_4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[\"123\"]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//wrapped array, duplicated wrapped entry
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONES_3><PHONE_3>1</PHONE_3><PHONE_3>2</PHONE_3></PHONES_3><PHONES_3><PHONE_3>3</PHONE_3><PHONE_3>4</PHONE_3></PHONES_3></root>");
			fail("wrapped array, duplicated wrapped entry - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3") != -1);			
		}
		
		try {
			//wrapped array, duplicated wrapped entry
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONES_4><PHONES_4>1</PHONES_4><PHONES_4>2</PHONES_4></PHONES_4><PHONES_4><PHONES_4>4</PHONES_4><PHONES_4>4</PHONES_4></PHONES_4></root>");
			fail("wrapped array, duplicated wrapped entry - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_4") != -1);			
		}
		
		try {
			//wrapped array, wrapped entry is wrong name
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONES_3><PHONE_3>1</PHONE_3><PHONE_3>2</PHONE_3><PHONE_33>3</PHONE_33></PHONES_3></root>");
			fail("wrapped entry is wrong name - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Wrapped element name is not matching specifications") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3/PHONE_33") != -1);			
		}
		
		try {
			//wrapped array, wrapped entry is wrong name
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONES_4><PHONES_4>1</PHONES_4><PHONES_4>2</PHONES_4><PHONES_44>4</PHONES_44></PHONES_4></root>");
			fail("wrapped entry is wrong name - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Wrapped element name is not matching specifications") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_4/PHONES_44") != -1);			
		}
		
		try {
			//wrapped array, string at wrong place
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><PHONES_3><PHONE_3>1</PHONE_3>3<PHONE_3>2</PHONE_3></PHONES_3></root>");
			fail("wrapped array, string at wrong place - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3") != -1);			
		}
		
		try {
			//array item is a simple integers
			xml = "<root><INTS_1></INTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><INTS_1/></root>"));
			//array item is a simple integers
			xml = "<root><INTS_1><INT_1>123</INT_1></INTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[123],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple integers
			xml = "<root><INTS_1><INT_1>123</INT_1><INT_1>456</INT_1></INTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[123,456],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));

			//array item is a simple integers
			xml = "<root><OBJECTS_1></OBJECTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><OBJECTS_1/></root>"));
			//array item is a simple integers
			xml = "<root><OBJECTS_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><OBJECTS_1><OBJECT_1/></OBJECTS_1></root>"));
			//array item is a simple integers
			xml = "<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"18\"></OBJECT_1></OBJECTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{\"name\":\"Joe\"},{\"age\":18}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"18\"/></OBJECTS_1></root>"));
			//array item is a simple integers
			xml = "<root><OBJECTS_1><OBJECT_1></OBJECT_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{},{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><OBJECTS_1><OBJECT_1/><OBJECT_1/></OBJECTS_1></root>"));
		}
		catch(Exception e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"aaa\"></age></OBJECT_1></OBJECTS_1></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/OBJECTS_1/OBJECT_1[2]/@age") != -1);			
		}	

		try {
			//array item is a simple integers
			xml = "<root></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root/>"));
			//array item is a simple integers
			xml = "<root><ARRAYS></ARRAYS></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS/></root>"));
			//array item is a simple integers
			xml = "<root><ARRAYS><ARRAY></ARRAY></ARRAYS></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS><ARRAY/></ARRAYS></root>"));
			//array item is a simple integers
			xml = "<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY></ARRAYS></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple integers
			xml = "<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>4</INT></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[4],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple integers
			xml = "<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY/><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>a</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT></INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>1</INT><INT></INT><INT>3</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[2]/INT[2]") != -1);			
		}	

		try {
			//array item is a simple integers
			xml = "<root><ARRAY2></ARRAY2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[]],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAY2/></root>"));
			//array item is a simple integers
			xml = "<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3]],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array item is a simple integers
			xml = "<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2><ARRAY2><INT>4</INT></ARRAY2><ARRAY2><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3],[4],[5,6,7,8]],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));

			//array item is a simple integers
			xml = "<root><ARRAYS3></ARRAYS3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS3/></root>"));
			//array item is a simple integers
			xml = "<root><ARRAYS3><ARRAY3></ARRAY3></ARRAYS3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS3><ARRAY3/></ARRAYS3></root>"));
			//array item is a simple integers
			xml = "<root><ARRAYS3><ARRAY3><STRING></STRING></ARRAY3></ARRAYS3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"\"]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS3><ARRAY3><STRING/></ARRAY3></ARRAYS3></root>"));
			//array item is a simple integers
			xml = "<root><ARRAYS3><ARRAY3><STRING>1</STRING><STRING></STRING><STRING>3</STRING></ARRAY3></ARRAYS3></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"1\",\"\",\"3\"]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS3><ARRAY3><STRING>1</STRING><STRING/><STRING>3</STRING></ARRAY3></ARRAYS3></root>"));
			
			xml = "<root><ARRAYS4><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY></ARRAY><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></ARRAYS4></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays4\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS4><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY/><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></ARRAYS4></root>"));
			xml = "<root><ARRAYS5><ARRAYS5><INT>1</INT><INT>2</INT><INT>3</INT></ARRAYS5><ARRAYS5></ARRAYS5><ARRAYS5><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAYS5></ARRAYS5></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays5\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS5><ARRAYS5><INT>1</INT><INT>2</INT><INT>3</INT></ARRAYS5><ARRAYS5/><ARRAYS5><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAYS5></ARRAYS5></root>"));
			xml = "<root><ARRAYS6><ARRAYS6><ARRAYS6>1</ARRAYS6><ARRAYS6>2</ARRAYS6><ARRAYS6>3</ARRAYS6></ARRAYS6><ARRAYS6></ARRAYS6><ARRAYS6><ARRAYS6>5</ARRAYS6><ARRAYS6>6</ARRAYS6><ARRAYS6>7</ARRAYS6><ARRAYS6>8</ARRAYS6></ARRAYS6></ARRAYS6></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays6\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><ARRAYS6><ARRAYS6><ARRAYS6>1</ARRAYS6><ARRAYS6>2</ARRAYS6><ARRAYS6>3</ARRAYS6></ARRAYS6><ARRAYS6/><ARRAYS6><ARRAYS6>5</ARRAYS6><ARRAYS6>6</ARRAYS6><ARRAYS6>7</ARRAYS6><ARRAYS6>8</ARRAYS6></ARRAYS6></ARRAYS6></root>"));
			xml = "<root><arrayOfArrays7><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></arrayOfArrays7></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays7\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><arrayOfArrays7><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY/><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></arrayOfArrays7></root>"));
			xml = "<root><arrayOfArrays8><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY></ARRAY><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></arrayOfArrays8></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays8\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><arrayOfArrays8><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY/><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></arrayOfArrays8></root>"));
			xml = "<root><arrayOfArrays9><arrayOfArrays9><INT>1</INT><INT>2</INT><INT>3</INT></arrayOfArrays9><arrayOfArrays9></arrayOfArrays9><arrayOfArrays9><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></arrayOfArrays9></arrayOfArrays9></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays9\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><arrayOfArrays9><arrayOfArrays9><INT>1</INT><INT>2</INT><INT>3</INT></arrayOfArrays9><arrayOfArrays9/><arrayOfArrays9><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></arrayOfArrays9></arrayOfArrays9></root>"));
			xml = "<root><arrayOfArrays10><arrayOfArrays10><arrayOfArrays10>1</arrayOfArrays10><arrayOfArrays10>2</arrayOfArrays10><arrayOfArrays10>3</arrayOfArrays10></arrayOfArrays10><arrayOfArrays10></arrayOfArrays10><arrayOfArrays10><arrayOfArrays10>5</arrayOfArrays10><arrayOfArrays10>6</arrayOfArrays10><arrayOfArrays10>7</arrayOfArrays10><arrayOfArrays10>8</arrayOfArrays10></arrayOfArrays10></arrayOfArrays10></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays10\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><arrayOfArrays10><arrayOfArrays10><arrayOfArrays10>1</arrayOfArrays10><arrayOfArrays10>2</arrayOfArrays10><arrayOfArrays10>3</arrayOfArrays10></arrayOfArrays10><arrayOfArrays10/><arrayOfArrays10><arrayOfArrays10>5</arrayOfArrays10><arrayOfArrays10>6</arrayOfArrays10><arrayOfArrays10>7</arrayOfArrays10><arrayOfArrays10>8</arrayOfArrays10></arrayOfArrays10></arrayOfArrays10></root>"));
			xml = "<root><arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>1</ns1:arrayOfArrays11><ns1:arrayOfArrays11>2</ns1:arrayOfArrays11><ns1:arrayOfArrays11>3</ns1:arrayOfArrays11></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>5</ns1:arrayOfArrays11><ns1:arrayOfArrays11>6</ns1:arrayOfArrays11><ns1:arrayOfArrays11>7</ns1:arrayOfArrays11><ns1:arrayOfArrays11>8</ns1:arrayOfArrays11></ns1:arrayOfArrays11></arrayOfArrays11></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays11\":[[1,2,3],[],[5,6,7,8]]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>1</ns1:arrayOfArrays11><ns1:arrayOfArrays11>2</ns1:arrayOfArrays11><ns1:arrayOfArrays11>3</ns1:arrayOfArrays11></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"/><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>5</ns1:arrayOfArrays11><ns1:arrayOfArrays11>6</ns1:arrayOfArrays11><ns1:arrayOfArrays11>7</ns1:arrayOfArrays11><ns1:arrayOfArrays11>8</ns1:arrayOfArrays11></ns1:arrayOfArrays11></arrayOfArrays11></root>"));
			xml = "<root><arrayOfArrays12 xmlns:ns2=\"http://test.com\"><arrayOfArrays12><ns2:arrayOfArrays12>1</ns2:arrayOfArrays12><ns2:arrayOfArrays12>2</ns2:arrayOfArrays12><ns2:arrayOfArrays12>3</ns2:arrayOfArrays12></arrayOfArrays12><arrayOfArrays12></arrayOfArrays12><arrayOfArrays12><ns2:arrayOfArrays12>5</ns2:arrayOfArrays12><ns2:arrayOfArrays12>6</ns2:arrayOfArrays12><ns2:arrayOfArrays12>7</ns2:arrayOfArrays12><ns2:arrayOfArrays12>8</ns2:arrayOfArrays12></arrayOfArrays12></arrayOfArrays12></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays12\":[[1,2,3],[],[5,6,7,8]]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//invalid namespace
			o = jsonSchemaForXML.mapXMLToJSONObject("<root><arrayOfArrays11 xmlns:ns1=\"http://test2.com\"><ns1:arrayOfArrays11><ns1:arrayOfArrays11>1</ns1:arrayOfArrays11><ns1:arrayOfArrays11>2</ns1:arrayOfArrays11><ns1:arrayOfArrays11>3</ns1:arrayOfArrays11></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>5</ns1:arrayOfArrays11><ns1:arrayOfArrays11>6</ns1:arrayOfArrays11><ns1:arrayOfArrays11>7</ns1:arrayOfArrays11><ns1:arrayOfArrays11>8</ns1:arrayOfArrays11></ns1:arrayOfArrays11></arrayOfArrays11></root>");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/arrayOfArrays11/ns1:arrayOfArrays11") != -1);			
		}	

		try {
			//invalid value in internal array
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"arrayOfArrays2\":[[1,2,3],[],[null,6,7,8]]}");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.arrayOfArrays2[2][0]") != -1);			
		}	

		try {
			//invalid value in internal array
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"arrayOfArrays2\":[[1,2,3],[],[5,6,7,[]]]}");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Value should not be a JSON array") != -1);
			assertTrue(e.toString().indexOf("path: $.arrayOfArrays2[2][3]") != -1);			
		}	

		try {
			//invalid value in internal array
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"arrayOfArrays2\":[[1,2,3],[],[\"a\",6,7,8]]}");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Value should not be a string") != -1);
			assertTrue(e.toString().indexOf("path: $.arrayOfArrays2[2][0]") != -1);			
		}	

		try {
			//invalid value in internal array
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"arrayOfArrays2\":[[1,2,3],\"b\",[5,6,7,8]]}");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Value should not be a string") != -1);
			assertTrue(e.toString().indexOf("path: $.arrayOfArrays2[1]") != -1);			
		}	
		
		try {
			//array with 1 null item
			xml = "<root><numbers1><numbers1>1</numbers1><numbers1></numbers1><numbers1>3</numbers1></numbers1></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"numbers1\":[1,null,3],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><numbers1><numbers1>1</numbers1><numbers1/><numbers1>3</numbers1></numbers1></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

	}
	
	@Test
	public void testJSONSchemaForXML_Definitions() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_definitions.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}

		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			xml = "<root><shipping_address id=\"1\"><city>Washington</city></shipping_address><billing_address id=\"2\"></billing_address></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"city\":\"Washington\"},\"billing_address\":{\"id\":2}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><billing_address id=\"2\"/><shipping_address id=\"1\"><city>Washington</city></shipping_address></root>"));
			xml = "<root><billing_address id=\"2\"><PHONES><PHONE>1</PHONE><PHONE>2</PHONE></PHONES></billing_address></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"billing_address\":{\"id\":2,\"phones\":[1,2]}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			xml = "<root><shipping_address id=\"1\"><city>Washington</city><ZIPCODE>1234</ZIPCODE></shipping_address><billing_address id=\"2\"></billing_address></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"city\":\"Washington\",\"zipcode\":1234},\"billing_address\":{\"id\":2}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><billing_address id=\"2\"/><shipping_address id=\"1\"><ZIPCODE>1234</ZIPCODE><city>Washington</city></shipping_address></root>"));
			xml = "<root><shipping_address id=\"1\"><destination><name>John Dust</name><children><name>John Jr 1</name></children><children><name>John Jr 2</name></children></destination><city>Washington</city><ZIPCODE>1234</ZIPCODE></shipping_address><billing_address id=\"2\"></billing_address></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"destination\":{\"name\":\"John Dust\", \"children\":[ {\"name\":\"John Jr 1\",\"children\":[]},{\"name\":\"John Jr 2\",\"children\":[]}]},\"city\":\"Washington\",\"zipcode\":1234},\"billing_address\":{\"id\":2}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><billing_address id=\"2\"/><shipping_address id=\"1\"><ZIPCODE>1234</ZIPCODE><city>Washington</city><destination><children><name>John Jr 1</name></children><children><name>John Jr 2</name></children><name>John Dust</name></destination></shipping_address></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"shipping_address\":{\"id\":1,\"destination\":{\"name\":\"John Dust\", \"children\":[ {\"name\":\"John Jr 1\",\"children\":[]},{\"name2\":\"John Jr 2\",\"children\":[]}]},\"city\":\"Washington\",\"zipcode\":123z4},\"billing_address\":{\"id\":2}}");
			fail("invalid namespace should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not defined in JSON schema") != -1);
			assertTrue(e.toString().indexOf("path: $.shipping_address.destination.children[1].name2") != -1);			
		}			
	}
	
	@Test
	public void testJSONSchema_additionalProperties1() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_additionalProperties.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple primitive properties
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":1,\"b\":2.2,\"c\":true,\"e\":\"aaa\"}");
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><a>1</a><b>2.2</b><c>true</c><e>aaa</e></root>"));
			//inner object
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":1,\"myObject\":{\"b\":2.2,\"c\":true,\"e\":\"aaa\"},\"f\":3}");
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><a>1</a><myObject><b>2.2</b><c>true</c><e>aaa</e></myObject><f>3</f></root>"));
			//array object
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,2,3,true,false,\"aa\",{\"b\":2.2,\"c\":true,\"e\":\"aaa\"}]}");
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><a>1</a><a>2</a><a>3</a><a>true</a><a>false</a><a>aa</a><a><b>2.2</b><c>true</c><e>aaa</e></a></root>"));
			//array object
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[2,3,4],5]}");
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><a>1</a><a><a>2</a><a>3</a><a>4</a></a><a>5</a></root>"));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":1,\"myObject\":{\"b\":2.2,\"c\":true,\"d\":xx,\"e\":\"aaa\"},\"f\":3}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.myObject.d") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":1,\"myObject\":{\"b\":2.2,\"c\":true,\"d\":111,\"e\":\"aaa\"},\"f\":xx}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.f") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[xx,3,4],5]}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.a[1][0]") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[2,3,{\"b\":1,\"c\":xxx}],5]}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.a[1][2].c") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,2,xx]}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.a[2]") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,2,{\"b\":1,\"c\":2}],\"d\":xx}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.d") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[2,3,4],xx]}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.a[2]") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[2,3,4],5],\"b\":xx}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $.b") != -1);			
		}			
		try {
			d = jsonSchemaForXML.mapJSONToXMLDocument("{\"a\":[1,[2,3,4],5],xx}");
			fail("invalid json should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON input") != -1);
			assertTrue(e.toString().indexOf("path: $") != -1);			
		}			

	
		try {
			xml = "<root><a>1</a></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\"}")));
			xml = "<root><a></a></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"\"}")));
			xml = "<root><a>1</a><b>2</b></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":\"2\"}")));
			xml = "<root><a>1</a><b><c>2</c><d>3</d></b></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":{\"c\":\"2\",\"d\":\"3\"}}")));
			xml = "<root><a>1</a><b>2</b><b>3</b></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":[\"2\",\"3\"]}")));
			xml = "<root b=\"2\"><a>1</a></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":\"2\"}")));
			xml = "<root b=\"2\"><a>1</a><b>3</b></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":[\"2\",\"3\"]}")));
			xml = "<root \n  \t b=\" \t  2 \t \n  \">  \n   \t <a>  \n  \t  1 \t  \n \t  </a>   \n   \t  \n  <b>3</b>   \n   \t  </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"a\":\"1\",\"b\":[\"2\",\"3\"]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			xml = "<root>1</root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);			
		}			
		try {
			xml = "<root> b <a>1</a></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);			
		}			
		try {
			xml = "<root>  <a>1</a>  b </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);			
		}			
		try {
			xml = "<root>  <a>1</b>   </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root/a") != -1);			
		}			
		try {
			xml = "<root>  <a>1<c>2   </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Mix XML elements and text is not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root/a/c") != -1);			
		}			
		try {
			xml = "<root>  <a>1<a> x <b>2</b>   </root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Mix XML elements and text is not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root/a") != -1);			
		}			
		try {
			xml = "<root><a><b><c>2</c><c>2</c></b><b><c>2</c><c>2</c></b></a><d><</d></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("invalid xml should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root/d") != -1);			
		}			
	}
	
	@Test
	public void testJSONSchema_null() {
		String schema = null;
		JSONObject o = null;
		Document d = null;
		String xml = null;
		String ostr = null;

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_null.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			//normal values
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"2\" str_attr=\"a\" str_attr_nullable=\"b\"><str>c</str><str_nullable>d</str_nullable><int_nullable>4</int_nullable><int>3</int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"b\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//all nullable int are empty
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"\" str_attr=\"a\" str_attr_nullable=\"b\"><str>c</str><str_nullable>d</str_nullable><int_nullable/><int>3</int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":null,\"str_attr\":\"a\",\"int_attr_nullable\":null,\"int\":3,\"str_attr_nullable\":\"b\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//all nullable str are empty
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"2\" str_attr=\"a\" str_attr_nullable=\"\"><str>c</str><str_nullable/><int_nullable>4</int_nullable><int>3</int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":\"\",\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//nullable strings map to empty strings
			ostr = "{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":null,\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":null}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"2\" str_attr=\"a\" str_attr_nullable=\"\"><str>c</str><str_nullable/><int_nullable>4</int_nullable><int>3</int></anObject></root>";
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//empty non nullable strings are ok
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"2\" str_attr=\"\" str_attr_nullable=\"b\"><str/><str_nullable>d</str_nullable><int_nullable>4</int_nullable><int>3</int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str\":\"\",\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":\"\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"b\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//additional property
			xml = "<root><anObject new_attr=\"abc\"><new_elem>def</new_elem></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"new_attr\":\"abc\",\"new_elem\":\"def\"}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, "<root><anObject><new_attr>abc</new_attr><new_elem>def</new_elem></anObject></root>"));			
			//additional property
			ostr = "{\"anObject\":{\"new_str\":\"\"}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			xml = "<root><anObject><new_str/></anObject></root>";
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array
			xml = "<root><anArrayOfNullableInt><anArrayOfNullableInt>1</anArrayOfNullableInt><anArrayOfNullableInt/><anArrayOfNullableInt>3</anArrayOfNullableInt></anArrayOfNullableInt></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anArrayOfNullableInt\":[1,null,3]}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
			//array 2
			xml = "<root><anObject2><anArrayOfInt>1</anArrayOfInt><anArrayOfInt>2</anArrayOfInt><anArrayOfNullableInt>3</anArrayOfNullableInt><anArrayOfNullableInt/><anArrayOfNullableInt>5</anArrayOfNullableInt></anObject2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject2\":{\"anArrayOfInt\":[1,2],\"anArrayOfNullableInt\":[3,null,5]}}")));
			d = jsonSchemaForXML.mapJSONToXMLDocument(o.toString());
			assertTrue(XMLComparator.areObjectsEqual(d, xml));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//non nullable attribute
			xml = "<root><anObject int_attr=\"\" int_attr_nullable=\"2\" str_attr=\"a\" str_attr_nullable=\"b\"><str>c</str><str_nullable>d</str_nullable><int_nullable>4</int_nullable><int>3</int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/anObject/@int_attr") != -1);			
		}
		
		try {
			//non nullable property
			xml = "<root><anObject int_attr=\"1\" int_attr_nullable=\"2\" str_attr=\"a\" str_attr_nullable=\"b\"><str>c</str><str_nullable>d</str_nullable><int_nullable>4</int_nullable><int></int></anObject></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/anObject/int") != -1);			
		}	

		try {
			//str null
			ostr = "{\"anObject\":{\"str\":null,\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"b\"}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.str") != -1);			
		}	
		try {
			//str_attr null
			ostr = "{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":null,\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"b\"}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.str_attr") != -1);			
		}	
		try {
			//int_attr null
			ostr = "{\"anObject\":{\"str\":\"c\",\"int_attr\":null,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":3,\"str_attr_nullable\":\"b\"}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.int_attr") != -1);			
		}	
		try {
			//int null
			ostr = "{\"anObject\":{\"str\":\"c\",\"int_attr\":1,\"str_nullable\":\"d\",\"int_nullable\":4,\"str_attr\":\"a\",\"int_attr_nullable\":2,\"int\":null,\"str_attr_nullable\":\"b\"}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("non nullable should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.int") != -1);			
		}	

		try {
			//additional new null property
			ostr = "{\"anObject\":{\"new_str\":null}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("additional new null property should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Additonal property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.new_str") != -1);			
		}	
		try {
			//additional new array property with null element
			ostr = "{\"anObject\":{\"new_array\":[1,null,3]}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("additional new null property should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Additonal property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject.new_array[1]") != -1);			
		}
		
		try {
			//non nullable array
			xml = "<root><anArrayOfInt><anArrayOfInt>1</anArrayOfInt><anArrayOfInt/><anArrayOfInt>3</anArrayOfInt></anArrayOfInt></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("non nullable array item should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/anArrayOfInt/anArrayOfInt[2]") != -1);			
		}
		try {
			//additional new null property
			ostr = "{\"anArrayOfInt\":[1,null,3]}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("additional new null property should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anArrayOfInt[1]") != -1);			
		}	

		try {
			//non nullable array
			xml = "<root><anObject2><anArrayOfInt>1</anArrayOfInt><anArrayOfInt></anArrayOfInt><anArrayOfNullableInt>3</anArrayOfNullableInt><anArrayOfNullableInt/><anArrayOfNullableInt>5</anArrayOfNullableInt></anObject2></root>";
			o = jsonSchemaForXML.mapXMLToJSONObject(xml);
			fail("non nullable array item should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: /root/anObject2/anArrayOfInt[2]") != -1);			
		}	
		try {
			//additional new null property
			ostr = "{\"anObject2\":{\"anArrayOfInt\":[1,2,null],\"anArrayOfNullableInt\":[3,null,5]}}";
			d = jsonSchemaForXML.mapJSONToXMLDocument(ostr);
			fail("additional new null property should throw exception");
		}
		catch(Exception e) {
			assertTrue(e.toString().indexOf("Property is not nullable") != -1);
			assertTrue(e.toString().indexOf("path: $.anObject2.anArrayOfInt[2]") != -1);			
		}	
	}
}
