package com.hcl.domino.commons.data.types;

import static com.hcl.domino.commons.data.types.JNXTypeUtil.word;
import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.misc.NotesConstants;

public interface TypedConstants {
  STATUS NOERROR = StatusImpl.NOERROR;
  
  WORD MAXWORD = word(NotesConstants.MAXWORD);
  WORD MAXPATH = word(NotesConstants.MAXPATH);
  WORD MAXUSERNAME = word(NotesConstants.MAXUSERNAME);
  WORD MAXENVVALUE = word(NotesConstants.MAXENVVALUE);
  
  WORD VT_LONG = word(NotesConstants.VT_LONG);
  WORD VT_TEXT = word(NotesConstants.VT_TEXT);
  WORD VT_NUMBER = word(NotesConstants.VT_NUMBER);
  WORD VT_TIMEDATE = word(NotesConstants.VT_TIMEDATE);
  
  WORD KFM_safecopy_Standard = word(NotesConstants.KFM_safecopy_Standard);
  
  WORD ACL_UPDATE_PRIVILEGES = word(NotesConstants.ACL_UPDATE_PRIVILEGES);
  
  WORD FILTER_SELECTED = word(NotesConstants.FILTER_SELECTED);
  
  WORD INFOPARSE_TITLE = word(NotesConstants.INFOPARSE_TITLE);
  WORD INFOPARSE_CLASS = word(NotesConstants.INFOPARSE_CLASS);
  WORD INFOPARSE_DESIGN_CLASS = word(NotesConstants.INFOPARSE_DESIGN_CLASS);
  WORD INFOPARSE_CATEGORIES = word(NotesConstants.INFOPARSE_CATEGORIES);
  
  WORD NOTE_CLASS_FILTER = word(NotesConstants.NOTE_CLASS_FILTER);
  WORD NOTE_CLASS_VIEW = word(NotesConstants.NOTE_CLASS_VIEW);
  WORD NOTE_CLASS_FORM = word(NotesConstants.NOTE_CLASS_FORM);
  
  WORD _NOTE_ID = word(NotesConstants._NOTE_ID);
  WORD _NOTE_OID = word(NotesConstants._NOTE_OID);
  WORD _NOTE_MODIFIED = word(NotesConstants._NOTE_MODIFIED);
  WORD _NOTE_ACCESSED = word(NotesConstants._NOTE_ACCESSED);
  WORD _NOTE_ADDED_TO_FILE = word(NotesConstants._NOTE_ADDED_TO_FILE);
  WORD _NOTE_CLASS = word(NotesConstants._NOTE_CLASS);
  WORD _NOTE_DB = word(NotesConstants._NOTE_DB);
  WORD _NOTE_FLAGS = word(NotesConstants._NOTE_FLAGS);
  WORD _NOTE_FLAGS2 = word(NotesConstants._NOTE_FLAGS2);
  WORD _NOTE_RESPONSE_COUNT = word(NotesConstants._NOTE_RESPONSE_COUNT);
  WORD _NOTE_RESPONSES = word(NotesConstants._NOTE_RESPONSES);
  
  WORD ITEM_SUMMARY = word(NotesConstants.ITEM_SUMMARY);
}
