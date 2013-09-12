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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGLine extends SVGShape {

	private static final String STYLE = "style";
	private static final String X1 = "x1";
	private static final String X2 = "x2";
	private static final String Y1 = "y1";
	private static final String Y2 = "y2";
	private static final String X = "x";
	private static final String Y = "y";

	public final static String TAG ="line";

	private Line2D.Double line2;
	private Line2 euclidLine;
	
	/** constructor
	 */
	public SVGLine() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGLine(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGLine(Element element) {
        super((SVGElement) element);
	}
	
	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGLine(Real2 x1, Real2 x2) {
		this();
		setXY(x1, 0);
		setXY(x2, 1);
		euclidLine = new Line2(x1, x2);
	}
	
	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGLine(Line2 line) {
		this(line.getXY(0), line.getXY(1));
		this.euclidLine = line;
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	public static void setDefaultStyle(SVGElement line) {
		line.setStroke("black");
		line.setStrokeWidth(1.0);
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGLine(this);
    }

	public Real2 getXY() {
		throw new RuntimeException("Cannot define getXY() for lines");
	}
	
	public double getX() {
		throw new RuntimeException("Cannot define getY() for lines");
	}
	
	public double getY() {
		throw new RuntimeException("Cannot define getY() for lines");
	}
	
	/**
	 * @param xy coordinates of the atom
	 * @param serial 0 or 1
	 */
	public void setXY(Real2 x12, int serial) {
		if (x12 == null) {
			System.err.println("null x2/y2 in line: ");
		} else {
			this.addAttribute(new Attribute(X+(serial+1), String.valueOf(x12.getX())));
			this.addAttribute(new Attribute(Y+(serial+1), String.valueOf(x12.getY())));
		}
	}
	
	public Real2 getXY(int serial) {
		Real2 xy = null;
		if (serial == 0) {
			xy = new Real2(this.getDouble(X1), this.getDouble(Y1));
		} else if (serial == 1) {
			xy = new Real2(this.getDouble(X2), this.getDouble(Y2));
		}
		return xy;
	}
	
	/**
	 * @param x12 coordinates of the atom
	 * @param serial 1 or 2
	 */
	@Deprecated
	public void setX12(Real2 x12, int serial) {
		if (x12 == null) {
			System.err.println("null x2/y2 in line: ");
		} else {
			this.addAttribute(new Attribute(X+serial, String.valueOf(x12.getX())));
			this.addAttribute(new Attribute(Y+serial, String.valueOf(x12.getY())));
		}
	}
	
	@Deprecated //use getXY
	public Real2 getX12(int serial) {
		Real2 xy = null;
		if (serial == 1) {
			xy = new Real2(this.getDouble(X1), this.getDouble(Y1));
		} else if (serial == 2) {
			xy = new Real2(this.getDouble(X2), this.getDouble(Y2));
		}
		return xy;
	}
	
//  <g style="stroke-width:0.2;">
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
//</g>
	
	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		ensureCumulativeTransform();
		Line2D line = createAndSetLine2D();
		fill(g2d, line);
		draw(g2d, line);
		restoreGraphicsSettingsAndTransform(g2d);
	}

	public void applyAttributes(Graphics2D g2d) {
		if (g2d != null) {
			double width = (double) this.getStrokeWidth();
			Stroke s = new BasicStroke((float)width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(s);
			super.applyAttributes(g2d);
		}
	}

	public Line2D.Double createAndSetLine2D() {
		ensureCumulativeTransform();
		double x1 = this.getDouble(X1);
		double y1 = this.getDouble(Y1);
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		double x2 = this.getDouble(X2);
		double y2 = this.getDouble(Y2);
		Real2 xy2 = new Real2(x2, y2);
		xy2 = transform(xy2, cumulativeTransform);
		float width = 5.0f;
		String style = this.getAttributeValue(STYLE);
		if (style != null && style.startsWith("stroke-width:")) {
			style = style.substring("stroke-width:".length());
			style = style.substring(0, (style+S_SEMICOLON).indexOf(S_SEMICOLON));
			width = (float) new Double(style).doubleValue();
			width *= 15.f;
		}
		line2 = new Line2D.Double(xy1.x, xy1.y, xy2.x, xy2.y);
		return line2;
	}
	
	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public Line2D.Double getLine2() {
		return line2;
	}

	public void setLine2(Line2D.Double line2) {
		this.line2 = line2;
	}
	
	public Line2 getEuclidLine() {
		if (euclidLine == null) {
			euclidLine = new Line2(this.getXY(0), this.getXY(1));
		}
		return euclidLine;
	}

	public void setEuclidLine(Line2 euclidLine) {
		this.euclidLine = euclidLine;
	}

	public void applyTransform(Transform2 transform) {
		Real2 xy = this.getXY(0);
		setXY(xy.getTransformed(transform), 0);
		xy = this.getXY(1);
		setXY(xy.getTransformed(transform), 1);
	}

	public void format(int places) {
		setXY(getXY(0).format(places), 0);
		setXY(getXY(1).format(places), 1);
	}

	public boolean connectsPoints(Real2 p0, Real2 p1, double eps) {
		return (this.getXY(0).isEqualTo(p0, eps) && this.getXY(1).isEqualTo(p1, eps)) || 
			(this.getXY(0).isEqualTo(p1, eps) && this.getXY(1).isEqualTo(p0, eps)); 
	}
	
	public boolean isVertical(double eps) {
		return Real.isEqual(this.getXY(0).getX(), this.getXY(1).getX(), eps);
	}
	
	public boolean isHorizontal(double eps) {
		return Real.isEqual(this.getXY(0).getY(), this.getXY(1).getY(), eps);
	}

	public boolean isZero(double eps) {
		Real2Range bbox = this.getBoundingBox();
		return bbox.getXRange().getRange() < eps && bbox.getYRange().getRange() < eps;
	}

	/** do lines join at ends?
	 * 
	 * @param l
	 * @param eps
	 * @return
	 */
	public Real2 getCommonEndPoint(SVGLine l, double eps) {
		Real2 point = null;
		if (l.getXY(0).isEqualTo(getXY(0), eps) ||  l.getXY(1).isEqualTo(getXY(0), eps)) {
			point = getXY(0);
		} else if (l.getXY(0).isEqualTo(getXY(1), eps) ||  l.getXY(1).isEqualTo(getXY(1), eps)) {
			point = getXY(1);
		}
		return point;
	}

	/** if this butts onto line at right angles.
	 * this and line should be hoizontal/vertical
	 * final point of this should be on target line
	 * @param l line to butt onto
	 * @param eps
	 * @return
	 */
	public boolean makesTJointWith(SVGLine l, double eps) {
		boolean endsOn = false;
		if (this.isHorizontal(eps) && l.isVertical(eps)) {
			RealRange yrange = l.getReal2Range().getYRange();
			double lx = l.getXY(0).getX();
			double thisy = this.getXY(0).getY();
			endsOn = yrange.contains(thisy) && 
				(Real.isEqual(lx, this.getXY(0).getX(), eps) ||
				Real.isEqual(lx, this.getXY(1).getX(), eps));
		} else if (this.isVertical(eps) && l.isHorizontal(eps)) {
			RealRange xrange = l.getReal2Range().getXRange();
			double ly = l.getXY(0).getY();
			double thisx = this.getXY(0).getX();
			endsOn = xrange.contains(thisx) && 
				(Real.isEqual(ly, this.getXY(0).getY(), eps) ||
				Real.isEqual(ly, this.getXY(1).getY(), eps));
		}
		return endsOn;
	}
	
	/** if point is close to one end of line get the other
	 * 
	 * @param point
	 * @param eps
	 * @return coordinates of other end null if point is not at a line end
	 */
	public Real2 getOtherPoint(Real2 point, double eps) {
		Real2 other = null;
		if (point.isEqualTo(getXY(0), eps)) {
			other = getXY(1);
		} else if (point.isEqualTo(getXY(1), eps)) {
			other = getXY(0);
		}
		return other;
	}
	
	public Real2Range getReal2Range() {
		Real2Range real2Range = new Real2Range(getXY(0), getXY(1));
		return real2Range;
	}
	
	public static boolean isEqual(SVGLine line0, SVGLine line1, double eps) {
		Line2 eLine0 = line0.getEuclidLine();
		Line2 eLine1 = line1.getEuclidLine();
		return (eLine0.getXY(0).isEqualTo(eLine1.getXY(0), eps) &&
				eLine0.getXY(1).isEqualTo(eLine1.getXY(1), eps)
				);
	}
	
	/** synonym for getReal2Range.
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			boundingBox = getReal2Range();
		}
		return boundingBox;
	}
	
	public String getXYString() {
		return getXY(0)+ S_SPACE + getXY(1);
	}

	public static List<SVGLine> findHorizontalOrVerticalLines(SVGElement svgElement, double eps) {
		List<SVGLine> horizontalVerticalList = new ArrayList<SVGLine>();
		Nodes lines = svgElement.query(".//svg:line", CMLConstants.SVG_XPATH);
		for (int i = 0; i < lines.size(); i++) {
			SVGLine line = (SVGLine) lines.get(i);
			if (line.isHorizontal(eps) || line.isVertical(eps)) {
				horizontalVerticalList.add(line);
			}
		}
		return horizontalVerticalList;
	}

	public void setWidth(double width) {
		this.addAttribute(new Attribute("stroke-width", String.valueOf(width)));
	}

	/**
	 * 
	 * @param svgLine
	 * @param d max difference in radians from zero
	 * @return
	 */
	public boolean isParallelTo(SVGLine svgLine, double d) {
		Angle angle = this.getEuclidLine().getAngleMadeWith(svgLine.getEuclidLine());
		double dd = Math.abs(angle.getAngle());
		return dd < d;
	}

	/**
	 * are two lines perpendicular 
	 * @param svgLine
	 * @param eps max difference between cosine and 0
	 * @return
	 */
	public boolean isPerpendicularTo(SVGLine svgLine, double eps) {
		Angle angle = this.getEuclidLine().getAngleMadeWith(svgLine.getEuclidLine());
		Double dd = Math.abs(angle.cos());
		return (dd < eps && dd > -eps);
	}

	/** makes a new list composed of the lines in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGLine> extractLines(List<SVGElement> elements) {
		List<SVGLine> lineList = new ArrayList<SVGLine>();
		for (SVGElement element : elements) {
			if (element instanceof SVGLine) {
				lineList.add((SVGLine) element);
			}
		}
		return lineList;
	}

	public Double getLength() {
		Line2 line2 = getEuclidLine();
		return (line2 == null) ? null : line2.getLength();
	}

	/** for horizontal or vertical lines make sure that first coord is smallest
	 * 
	 * @param eps
	 */
	public void normalizeDirection(double eps) {
		Real2 xy0 = getXY(0);
		Real2 xy1 = getXY(1);
		if (isHorizontal(eps)) {
			if (xy0.getX() > xy1.getX()) {
				this.setXY(xy0, 1);
				this.setXY(xy1, 0);
			}
		} else if (isVertical(eps)) {
			if (xy0.getY() > xy1.getY()) {
				this.setXY(xy0, 1);
				this.setXY(xy1, 0);
			}
		}
	}
	
	@Override
	public String getGeometricHash() {
		return getAttributeValue(X1)+" "+getAttributeValue(Y1)+" "+getAttributeValue(X2)+" "+getAttributeValue(Y2);
	}
}
