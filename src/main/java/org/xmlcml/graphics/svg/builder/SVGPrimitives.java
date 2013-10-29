package org.xmlcml.graphics.svg.builder;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;

/** contains lists of SVG primitives in an SVG object.
 * 
 * Basically a DataTransferObject, which is built during interpretation.
 * 
 * @author pm286
 *
 */
public class SVGPrimitives {

	private List<SVGCircle> circleList;
	private List<SVGLine> lineList;
	private List<SVGPath> pathList;
	private List<SVGPolygon> polygonList;
	private List<SVGPolyline> polylineList;
	private List<SVGRect> rectList;
	private List<SVGShape> shapeList; // does not include Path or Line
	private List<SVGText> textList;
	private List<SVGShape> unclassifiedShapeList;
	
	public SVGPrimitives() {
		
	}
	
/**
		List<SVGPolygon>  polygonList = new ArrayList<SVGPolygon>();
		List<SVGPolyline>  polylineList = new ArrayList<SVGPolyline>();
		List<SVGLine>  lineList = new ArrayList<SVGLine>();
		List<SVGShape>  shapeList1 = new ArrayList<SVGShape>();
		SVGSVG svg = new SVGSVG();
		for (SVGShape shape : shapeList) {
			if (shape instanceof SVGPolyline) {
				polylineList.add((SVGPolyline)shape);
				shape.setFill("none");
	//			shape.setStroke("1.0");
	//			svg.appendChild(shape);
			} else if (shape instanceof SVGLine) {
				lineList.add((SVGLine)shape);
				shape.setFill("blue");
				svg.appendChild(shape);
			} else if (shape instanceof SVGPolygon) {
				SVGPolygon polygon = (SVGPolygon)shape;
				SVGCircle circle =  path2ShapeConverter.convertToCircle(polygon);
				polygonList.add((SVGPolygon)shape);
				shape.setFill("green");
				svg.appendChild(shape);
			} else {
				shapeList1.add(shape);
			}
		}
 */

	private void addShapeList(List<SVGShape> shapeList) {
		for (SVGShape shape : shapeList) {
			if (false) {
			} else if (shape instanceof SVGCircle) {
				add((SVGCircle)shape);
			} else if (shape instanceof SVGLine) {
				add((SVGLine)shape);
			} else if (shape instanceof SVGPath) {
				add((SVGPath)shape);
			} else if (shape instanceof SVGPolygon) {
				add((SVGPolygon)shape);
			} else if (shape instanceof SVGPolyline) {
				add((SVGPolyline)shape);
			} else if (shape instanceof SVGRect) {
				add((SVGRect)shape);
			} else {
				add(shape);
			}
		}
	}
	
/**
 * 
 * @param shape
 */
	public void addUnclassified(SVGShape shape) {
		ensureUnclassifiedShapeList();
		unclassifiedShapeList.add(shape);
	}

	private void ensureUnclassifiedShapeList() {
		if (this.unclassifiedShapeList == null) {
			unclassifiedShapeList = new ArrayList<SVGShape>();
		}
	}

	/** circle */
	
	private void ensureCircleList() {
		if (this.circleList == null) {
			circleList = new ArrayList<SVGCircle>();
		}
	}

	public List<SVGCircle> getCircleList() {
		return circleList;
	}

	public void add(SVGCircle circle) {
		ensureCircleList();
		circleList.add(circle);
	}

	public void addCircles(List<SVGCircle> circleList) {
		ensureCircleList();
		circleList.addAll(circleList);
	}
	
	/** line */
	
	private void ensureLineList() {
		if (this.lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
	}

	public List<SVGLine> getLineList() {
		return lineList;
	}

	public void add(SVGLine line) {
		ensureLineList();
		lineList.add(line);
	}

	public void addLines(List<SVGLine> lineList) {
		ensureLineList();
		lineList.addAll(lineList);
	}

	/** path */
	
	private void ensurePathList() {
		if (this.pathList == null) {
			pathList = new ArrayList<SVGPath>();
		}
	}

	public List<SVGPath> getPathList() {
		return pathList;
	}

	public void add(SVGPath path) {
		ensurePathList();
		pathList.add(path);
	}

	public void addPaths(List<SVGPath> pathList) {
		ensurePathList();
		pathList.addAll(pathList);
	}

	/** polygon */
	
	private void ensurePolygonList() {
		if (this.polygonList == null) {
			polygonList = new ArrayList<SVGPolygon>();
		}
	}

	public List<SVGPolygon> getPolygonList() {
		return polygonList;
	}

	public void add(SVGPolygon polygon) {
		ensurePolygonList();
		polygonList.add(polygon);
	}

	public void addPolygons(List<SVGPolygon> polygonList) {
		ensurePolygonList();
		polygonList.addAll(polygonList);
	}
	/** polyline */
	
	private void ensurePolylineList() {
		if (this.polylineList == null) {
			polylineList = new ArrayList<SVGPolyline>();
		}
	}

	public List<SVGPolyline> getPolylineList() {
		return polylineList;
	}

	public void add(SVGPolyline polyline) {
		ensurePolylineList();
		polylineList.add(polyline);
	}

	public void addPolylines(List<SVGPolyline> polylineList) {
		ensurePolylineList();
		polylineList.addAll(polylineList);
	}
	/** rect */
	
	private void ensureRectList() {
		if (this.rectList == null) {
			rectList = new ArrayList<SVGRect>();
		}
	}

	public List<SVGRect> getRectList() {
		return rectList;
	}

	public void add(SVGRect rect) {
		ensureRectList();
		rectList.add(rect);
	}

	public void addRects(List<SVGRect> rectList) {
		ensureRectList();
		rectList.addAll(rectList);
	}

	/** shape */
	
	private void ensureShapeList() {
		if (this.shapeList == null) {
			shapeList = new ArrayList<SVGShape>();
		}
	}

	public List<SVGShape> getShapeList() {
		return shapeList;
	}

	public void add(SVGShape shape) {
		ensureShapeList();
		shapeList.add(shape);
	}

	public void addShapes(List<SVGShape> shapeList) {
		ensureShapeList();
		shapeList.addAll(shapeList);
	}

	/** text */
	
	private void ensureTextList() {
		if (this.textList == null) {
			textList = new ArrayList<SVGText>();
		}
	}

	public List<SVGText> getTextList() {
		return textList;
	}

	public void add(SVGText text) {
		ensureTextList();
		textList.add(text);
	}

	public void addTexts(List<SVGText> textList) {
		ensureTextList();
		textList.addAll(textList);
	}

}
