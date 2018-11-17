# JSON-XML Converter via OpenAPI Schema 
This library converts JSON to and from XML based on the JSON schema included in the swagger / OpenAPI 3 document.
It is a JAVA based library.

# Sample Code

```java
JSONSchemaForXML jsonSchemaForXML = new JSONSchemaForXML("{...json schema...}");
String xmlOutput = jsonSchemaForXML.mapJSONToXMLString("{...json object...}", true);
String jsonOutput = jsonSchemaForXML.mapXMLToJSONString("<root>...xml document...</root>", true);
```

# Basic Example

Sample Input Schema

```json
{
  "$id": "https://example.com/person.schema.json",
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Very Simple Object",
  "type": "object",
  "properties": {
    "aString": {
      "type": "string"
    }
  }
}
```

Sample Input XML (and output XML of sample data bellow)
```xml
<root>
    <aString>Hello !!</aString>
</root>
```

Sample Input JSON (and outout JSON of same data above)
```json
{
  "aString": "Hello !!"
}
```

# Install from Maven Central
Add a dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.geckotechnology.xmljsonconvert</groupId>
    <artifactId>xmljsonconvert</artifactId>
    <version>0.9.1</version>
</dependency>
```

# Release History
|Version|Date|Details|
|---|---|---|
|0.8.0|28-Oct-18|Support for XML to JSON|
|0.8.1|4-Nov-18|Syntax highlighting<br>optimize code for xpath (used when raising exception)|
|0.9.0|8-Nov-18|JSON to XML implemented|
|0.9.1|17-Nov-18|Code cleanup and more test cases|

# TODO (by order of priority):
* ability to control for XML to JSON
  * if an empty XML non wrapped array (hence no XML element) creates an empty JSON array
  * space trim for string content (text node) and XML attributes (quote delimited)
  * for additional properties
    * if number, boolean and null are mapped to their type rather than a string
* Support for const without type
* additionalProperties support:
  * with schema
* Support schema oneOf, anyOf, allOf, not
* support for $ref
  * a definitions in schema is just a proxy to another schema in definitions
  * support relative URI
  * external $ref
