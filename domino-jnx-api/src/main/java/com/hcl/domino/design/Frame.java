package com.hcl.domino.design;

import java.util.Optional;

import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.NOTELINK;

/**
 * Represents one frame of a frameset
 * 
 * @author Karsten Lehmann
 * @since 1.0.40
 */
public interface Frame extends FrameContent<Frame> {
  
  /**
   * Sets the frame name
   * 
   * @param name name
   * @return this frame
   */
	Frame setName(String name);
	
	/**
	 * Reads the frame name
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Returns the sequence number of the frame, used in Nomad Mobile
	 * for phone navigation
	 * 
	 * @return sequence number
	 */
	int getSequenceNo();
	
	/**
	 * Sets the sequence number of the frame, used in Nomad Mobile
   * for phone navigation
   * 
	 * @param n number
	 * @return this frame
	 */
	Frame setSequenceNo(int n);
	
	enum FrameContentType {
		Link,
		NamedElement,
		URL
	}
	
	/**
	 * Returns the type of the content displayed in the frame
	 * 
	 * @return type
	 */
	Optional<FrameContentType> getContentType();
	
	/**
	 * Sets the type/name of a named design element and switches
	 * the frame content type to {@link FrameContentType#NamedElement}.<br>
	 * Supported design element types:
	 * <ul>
	 * <li>{@link Page}</li>
	 * <li>{@link Form}</li>
	 * <li>{@link Frameset}</li>
	 * <li>{@link View}</li>
	 * <li>{@link Folder}</li>
	 * <li>{@link Navigator}</li>
	 * </ul>
	 * 
	 * @param type type
	 * @param name name
	 * @return this frame
	 */
	Frame setNamedElement(Class<? extends DesignElement> type, String name);
	
	/**
	 * Returns the type of the named element
	 * 
	 * @return type
	 */
  Optional<Class<? extends DesignElement>> getNamedElementType();

  /**
   * Returns the name of the named element
   * 
   * @return name
   */
  Optional<String> getNamedElement();

	/**
	 * Sets the URL of the frame content and switches the
	 * frame content type to  {@link FrameContentType#URL}
	 * 
	 * @param url url
	 * @return this frame
	 */
	Frame setContentUrl(String url);
	
	/**
	 * Sets the frame content via doclink and switches the
   * frame content type to  {@link FrameContentType#Link}
   * 
	 * @param link doclink
	 * @return this frame
	 */
	Frame setContentLink(NOTELINK link);
	
	/**
   * Sets the frame content via doclink and switches the
   * frame content type to  {@link FrameContentType#Link}.<br>
   * <br>
   * Please note that there are three possible ways to define
   * valid link data: (replicaid), (replicaid, viewunid) and
   * (replicaid, viewunid, docunid).
	 * 
	 * @param replicaId DB replica ID (for db links)
	 * @param viewUnid view unid (for view links or doc links)
	 * @param docUnid doc unid (for doc links)
	 * @return this frame
	 */
	Frame setContentLink(String replicaId, String viewUnid, String docUnid);
	
	/**
	 * Sets the target frame for links clicked within the
	 * frame
	 * 
	 * @param target target
	 * @return this frame
	 */
	Frame setTargetName(String target);
	
	/**
	 * Returns the target frame for links clicked within the
   * frame
   * 
	 * @return target
	 */
	String getTargetName();
	
	enum ScrollType {
		On,
		Off,
		Auto,
		Default
	}
	
	/**
	 * Sets the scroll type
	 * 
	 * @param type type
	 * @return this frame
	 */
	Frame setScrollType(ScrollType type);

	/**
	 * Gets the scroll type
	 * 
	 * @return type
	 */
	Optional<ScrollType> getScrollType();
	
	/**
	 * Sets whether the frame can be resized
	 * 
	 * @param b true if allowed
	 * @return this frame
	 */
	Frame setAllowResizing(boolean b);

	/**
	 * Reads whether the frame can be resized
	 * 
	 * @return true if allowed
	 */
	boolean isAllowResizing();
	
	/**
	 * Used to set the initial focus in this frame
	 * 
	 * @param b true for initial focus
	 * @return this frame
	 */
	Frame setInitialFocus(boolean b);

	/**
	 * Checks if frame has initial focus
	 * 
	 * @return true for initial focus
	 */
	boolean isInitialFocus();
	
	/**
	 * Sets Designer option "3-D border"
	 * 
	 * @param b true for border
	 * @return this frame
	 */
	Frame setBorderEnabled(boolean b);
	
	/**
	 * Returns the Designer option "3-D border"
	 * 
	 * @return true for border
	 */
	boolean isBorderEnabled();
	
	/**
	 * Returns the formula for the caption border text
	 * 
	 * @param formula formula
	 * @return this frame
	 */
	Frame setCaptionFormula(String formula);
	
	/**
	 * Reads the formula for the caption border text
	 * 
	 * @return formula
	 */
	Optional<String> getCaptionFormula();
	
	enum CaptionMode {
		None,
		CaptionOnly,
		ArrowsOnly,
		Both
	}
	
	/**
	 * Specifies the caption border appearance
	 * 
	 * @param mode mode
	 * @return this frame
	 */
	Frame setCaptionMode(CaptionMode mode);
	
	/**
	 * Returns the caption border appearance
	 * 
	 * @return mode
	 */
	CaptionMode getCaptionMode();
	
	enum CaptionBorderAlignment {
		Left,
		Right,
		Top,
		Bottom
	}
	
	/**
	 * Changes the alignment of the caption/arrow border.<br>
	 * The Designer UI enforces that the border can only be
	 * aligned at a movable edge of the frame.
	 * 
	 * @param align alignment
	 * @return this frame
	 */
	Frame setCaptionBorderAlignment(CaptionBorderAlignment align);

	/**
	 * Returns the alignment of the caption/arrow border.<br>
	 * 
	 * @return alignment
	 */
	Optional<CaptionBorderAlignment> getCaptionBorderAlignment();
	
	enum CaptionTextAlignment {
		Left,
		Right,
		Center
	}
	
	/**
	 * Sets the text alignment within the caption/arrow border.
	 * 
	 * @param align text alignment
	 * @return this frame
	 */
	Frame setCaptionTextAlignment(CaptionTextAlignment align);
	
	/**
	 * Returns the text alignment within the caption/arrow border.
	 * 
	 * @return text alignment
	 */
	Optional<CaptionTextAlignment> getCaptionTextAlignment();
	
	enum OptionUnit {
		Percent,
		Pixels
	}
	
	/**
	 * Choose a size in pixels or as a percent of the frame.<br>
	 * This size is the default size that the frame opens to when the
	 * user clicks on the border of a closed frame.
	 * 
	 * @param amount size
	 * @param unit size unit
	 * @return this frame
	 */
	Frame setOpen(int amount, OptionUnit unit);
	
	/**
	 * Returns the unit for the {@link #getOpen()} value
   * 
	 * @return size
	 */
	Optional<OptionUnit> getOpenUnit();
	
	/**
   * Returns a size in pixels or as a percent of the frame.<br>
   * This size is the default size that the frame opens to when the
   * user clicks on the border of a closed frame.
	 * 
	 * @return size
	 */
	int getOpen();
	
	/**
	 * Sets the font style for the caption text. Use
	 * {@link MemoryStructureWrapperService#newStructure(Class, int)}
	 * with {@link FontStyle} to create a new instance
	 * 
	 * @param style style
	 * @return this frame
	 */
	Frame setCaptionStyle(FontStyle style);
	
	/**
	 * Returns the font style for the caption text
	 * 
	 * @return style
	 */
	FontStyle getCaptionStyle();
	
	/**
	 * Sets the font name for the caption border text
	 * 
	 * @param fontName font name
	 * @return this frame
	 */
	Frame setCaptionFontName(String fontName);
	
	/**
	 * Returns the font name for the caption border text
	 * 
	 * @return font name or empty string
	 */
	String getCaptionFontName();
	
	/**
	 * Sets the text color for the caption border
	 * 
	 * @param color new color
	 * @return this frame
	 */
	Frame setCaptionTextColor(ColorValue color);
	
	/**
	 * Returns the text color for the caption border
	 * 
	 * @return color
	 */
	Optional<ColorValue> getCaptionTextColor();

	/**
	 * Sets the background color for the caption border
	 * 
	 * @param color new color
	 * @return this frame
	 */
	Frame setCaptionBackgroundColor(ColorValue color);

	/**
	 * Returns the background color for the caption border
	 * 
	 * @return color
	 */
	Optional<ColorValue> getCaptionBackgroundColor();
	
	/**
	 * 
	 * @param amount
	 * @return
	 */
	Frame setMarginHeight(int amount);
	
	/**
	 * Resets the margin height to the default value
	 * 
	 * @return this frame
	 */
	Frame setMarginHeightDefault();

	/**
	 * Returns the frame margin height.
	 * 
	 * @return height
	 */
	Optional<Integer> getMarginHeight();
	
	/**
	 * Sets the margin width
	 * 
	 * @param amount size
	 * @return this frame
	 */
	Frame setMarginWidth(int amount);
	
	/**
	 * Resets the margin width to the default value
	 * @return
	 */
	Frame setMarginWidthDefault();
	
	/**
	 * Returns the frame margin width
	 * 
	 * @return width
	 */
	Optional<Integer> getMarginWidth();
	
	/**
	 * Sets the HTML element id
	 * 
	 * @param id id
	 * @return this frame
	 */
	Frame setHTMLId(String id);
	
	/**
	 * Reads the HTML element id
	 * 
	 * @return id
	 */
	String getHTMLId();
	
	/**
	 * Sets the HTML element classname
	 * 
	 * @param c classname
	 * @return this frame
	 */
	Frame setHTMLClassName(String c);
	
	/**
	 * Reads the HTML element classname
	 * 
	 * @return classname
	 */
	String getHTMLClassName();
	
	/**
	 * Sets the HTML element style
	 * 
	 * @param style style
	 * @return this frame
	 */
	Frame setHTMLStyle(String style);
	
	/**
	 * Reads the HTML element style
	 * 
	 * @return style
	 */
	String getHTMLStyle();
	
	/**
	 * Sets the HTML element title
	 * 
	 * @param title title
	 * @return this frame
	 */
	Frame setHTMLTitle(String title);
	
	/**
	 * Reads the HTML element title
	 * 
	 * @return title
	 */
	String getHTMLTitle();
	
	/**
	 * Sets other HTML attributes
	 * 
	 * @param attr attributes
	 * @return this frame
	 */
	Frame setHTMLAttributes(String attr);
	
	/**
	 * Reads other HTML attributes
	 * 
	 * @return attributes
	 */
	String getHTMLAttributes();

	/**
	 * Removes the frame content
	 * 
	 * @return this frame
	 */
	Frame removeFrameContents();
	
}
