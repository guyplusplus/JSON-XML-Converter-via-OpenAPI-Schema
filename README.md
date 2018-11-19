# JSON-XML Converter via OpenAPI Schema 
This library converts JSON to and from XML based on the JSON schema included in the swagger / OpenAPI 3 document.
It is a JAVA based library.

The performance of the convertion is very good as both XML-to-JSON and JSON-to-XML is using streamed based parser. The target object is though created in memory before being converted into a string.

# Sample Code

```java
JSONSchemaForXML jsonSchemaForXML = new JSONSchemaForXML("{...OpenAPI flavored json schema...}");
String xmlOutput = jsonSchemaForXML.mapJSONToXMLString("{...json object...}", true); //true means formatted
String jsonOutput = jsonSchemaForXML.mapXMLToJSONString("<root>...xml document...</root>", true); //true means formatted
```

# Basic Example

Sample OpenAPI flavored JSON Schema

```json
{
  "$id": "https://example.com/person.schema.json",
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Very Simple Object",
  "type": "object",
  "properties": {
    "aString": {
      "type": "string",
      "xml": {
        "name": "xml_string"
      }
    },
    "aNumber": {
      "type": "number"
    }
  }
}
```

Sample Input XML (and output XML of sample data bellow)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
    <xml_string>Hello !!</xml_string>
    <aNumber>12.34</aNumber>
</root>
```

Sample Input JSON (and output JSON of sample data above)
```json
{
  "aNumber": 12.34,
  "aString": "Hello !!"
}
```

# Advanced Example

Sample OpenAPI flavored JSON Schema

```json
{
  "$id": "https://example.com/person.schema.json",
  "$schema": "http://json-schema.org/draft-07/schema#",
  "definitions": {
    "person": {
      "type": "object",
      "properties": {
        "uid":             { "$ref": "#/definitions/nationalId" },
        "fullname":        { "$ref": "#/definitions/fullname" },
        "children": {
          "type": "array",
          "items":         { "$ref": "#/definitions/person" },
          "xml": {
            "wrapped": true
          }
        }
      },
      "xml": {
        "name": "person"
      }
    },
    "nationalId": {
      "type": "integer",
      "xml": {
        "attribute": true
      }
    },
    "fullname": {
      "type": "string",
      "xml": {
        "prefix": "global",
        "namespace": "https://test.com/global"
      }
    }
  },
  
  "$ref": "#/definitions/person"
}
```

Sample Input XML (and output XML of sample data bellow)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<person uid="0">
    <global:fullname xmlns:global="https://test.com/global">John Brooks</global:fullname>
    <children>
        <person uid="1">
            <global:fullname xmlns:global="https://test.com/global">Paul Brooks</global:fullname>
        </person>
        <person uid="2">
            <global:fullname xmlns:global="https://test.com/global">Helen Brooks</global:fullname>
            <dob>19651231</dob>
            <children>
                <person uid="5">
                    <global:fullname xmlns:global="https://test.com/global">Lilly Alderberg</global:fullname>
                </person>
            </children>
        </person>
    </children>
</person>
```

Sample Input JSON (and output JSON of sample data above)
```json
{
  "uid": 0,
  "children": [
    {
      "uid": 1,
      "fullname": "Paul Brooks"
    },
    {
      "uid": 2,
      "children": [{
        "uid": 5,
        "fullname": "Lilly Alderberg"
      }],
      "dob": "19651231",
      "fullname": "Helen Brooks"
    }
  ],
  "fullname": "John Brooks"
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
