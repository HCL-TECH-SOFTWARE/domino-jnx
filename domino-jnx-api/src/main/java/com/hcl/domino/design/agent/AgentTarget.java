package com.hcl.domino.design.agent;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the broad document selection for running an agent.
 * 
 * @author Jesse Gallagher
 * @since 1.0.42
 */
public enum AgentTarget implements INumberEnum<Short> {
  /** Unknown or unavailable */
  NONE(RichTextConstants.ASSISTSEARCH_TYPE_NONE),
  /** All documents in database */
  ALL(RichTextConstants.ASSISTSEARCH_TYPE_ALL),
  /** New documents since last run */
  NEW(RichTextConstants.ASSISTSEARCH_TYPE_NEW),
  /** New or modified docs since last run */
  MODIFIED(RichTextConstants.ASSISTSEARCH_TYPE_MODIFIED),
  /** Selected documents */
  SELECTED(RichTextConstants.ASSISTSEARCH_TYPE_SELECTED),
  /** All documents in view */
  VIEW(RichTextConstants.ASSISTSEARCH_TYPE_VIEW),
  /** All unread documents */
  UNREAD(RichTextConstants.ASSISTSEARCH_TYPE_UNREAD),
  /** Prompt user */
  PROMPT(RichTextConstants.ASSISTSEARCH_TYPE_PROMPT),
  /** Works on the selectable object */
  UI(RichTextConstants.ASSISTSEARCH_TYPE_UI);

  private final short value;

  AgentTarget(final short value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}