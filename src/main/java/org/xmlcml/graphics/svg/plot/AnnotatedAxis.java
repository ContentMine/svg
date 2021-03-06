package org.xmlcml.graphics.svg.plot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.plot.AxialBox.AxisType;
import org.xmlcml.graphics.svg.text.SVGPhrase;
import org.xmlcml.graphics.svg.text.SVGWord;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * An axis (vertical of horizontal) with (probably) one or more
 *   axial line (SVGLine)
 *   tick marks (major and minor)
 *   scales (list of numbers aligned with ticks)
 *   axial titles
 *   
 * @author pm286
 *
 */
public class AnnotatedAxis {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxis.class);
	private static final double AXIS_END_EPS = 1.0;
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	static double EPS = 0.01;
	private LineDirection direction;
	private RealRange range;
	private RealArray majorTicksScreenCoords; // the position of the minor ticks
	private RealArray minorTicksPixels; // the position of the major ticks
	private RealArray tickNumberUserCoords; // the actual numbers in the scale
	private RealArray tickNumberScreenCoords; // the best estimate of the numbers positions
	private String tickSignature;
	private Double majorTickLength;
	private Double minorTickLength;
	private SVGLine singleLine;
	private Double screenToUserScale;
	private Double screenToUserConstant;
	private AxisTickBox axisTickBox;
	private AxialBox axialBox;
	private List<SVGLine> tickLines;
	SVGPhrase scalesPhrase;
	private AxisType axisType;


	protected AnnotatedAxis(AxialBox axialBox) {
		this.axialBox = axialBox;
	}
	
	public AnnotatedAxis(AxialBox axialBox, AxisType axisType) {
		this(axialBox);
		this.axisType = axisType;
		this.direction = axisType == null ? null : axisType.getDirection();		
	}

	void setRange(RealRange range) {
		this.range = range;
	}

	public RealArray getMajorTicksPixels() {
		return majorTicksScreenCoords;
	}

	public void setMajorTicksPixels(RealArray majorTicksPixels) {
		this.majorTicksScreenCoords = majorTicksPixels;
	}

	public RealArray getMinorTicksPixels() {
		return minorTicksPixels;
	}

	public void setMinorTicksPixels(RealArray minorTicksPixels) {
		this.minorTicksPixels = minorTicksPixels;
	}

	public RealArray getTickNumberUserCoords() {
		return tickNumberUserCoords;
	}

	public void setTickNumberUserCoords(RealArray tickNumberUserCoords) {
		this.tickNumberUserCoords = tickNumberUserCoords;
	}

	public RealArray getTickNumberScreenCoords() {
		return tickNumberScreenCoords;
	}

	public void setTickNumberScreenCoords(RealArray tickNumberScreenCoords) {
		this.tickNumberScreenCoords = tickNumberScreenCoords;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("dir: "+direction+"; ");
		sb.append("range: "+range+"\n");
		sb.append("majorTicks: "+majorTicksScreenCoords+"\n");
		sb.append("minorTicks: "+minorTicksPixels+"\n");
		sb.append("tickNumberUserCoords: "+tickNumberUserCoords+"\n");
		sb.append("tickNumberScreenCoords: "+tickNumberScreenCoords+"\n");
		return sb.toString();
	}

	public void setTickSignature(String string) {
		this.tickSignature = string;
	}

	public void setMajorTickLength(Double majorTickLength) {
		this.majorTickLength = majorTickLength;
	}

	public void setMinorTickLength(Double minorTickLength) {
		this.minorTickLength = minorTickLength;
	}

	public RealRange getRange() {
		return range;
	}

	public String getTickSignature() {
		return tickSignature;
	}

	public Double getMajorTickLength() {
		return majorTickLength;
	}

	public Double getMinorTickLength() {
		return minorTickLength;
	}

	public void setSingleLine(SVGLine singleLine) {
		this.singleLine = singleLine;
	}

	public SVGLine getSingleLine() {
		return singleLine;
	}
	
	public LineDirection getDirection() {
		return direction;
	}

	public void setDirection(LineDirection direction) {
		this.direction = direction;
	}

	public SVGPhrase getScalesPhrase() {
		return scalesPhrase;
	}

	public void setScalesPhrase(SVGPhrase scalesPhrase) {
		this.scalesPhrase = scalesPhrase;
	}

	private static RealArray getPixelCoordinatesForTickLines(LineDirection direction, List<SVGLine> tickLines) {
		double[] coord = new double[tickLines.size()];
		for (int i = 0; i < tickLines.size(); i++) {
			SVGLine tickLine = tickLines.get(i);
			Real2 xy = tickLine.getXY(0);
			coord[i] = (LineDirection.HORIZONTAL.equals(direction)) ? xy.getX() : xy.getY();
		}
		RealArray tickLineCoordArray = new RealArray(coord);
		return tickLineCoordArray;
	}

	private void mapTicksToTickValues() {
		if (tickNumberScreenCoords == null) {
			if (tickNumberUserCoords != null && majorTicksScreenCoords != null) {
				int missingTickCount = tickNumberUserCoords.size() - majorTicksScreenCoords.size();
				if (missingTickCount == 0) {
					// we ought to check values of tick values?
					tickNumberScreenCoords = new RealArray(majorTicksScreenCoords);
				} else if (missingTickCount == 1 || missingTickCount == 2) {
					missingTickCount -= addMissingEndTicks();
					if (missingTickCount == 0) {
						tickNumberScreenCoords = new RealArray(majorTicksScreenCoords);
					} else {
						LOG.error("missing "+missingTickCount+" from axis");
					}
				}
			} else {
				LOG.debug("missing tickNumberUserCoords and/or majorTicksScreenCoords");
			}
		} else {
			LOG.debug("Cannot map ticks to pixels");
		}
	}

	private int addMissingEndTicks() {
		int added = 0;
		Double lowAxis = range.getMin();
		Double lowTickPosition = majorTicksScreenCoords.get(0);
		if (lowTickPosition - lowAxis > AXIS_END_EPS) {
			majorTicksScreenCoords.insertElementAt(0, lowAxis);
			added++;
		}
		Double hiAxis = range.getMax();
		Double hiTickPosition = majorTicksScreenCoords.get(majorTicksScreenCoords.size() - 1);
		if (hiAxis - hiTickPosition > AXIS_END_EPS) {
			majorTicksScreenCoords.addElement(hiAxis);
			added++;
		}
		return added;
	}

	private void createScreenToUserTransform() {
		if (majorTicksScreenCoords != null && tickNumberUserCoords != null) {
			screenToUserScale = majorTicksScreenCoords.getRange().getScaleTo(tickNumberUserCoords.getRange());
			screenToUserConstant = majorTicksScreenCoords.getRange().getConstantTo(tickNumberUserCoords.getRange());
			LOG.debug("screen2User: "+screenToUserScale+"; "+screenToUserConstant);
		} else {
			LOG.debug("no majorTicksScreenCoords or tickNumberUserCoords");
		}
	}
	
	/** transform screen coords on this axis to user coords (numbers on axis).
	 * 
	 * @param xscreen
	 * @return
	 */
	 public double transformScreenToUser(double xscreen) {
		return majorTicksScreenCoords.getRange().transformToRange(tickNumberUserCoords.getRange(), xscreen);
	}

	AxisTickBox createTickBoxAndAxialLines(SVGLine h1, List<SVGLine> horizontalLines, List<SVGLine> verticalLines) {
		this.axisTickBox = null;
		if (verticalLines != null && verticalLines.size() > 0) {
			this.axisTickBox = new AxisTickBox(h1, direction);
			axisTickBox.extractContainedAxialLines(horizontalLines, verticalLines);
		}
		return axisTickBox;
	}

	private SVGPhrase processScales(AxialBox axialBox) {
		this.scalesPhrase = null;
		if (axialBox.getTextList().size() > 0) {
			if (LineDirection.HORIZONTAL.equals(this.direction)) {
				processHorizontalAxis(axialBox);
			} else {
				processVerticalAxis(axialBox);
			}
		}
		return this.scalesPhrase;
	}

	private void processVerticalAxis(AxialBox axialBox) {
		List<SVGWord> wordList = new ArrayList<SVGWord>();
		SVGWord word = new SVGWord(axialBox.getTextList().get(0));
		wordList.add(word);
		for (int i = 1; i < axialBox.getTextList().size(); i++) {
			SVGText text = axialBox.getTextList().get(i);
			if (word.canAppend(text)) {
				word.append(text);
			} else {
				word = new SVGWord(axialBox.getTextList().get(i));
				wordList.add(word);
			}
		}
		double[] values = new double[wordList.size()];
		for (int i = 0; i < wordList.size(); i++) {
			SVGWord word0 = wordList.get(i);
			String ss = word0.getStringValue();
			AxialBox.LOG.trace("ss "+ss);
			values[i] = new Double(ss);
		}
		RealArray realArray = new RealArray(values);
		setTickNumberUserCoords(realArray);
	}

	private void processHorizontalAxis(AxialBox axialBox) {
		scalesPhrase = SVGPhrase.createPhraseFromCharacters(axialBox.getTextList());
		setTickNumberUserCoords(scalesPhrase.getNumericValues());
	}

	private void processTitle() {
		AxialBox.LOG.trace("AxisTitle title NYI");
	}

	void createAxisAndRanges(AxialBox axialBox) {
		LOG.debug("createAxisAndRanges: "+this.axisType+"; tickLines: "+tickLines);
		if (singleLine == null) {
			throw new RuntimeException("null line in :"+this);
		}
		RealRange range = singleLine.getBoundingBox().getXRange();
		// assume sorted - we'll need to add sort later
		Real2Range tick2Range = SVGLine.getReal2Range(this.tickLines);
		RealRange tickRange = LineDirection.HORIZONTAL.equals(this.direction) ? tick2Range.getXRange() : tick2Range.getYRange();
		if (RealRange.isEqual(range, tickRange, AnnotatedAxis.EPS)) {
			setRange(range);
		} else if (axialBox.useRange) {
			// use length or axis
			setRange(range);
		}
	}

	/**
	 * public only because of test
	 */
	public void calculateAxisPropertiesAndReturnAxis() {
		mapTicksToTickValues();
		createScreenToUserTransform();
	}

	private void analyzeMajorAndMinorTickLengths(Multiset<Double> tickLengths) {
		Double majorTickLength = null;
		Double minorTickLength = null;
		for (Double d : tickLengths.elementSet()) {
			if (majorTickLength == null) {
				majorTickLength = d;
			} else {
				if (d < majorTickLength) {
					minorTickLength = d;
				} else {
					minorTickLength = majorTickLength;
					majorTickLength = d;
				}
			}
		}
		setMajorTickLength(majorTickLength);
		setMinorTickLength(minorTickLength);
	}

	private void getTickLinesAndSignature(AxialBox axialBox) {
		StringBuilder sb = new StringBuilder();
		List<SVGLine> majorTickLines = new ArrayList<SVGLine>();
		List<SVGLine> minorTickLines = new ArrayList<SVGLine>();
		for (SVGLine tickLine : this.tickLines) {
			Double l = tickLine.getLength();
			String ss = null;
			if (Real.isEqual(l,  getMajorTickLength(), AnnotatedAxis.EPS)) {
				ss = AxialBox.MAJOR_CHAR;
				majorTickLines.add(tickLine);
			} else {
				ss = AxialBox.MINOR_CHAR;
				minorTickLines.add(tickLine);
			}
			sb.append(ss);
		}
		setMajorTicksPixels(getPixelCoordinatesForTickLines(direction, majorTickLines));
		setMinorTicksPixels(getPixelCoordinatesForTickLines(direction, minorTickLines));
		setTickSignature(sb.toString());
	}

	void createMainAndTickLines(LineDirection direction, SVGLine singleLine, List<SVGLine> tickLines) {
		LOG.debug("creating ticklines "+singleLine+"; "+tickLines);
		setSingleLine(singleLine);
		setDirection(direction);
		this.tickLines = tickLines;
		Multiset<Double> tickLengths = HashMultiset.create();
		for (SVGLine tickLine : tickLines) {
			tickLengths.add((Double)Real.normalize(tickLine.getLength(), 2));
		}
		LOG.debug(">ticks>"+tickLengths);
		if (tickLengths.elementSet().size() == 1) {
			setMajorTickLength(tickLengths.elementSet().iterator().next());
			getTickLinesAndSignature(axialBox);
		} else if (tickLengths.elementSet().size() == 2) {
			analyzeMajorAndMinorTickLengths(tickLengths);
			getTickLinesAndSignature(axialBox);
		} else {
			AxialBox.LOG.error("cannot process ticks: "+tickLengths);
		}
	}

	void processScalesTitle(AxialBox axialBox) {
		processScales(axialBox);
		processTitle();
	}

	public void setAxisTickBox(AxisTickBox axisTickBox) {
		this.axisTickBox = axisTickBox;
	}
	
}
