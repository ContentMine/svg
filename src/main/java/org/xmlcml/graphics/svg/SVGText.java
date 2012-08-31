/**
 *    Copyright 2011 Peter Murray-Rust et. al.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xmlcml.graphics.svg;

import java.awt.Color;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.EuclidConstants;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;
import org.xmlcml.euclid.Vector2;

/** draws text.
 * 
 * NOTE: Text can be rotated and the additonal fields manage some of the
 * metrics for this. Still very experimental
 * 
 * @author pm286
 *
 */
public class SVGText extends SVGElement {
	private static Logger LOG = Logger.getLogger(SVGText.class);
	public final static String TAG ="text";
	
    public static String SUB0 = CMLConstants.S_UNDER+CMLConstants.S_LCURLY;
    public static String SUP0 = CMLConstants.S_CARET+CMLConstants.S_LCURLY;
    public static String SUB1 = CMLConstants.S_RCURLY+CMLConstants.S_UNDER;
    public static String SUP1 = CMLConstants.S_RCURLY+CMLConstants.S_CARET;
    
    public final static Double DEFAULT_FONT_WIDTH_FACTOR = 10.0;
    
	// these are all when text is used for concatenation, etc.
	private double estimatedHorizontallength = Double.NaN; 
	private double currentFontSize = Double.NaN;
	private double currentBaseY = Double.NaN;
	private String rotate = null;
	private double calculatedTextEndCoordinate = Double.NaN;
	
	/** constructor
	 */
	public SVGText() {
		super(TAG);
		init();
	}
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	private void clearRotate() {
		estimatedHorizontallength = Double.NaN; 
		currentBaseY = Double.NaN;
		calculatedTextEndCoordinate = Double.NaN;
		this.setBoundingBoxCached(false);
	}

	public static void setDefaultStyle(SVGText text) {
		text.setStroke("none");
		text.setFontSize(1.0);
	}
	
	/** constructor
	 */
	public SVGText(SVGText element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGText(Element element) {
        super((SVGElement) element);
	}
	
	protected SVGText(String tag) {
		super(tag);
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGText(this);
    }

    public double getX() {
    	String s = this.getAttributeValue("x");
    	return (s != null) ? new Double(s).doubleValue()  : 0.0;
    }

    public double getY() {
    	String s = this.getAttributeValue("y");
    	return (s != null) ? new Double(s).doubleValue() : 0.0;
    }
    
	protected void drawElement(Graphics2D g2d) {
		double fontSize = this.getFontSize();
		fontSize *= cumulativeTransform.getMatrixAsArray()[0] * 0.3;
		fontSize = (fontSize < 8) ? 8 : fontSize;
		String text = this.getText();
		Real2 xy = this.getXY();
		xy = transform(xy, cumulativeTransform);
		xy.plusEquals(new Real2(fontSize*0.65, -0.65*fontSize));
		Color color = this.getColor("fill");
		color = (color == null) ? Color.DARK_GRAY : color;
		g2d.setColor(color);
		g2d.setFont(new Font("SansSerif", Font.PLAIN, (int)fontSize));
		g2d.drawString(text, (int)xy.x, (int)xy.y);
	}
	
	
	/** constructor.
	 * 
	 * @param xy
	 * @param text
	 */
	public SVGText(Real2 xy, String text) {
		this();
		setXY(xy);
		setText(text);
	}

	public void applyTransform(Transform2 t2) {
		//assume scale and translation only
		Real2 xy = getXY();
		xy.transformBy(t2);
		this.setXY(xy);
		transformFontSize(t2);
		//rotate text? // not tested
		Angle angle = t2.getAngleOfRotation();
		if (!angle.isEqualTo(0.0, EPS)) {
			Transform2 t = Transform2.getRotationAboutPoint(angle, xy);
			this.setTransform(t);
			LOG.trace("text: "+this.toXML());
		}
	}

	/** result is always positive
	 * 
	 * @param t2
	 */
	public void transformFontSize(Transform2 t2) {
		Double fontSize = this.getFontSize();
		// transform fontSize
		if (fontSize != null) {
			Real2 ff = new Real2(fontSize, 1.0);
			ff.transformBy(t2);
//			double size = ff.getX(); // old
			double size = Math.max(ff.getX(), ff.getY()); // takes account of rotation
			LOG.trace("FS "+ff+" .. "+size);
			this.setFontSize(size);
		}
	}



    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
    	setXY(getXY().format(places));
    	Double fontSize = this.getFontSize();
    	if (fontSize != null) {
    		fontSize = Util.format(fontSize, places);
    		this.setFontSize(fontSize);
    	}
    }

	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		Nodes nodes = this.query("./text()");
		return nodes.size() == 1 ? nodes.get(0).getValue() : null;
	}

	/**
	 * clears text and replaces if not null
	 * @param text the text to set
	 */
	public void setText(String text) {
		if (this.getChildCount() > 0) {
			Node node = this.getChild(0);
			if (node instanceof Text) {
				node.detach();
			} else if (node instanceof SVGTSpan) {
				// expected child
			} else {
				LOG.debug("unexpected child of SVGText: "+node.getClass());
			}
		}
		if (text != null) {
			this.appendChild(text);
		}
		boundingBox = null;
		calculatedTextEndCoordinate = Double.NaN;
		estimatedHorizontallength = Double.NaN; 
	}

	/** extent of text
	 * defined as the point origin (i.e. does not include font)
	 * @return
	 */
	public Real2Range getBoundingBoxForCenterOrigin() {
		
//		double fontWidthFactor = DEFAULT_FONT_WIDTH_FACTOR;
//		double fontWidthFactor = 1.0;
		// seems to work??
		double fontWidthFactor = 0.3;
		double halfWidth = getEstimatedHorizontalLength(fontWidthFactor) / 2.0;
		
		double height = this.getFontSize();
		Real2Range boundingBox = new Real2Range();
		Real2 center = getXY();
		boundingBox.add(center.plus(new Real2(halfWidth, 0.0)));
		boundingBox.add(center.plus(new Real2(-halfWidth, height)));
		return boundingBox;
	}

	/** extent of text
	 * defined as the point origin (i.e. does not include font)
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			double fontWidthFactor = 1.0;
			double width = getEstimatedHorizontalLength(fontWidthFactor);
			double height = this.getFontSize() * fontWidthFactor;
			Real2 xy = this.getXY();
			boundingBox = new Real2Range(xy, xy.plus(new Real2(width, -height)));
			
			rotateBoundingBoxForRotatedText();
		}
		return boundingBox;
	}
	
	private void rotateBoundingBoxForRotatedText() {
		Transform2 t2 = this.getTransform();
		if (t2 != null) {
			Angle rotation = t2.getAngleOfRotation();
			// significant rotation?
			if (!rotation.isEqualTo(0., 0.001)) {
				Real2[] corners = boundingBox.getCorners();
				corners[0].transformBy(t2);
				corners[1].transformBy(t2);
				boundingBox = new Real2Range(corners[0], corners[1]);
			}
		}
	}

	/** this is a hack and depends on what information is available
	 * include fontSize and factor
	 * @param fontWidthFactor
	 * @return
	 */
	public Double getEstimatedHorizontalLength(double fontWidthFactor) {
		String s = getText();
		if (s == null) {
			estimatedHorizontallength = Double.NaN;
		} else {
			String family = this.getFontFamily();
			double[] lengths = FontWidths.getFontWidths(family);
			if (lengths == null) {
				lengths = FontWidths.SANS_SERIF;
			}
			double fontSize = this.getFontSize();
			estimatedHorizontallength = 0.0;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c > 255) {
					c = 's';  // as good as any
				}
				double length = fontSize * fontWidthFactor * lengths[(int)c];
				estimatedHorizontallength += length;
			}
		}
		return estimatedHorizontallength;
	}
	
	public Real2 getCalculatedTextEnd(double fontWidthFactor) {
		getRotate();
		Real2 xyEnd = null;
		getEstimatedHorizontalLength(fontWidthFactor);
		if (!Double.isNaN(estimatedHorizontallength)) {
			if (rotate == null) {
				xyEnd = this.getXY().plus(new Real2(estimatedHorizontallength, 0.0));
			} else if (rotate.equals(SVGElement.YPLUS)) {
				xyEnd = this.getXY().plus(new Real2(0.0, -estimatedHorizontallength));
			} else if (rotate.equals(SVGElement.YMINUS)) {
				xyEnd = this.getXY().plus(new Real2(0.0, estimatedHorizontallength));
			}
		}
		return xyEnd;
	}
	
	public double getCalculatedTextEndCoordinate(double fontWidthFactor) {
		if (Double.isNaN(calculatedTextEndCoordinate)) {
			getRotate();
			Real2 xyEnd = getCalculatedTextEnd(fontWidthFactor);
			if (xyEnd != null) {
				if (rotate == null) {
					calculatedTextEndCoordinate = xyEnd.getX();
				} else if (rotate.equals(YMINUS)){
					calculatedTextEndCoordinate = xyEnd.getY();
				} else if (rotate.equals(YPLUS)){
					calculatedTextEndCoordinate = xyEnd.getY();
				} else {
					calculatedTextEndCoordinate = xyEnd.getY();
				}
			}
		}
		return calculatedTextEndCoordinate;
	}
	
	public void setCalculatedTextEndCoordinate(double coord) {
		this.calculatedTextEndCoordinate = coord;
	}
	
	public double getCurrentFontSize() {
		if (Double.isNaN(currentFontSize)) {
			currentFontSize = this.getFontSize();
		}
		return currentFontSize;
	}
	public void setCurrentFontSize(double currentFontSize) {
		this.currentFontSize = currentFontSize;
	}
	
	public double getCurrentBaseY() {
		getRotate();
		if (Double.isNaN(currentBaseY)) {
			currentBaseY = (rotate == null) ? this.getY() : this.getX();
		}
		return currentBaseY;
	}
	public void setCurrentBaseY(double currentBaseY) {
		this.currentBaseY = currentBaseY;
	}
	
	public String getRotate() {
		if (rotate == null) {
			rotate = getAttributeValue(SVGElement.ROTATE);
		}
		return rotate;
	}
	
	public void setRotate(String rotate) {
		this.rotate = rotate;
		clearRotate();
	}
	
	/**
	 * tries to concatenate text1 onto this. If success (true) alters this,
	 * else leaves this unaltered
	 * 
	 * @param fontWidthFactor
	 * @param fontHeightFactor
	 * @param text1 left text
	 * @param subVert fraction of large font size to determine subscript
	 * @param supVert fraction of large font size to determine superscript
	 * @return null if concatenated
	 */
	public boolean concatenateText(double fontWidthFactor, double fontHeightFactor, 
			SVGText text1, double subVert, double supVert, double eps) {

		String rotate0 = this.getAttributeValue(SVGElement.ROTATE);
		String rotate1 = text1.getAttributeValue(SVGElement.ROTATE);
		// only compare text in same orientation
		boolean rotated = false;
		if (rotate0 == null) {
			rotated = (rotate1 != null);
		} else {
			rotated = (rotate1 == null || !rotate0.equals(rotate1));
		}
		if (rotated) {
			LOG.debug("text orientation changed");
			return false;
		}
		String newText = null;
		String string0 = this.getText();
		double fontSize0 = this.getCurrentFontSize();
		Real2 xy0 = this.getXY();
		String string1 = text1.getText();
		double fontSize1 = text1.getFontSize();
		Real2 xy1 = text1.getXY();
		double fontRatio0to1 = fontSize0 / fontSize1;
		double fontWidth = fontSize0 * fontWidthFactor;
		double fontHeight = fontSize0 * fontHeightFactor;
		// TODO update for different orientation
		double coordHoriz0 = (rotate0 == null) ? xy0.getX() : xy0.getY();
		double coordHoriz1 = (rotate1 == null) ? xy1.getX() : xy1.getY();
//		double coordHoriz1 = xy1.getX();
//		double coordVert0 = xy0.getY();
		double coordVert0 = this.getCurrentBaseY();
		double coordVert1 = (rotate1 == null) ? xy1.getY() : xy1.getX();
		double deltaVert = coordVert0 - coordVert1;
		double maxFontSize = Math.max(fontSize0, fontSize1);
		double unscriptFontSize = Double.NaN;
		String linker = null;
		// anticlockwise Y rotation changes order
		double sign = (YPLUS.equals(rotate)) ? -1.0 : 1.0;
		double[] fontWidths = FontWidths.getFontWidths(this.getFontFamily());
		double spaceWidth = fontWidths[(int)C_SPACE] * maxFontSize * fontWidthFactor;
		
		// same size of font?
		LOG.debug(""+this.getText()+"]["+text1.getText()+ " ...fonts... " + fontSize0+"/"+fontSize1);
		// has vertical changed by more than the larger font size?
		if (!Real.isEqual(coordVert0, coordVert1, maxFontSize * fontHeightFactor)) {
			LOG.debug("changed vertical height "+coordVert0+" => "+coordVert1+" ... "+maxFontSize);
			LOG.trace("COORDS "+xy0+"..."+xy1);
//			text0.debug("T0");
//			text1.debug("T1");
			LOG.trace("BASEY "+this.getCurrentBaseY()+"..."+text1.getCurrentBaseY());
		} else if (fontRatio0to1 > 0.95 && fontRatio0to1 < 1.05) {
			// no change of size
			if (Real.isEqual(coordVert0, coordVert1, eps)) {
				// still on same line?
				// allow a space
				double gapXX = (coordHoriz1 - coordHoriz0) * sign;
				double calcEnd = this.getCalculatedTextEndCoordinate(fontWidthFactor);
				double gapX = (coordHoriz1 - calcEnd) *sign;
				double nspaces = (gapX / spaceWidth);
				if (gapXX < 0) {
					LOG.debug("text R to L ... "+gapXX);
					// in front of preceding (axes sometime go backwards
					linker = null;
				} else if (nspaces < 0.5) {
					nspaces = 0;
				} else if (nspaces > 2) {
					nspaces = 100;
				} else {
					nspaces = 1;
				}
				linker = null;
				if (nspaces == 0) {
					linker = CMLConstants.S_EMPTY;
				} else if (nspaces == 1) {
					linker = CMLConstants.S_SPACE;
				}
			} else {
				LOG.debug("slight vertical change: "+coordVert0+" => "+coordVert1);
			}
		} else if (fontRatio0to1 > 1.05) {
			// coords down the page?
			LOG.debug("Trying sscript "+deltaVert);
			// sub/superScript
			if (deltaVert > 0 && Real.isEqual(deltaVert, subVert * fontHeight, maxFontSize)) {
				// start of subscript?
				linker = SUB0;
				LOG.debug("INSUB");
				// save font as larger size
				this.setFontSize(text1.getFontSize());
			} else if (deltaVert < 0 && Real.isEqual(deltaVert, supVert * fontHeight, maxFontSize)) {
				// start of superscript?
				linker = SUP0;
				LOG.debug("INSUP");
				// save font as larger size
				this.setFontSize(text1.getFontSize());
			} else {
				LOG.debug("ignored font change");
			}
		} else if (fontRatio0to1 < 0.95) {
			LOG.debug("Trying unscript "+deltaVert);
			// end of sub/superScript
			if (deltaVert > 0 && Real.isEqual(deltaVert, -supVert * fontHeight, maxFontSize)) {
				// end of superscript?
				linker = SUP1;
				LOG.debug("OUTSUP");
			} else if (deltaVert < 0 && Real.isEqual(deltaVert, -subVert * fontHeight, maxFontSize)) {
				// end of subscript?
				linker = SUB1;
				LOG.debug("OUTSUB");
			} else {
				LOG.debug("ignored font change");
			}
			if (newText != null) {
				this.setCurrentBaseY(text1.getCurrentBaseY());
			}
			unscriptFontSize = text1.getFontSize();
		} else {
			LOG.debug("change of font size: "+fontSize0+"/"+fontSize1+" .... "+this.getText()+" ... "+text1.getText());
		}
		if (linker != null) {
			newText = string0 + linker + string1;
			this.setText(newText);
			this.setCurrentFontSize(text1.getFontSize());
			// preserve best estimate of text length
			this.setCalculatedTextEndCoordinate(text1.getCalculatedTextEndCoordinate(fontWidthFactor));
			if (!Double.isNaN(unscriptFontSize)) {
				this.setFontSize(unscriptFontSize);
				this.setCurrentFontSize(unscriptFontSize);
				LOG.debug("setting font to "+unscriptFontSize);
			}
			LOG.debug("merged => "+newText);
		}
		LOG.debug("new...."+newText);
		return (newText != null);
	}
	
	public SVGRect getBoundingSVGRect() {
		Real2Range r2r = getBoundingBox();
		SVGRect rect = new SVGRect();
		rect.setBounds(r2r);
		return rect;
	}
	
	/** property of graphic bounding box
	 * can be overridden
	 * @return default none
	 */
	protected String getBBFill() {
		return "none";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default magenta
	 */
	protected String getBBStroke() {
		return "magenta";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.5
	 */
	protected double getBBStrokeWidth() {
		return 0.2;
	}
	
	public void createWordWrappedTSpans(Double textWidthFactor, Real2Range boundingBox, Double fSize) {
		String textS = getText().trim();
		if (textS.length() == 0) {
			return;
		}
		this.setText(null);
		double fontSize = fSize == null ? this.getFontSize() : fSize;
		String[] tokens = textS.split(EuclidConstants.S_WHITEREGEX);
		Double x0 = boundingBox.getXRange().getMin();
		Double x1 = boundingBox.getXRange().getMax();
		Double x = x0;
		Double y0 = boundingBox.getYRange().getMin();
		Double y = y0;
		Double deltay = fontSize*1.2;
		y += deltay;
		SVGTSpan span = this.createSpan(tokens[0], new Real2(x0, y), fontSize);
		int ntok = 1;
		while (ntok < tokens.length) { 
			String s = span.getText();
			span.setText(s+" "+tokens[ntok]);
			double xx = span.getCalculatedTextEndCoordinate(textWidthFactor);
			if (xx > x1) {
				span.setText(s);
				y += deltay;
				span = this.createSpan(tokens[ntok], new Real2(x0, y), fontSize);
			}
			ntok++;
		}
		this.clearRotate();
	}
	
	public SVGTSpan createSpan(String text, Real2 xy, Double fontSize) {
		SVGTSpan span = new SVGTSpan();
		span.setXY(xy);
		span.setFontSize(fontSize);
		span.setText(text);
		appendChild(span);
		return span;
	}

	/** makes a new list composed of the texts in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGText> extractTexts(List<SVGElement> elements) {
		List<SVGText> textList = new ArrayList<SVGText>();
		for (SVGElement element : elements) {
			if (element instanceof SVGText) {
				textList.add((SVGText) element);
			}
		}
		return textList;
	}
	/** special routine to make sure characters are correctly oriented
	 * 
	 */
	public void setTransformToRotateAboutTextOrigin() {
		Transform2 transform2 = this.getTransform();
		RealSquareMatrix rotMat = transform2.getRotationMatrix();
		setTransformToRotateAboutTextOrigin(rotMat);
	}

	
	public void setTransformToRotateAboutTextOrigin(RealSquareMatrix rotMat) {
		Real2 xy = new Real2(this.getXY());
		Transform2 newTransform2 = new Transform2(new Vector2(xy)); 
		newTransform2 = newTransform2.concatenate(new Transform2(rotMat));
		newTransform2 = newTransform2.concatenate(new Transform2(new Vector2(xy.multiplyBy(-1.0))));
		this.setTransform(newTransform2);
	}
}
