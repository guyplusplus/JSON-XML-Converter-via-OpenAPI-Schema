package com.geckotechnology.xmljsonconvert.core.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.geckotechnology.xmljsonconvert.core.SimplePath;

public class SimplePathTest {

	@Test
	public void testSimplePathXML() {
		SimplePath sp = new SimplePath(SimplePath.XML_PATH);
		//test empty path
		assertEquals(sp.toString(), "/");
		//test single push
		sp.pushElement("e1");
		assertEquals(sp.toString(), "/e1");
		//test multi push for all types
		sp.pushElement("e2").pushIndex(10).pushElement("e3").pushXMLAttribute("attr1");
		assertEquals(sp.toString(), "/e1/e2[10]/e3/@attr1");
		//test single pop
		sp.pop();
		assertEquals(sp.toString(), "/e1/e2[10]/e3");
		//test multi pop
		sp.pop().pop();
		assertEquals(sp.toString(), "/e1/e2");
		//test pop until stack empty
		sp.pop().pop();
		assertEquals(sp.toString(), "/");
	}
	
	@Test
	public void testSimplePathJSON() {
		SimplePath sp = new SimplePath(SimplePath.JSON_PATH);
		//test empty path
		assertEquals(sp.toString(), "$");
		//test single push
		sp.pushElement("e1");
		assertEquals(sp.toString(), "$.e1");
		//test multi push for all types
		sp.pushElement("e2").pushIndex(10).pushElement("e3").pushElement("e4");
		assertEquals(sp.toString(), "$.e1.e2[10].e3.e4");
		//test single pop
		sp.pop();
		assertEquals(sp.toString(), "$.e1.e2[10].e3");
		//test multi pop
		sp.pop().pop();
		assertEquals(sp.toString(), "$.e1.e2");
		//test pop until stack empty
		sp.pop().pop();
		assertEquals(sp.toString(), "$");
	}
	
	@Test
	public void testSimplePathClone() {
		SimplePath sp1 = new SimplePath(SimplePath.XML_PATH);
		sp1.pushElement("e1").pushElement("e2").pushIndex(10).pushElement("e3").pushXMLAttribute("attr1");
		assertEquals(sp1.toString(), "/e1/e2[10]/e3/@attr1");
		SimplePath sp2 = sp1.clone();
		assertEquals(sp2.toString(), "/e1/e2[10]/e3/@attr1");
		sp1.pop();
		assertEquals(sp1.toString(), "/e1/e2[10]/e3");
		assertEquals(sp2.toString(), "/e1/e2[10]/e3/@attr1");
		sp2.pushElement("e4");
		assertEquals(sp1.toString(), "/e1/e2[10]/e3");
		assertEquals(sp2.toString(), "/e1/e2[10]/e3/@attr1/e4");
	}
}
