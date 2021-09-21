package com.hcl.domino.richtext.structures;

import java.util.Optional;

import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDFrame.BorderAlignment;
import com.hcl.domino.richtext.records.CDFrame.TextAlignment;

/**
 * Variable data values of a {@link CDFrame} that store additional
 * frame properties
 * 
 * @author Karsten Lehmann
 */
public class CDFrameVariableData {
	private CDFrame cdFrame;
	private Optional<String> captionFormula = Optional.empty();
	private Optional<BorderAlignment> borderAlignment = Optional.empty();
	private Optional<TextAlignment> textAlign = Optional.empty();
	private int open;
	private Optional<ColorValue> backgroundColor = Optional.empty();
	private Optional<FontStyle> fontStyle = Optional.empty();
	private Optional<ColorValue> textColor = Optional.empty();
	private Optional<String> fontName = Optional.empty();
	private int sequenceNo;
	
	public CDFrameVariableData(CDFrame cdFrame) {
		this.cdFrame = cdFrame;
	}
	
	public CDFrame getParent() {
		return cdFrame;
	}
	
	public Optional<String> getCaptionFormula() {
		return captionFormula;
	}

	public CDFrameVariableData setCaptionFormula(String captionFormula) {
		this.captionFormula = Optional.ofNullable(captionFormula);
		return this;
	}

	public Optional<BorderAlignment> getBorderAlignment() {
		return borderAlignment;
	}

	public CDFrameVariableData setBorderAlignment(BorderAlignment borderAlignment) {
		this.borderAlignment = Optional.ofNullable(borderAlignment);
		return this;
	}

	/**
	 * Returns the text alignment within the caption border
	 * 
	 * @return alignment
	 */
	public Optional<TextAlignment> getTextAlignment() {
		return textAlign;
	}

	/**
	 * Sets the text alignment within the caption border
	 * 
	 * @param textAlign new alignment
	 * @return this frame data
	 */
	public CDFrameVariableData setTextAlignment(TextAlignment textAlign) {
		this.textAlign = Optional.ofNullable(textAlign);
		return this;
	}

	public int getOpen() {
		return open;
	}

	public CDFrameVariableData setOpen(int open) {
		this.open = open;
		return this;
	}

	public Optional<ColorValue> getBackgroundColor() {
		return backgroundColor;
	}

	public CDFrameVariableData setBackgroundColor(ColorValue backgroundColor) {
		this.backgroundColor = Optional.ofNullable(backgroundColor);
		return this;
	}

	public Optional<FontStyle> getFontStyle() {
		return fontStyle;
	}

	public CDFrameVariableData setFontStyle(FontStyle fontId) {
		this.fontStyle = Optional.ofNullable(fontId);
		return this;
	}

	public Optional<ColorValue> getTextColor() {
		return textColor;
	}

	public CDFrameVariableData setTextColor(ColorValue textColor) {
		this.textColor = Optional.ofNullable(textColor);
		return this;
	}

	public Optional<String> getFontName() {
		return fontName;
	}

	public CDFrameVariableData setFontName(String fontName) {
		this.fontName = Optional.ofNullable(fontName);
		return this;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public CDFrameVariableData setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
		return this;
	}

	@Override
	public String toString() {
		return "CDFrameVariableData [captionFormula=" + captionFormula + ", unknownValue=" + borderAlignment //$NON-NLS-1$ //$NON-NLS-2$
				+ ", textAlign=" + textAlign + ", open=" + open + ", backgroundColor=" + backgroundColor //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ", fontStyle=" + fontStyle + ", textColor=" + textColor + ", fontName=" + fontName + ", sequenceNo=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ sequenceNo + "]"; //$NON-NLS-1$
	}

	
}
