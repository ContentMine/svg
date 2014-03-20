package org.xmlcml.graphics.svg;

import junit.framework.Assert;
import nu.xom.Attribute;
import org.junit.Test;
import org.xmlcml.cml.testutil.JumboTestUtils;
import org.xmlcml.euclid.Real2;

public class GraphicsElementTest {

	@Test
	public void testUseStyleAttribute() {
		SVGCircle circle = new SVGCircle(new Real2(10., 20.), 3.);
		circle.setStroke("red");
		circle.setStrokeWidth(1.0);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' fill='#aaffff' stroke='red' stroke-width='1.0'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
	}

	@Test
	public void testUseStyleAttribute2() {
		SVGCircle circle = new SVGCircle(new Real2(10., 20.), 3.);
		// not default attributes (maybe a bad idea, but...)
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' "+ 
				" xmlns='http://www.w3.org/2000/svg' stroke='black' stroke-width='0.5' fill='#aaffff'/>", circle, true, 0.001);
		circle.setUseStyleAttribute(true);
		circle.setStroke("red");
		circle.setStrokeWidth(1.0);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' style=' fill : #aaffff; stroke : red; stroke-width : 1.0;'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
	}
	

	@Test
	public void testUseStyleAttribute3() {
		SVGCircle circle = new SVGCircle(new Real2(10., 20.), 3.);
		circle.setUseStyleAttribute(true);
		circle.setStroke("red");
		circle.setStrokeWidth(1.0);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' style=' fill : #aaffff; stroke : red; stroke-width : 1.0;'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
		circle.setUseStyleAttribute(false);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' fill='#aaffff' stroke='red' stroke-width='1.0'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
	}
	

	@Test
	public void testUseStyleAttribute4() {
		SVGCircle circle = new SVGCircle(new Real2(10., 20.), 3.);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' "+ 
				" xmlns='http://www.w3.org/2000/svg'  stroke='black' stroke-width='0.5' fill='#aaffff'/>", circle, true, 0.001);
		circle.setUseStyleAttribute(true);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' style=' fill : #aaffff; stroke : red; stroke-width : 1.0;'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
		circle.setStroke(null);
		circle.setStrokeWidth(null);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' style=' fill : #aaffff;'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
		circle.setUseStyleAttribute(false);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' fill='#aaffff'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
		circle.setStroke("red");
		circle.setStrokeWidth(1.0);
		JumboTestUtils.assertEqualsIncludingFloat("style",
				"<circle cx='10.0' cy='20.0' r='3.0' stroke='red' stroke-width='1.0' fill='#aaffff'"+ 
				" xmlns='http://www.w3.org/2000/svg'/>", circle, true, 0.001);
	}
	

	@Test
	public void testUseStyleAttribute5() {
		SVGCircle circle = new SVGCircle(new Real2(10., 20.), 3.);
		circle.addAttribute(new Attribute("style", "line-cap : smooth;"));
		Assert.assertEquals("fill", "#aaffff", circle.getFill());
		Assert.assertEquals("stroke", "black", circle.getStroke());
		Assert.assertEquals("bundle", "black",  circle.getAttributeValue(StyleBundle.STROKE));
		Assert.assertEquals("width", 0.5, circle.getStrokeWidth(), 0.001);
		Assert.assertEquals("style", "line-cap : smooth;", circle.getStyle());
		circle.setUseStyleAttribute(true);
		Assert.assertNull("bundle",  circle.getAttributeValue(StyleBundle.STROKE));
		Assert.assertEquals("bundle",  "black", circle.getStroke());
		Assert.assertEquals("style", " fill : #aaffff; stroke : black; stroke-width : 0.5; line-cap : smooth;", circle.getStyle());
		circle.setUseStyleAttribute(false);
		circle.setStroke("red");
		circle.setStrokeWidth(3.0);
		circle.setOpacity(0.2);
		circle.setFill(null);
		Assert.assertNull("fill", circle.getFill());
		Assert.assertEquals("stroke", "red", circle.getStroke());
		Assert.assertEquals("stroke", "red",  circle.getAttributeValue(StyleBundle.STROKE));
		Assert.assertEquals("opacity", 0.2, circle.getOpacity(), 0.001);
		Assert.assertEquals("opacity", "0.2",  circle.getAttributeValue(StyleBundle.OPACITY));
		Assert.assertEquals("width", 3.0, circle.getStrokeWidth(), 0.001);
		Assert.assertEquals("style", " line-cap : smooth;", circle.getStyle());
		circle.setUseStyleAttribute(true);
		Assert.assertNull("bundle",  circle.getAttributeValue(StyleBundle.STROKE));
		Assert.assertEquals("bundle",  "red", circle.getStroke());
		Assert.assertEquals("style", " stroke : red; stroke-width : 3.0; opacity : 0.2; line-cap : smooth;", circle.getStyle());
	}
}
