package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code queryods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface QueryOds {
  
  /** Include all children */
  int QUERY_FLAG_INCLUDEDESCENDANTS = 0x00000001;

  /**   String is a Notes Full Text Search Query String  */
  int TEXTTERM_FLAG_RAW = 0x00000001;
  /**   String is in Verity Syntax  */
  int TEXTTERM_FLAG_VERITY = 0x00000002;
  /**   String is comma-separated list of words; AND assumed  */
  int TEXTTERM_FLAG_AND = 0x00000004;
  /**   String is comma-separated list of words; ACCRUE assumed  */
  int TEXTTERM_FLAG_ACCRUE = 0x00000008;
  /**   String is comma-separated list of words; NEAR assumed  */
  int TEXTTERM_FLAG_NEAR = 0x00000010;
  /**   This object is displayed as plain text  */
  int TEXTTERM_FLAG_PLAINTEXT = 0x00000020;
  
  int MAXTEXTTERMCOUNT = 10;
  
  /**  Search on modified and created date */
  int QUERYBYFIELD_FLAG_BYDATE = 0x00000001;
  /**  Search by author */
  int QUERYBYFIELD_FLAG_BYAUTHOR = 0x00000002;
  
  /**   Greater than value  */
  short QUERYBYFIELD_OP_GREATER = 1;
  /**   Less than value  */
  short QUERYBYFIELD_OP_LESS = 2;
  /**   Not equal to value  */
  short QUERYBYFIELD_OP_NOTEQUAL = 3;
  /**   Between Date1 and Date2  */
  short QUERYBYFIELD_OP_BETWEEN = 4;
  /**   Not between Date1 and Date2  */
  short QUERYBYFIELD_OP_NOTWITHIN = 5;
  /**   Equal to value  */
  short QUERYBYFIELD_OP_EQUAL = 6;
  /**   Contains value  */
  short QUERYBYFIELD_OP_CONTAINS = 7;
  /**   In the last n days  */
  short QUERYBYFIELD_OP_INTHELAST = 8;
  /**   In the next n days  */
  short QUERYBYFIELD_OP_INTHENEXT = 9;
  /**   Older than n days  */
  short QUERYBYFIELD_OP_OLDERTHAN = 10;
  /**   Due more than n days from now  */
  short QUERYBYFIELD_OP_DUEIN = 11;
  /**   Does not contain  */
  short QUERYBYFIELD_OP_DOESNOTCONTAIN = 12;
  
  /** Show formula as plain text */
  int QUERYFORMULA_FLAG_PLAINTEXT = 0x00000001;
  
  /** Folder is private */
  int QUERYBYFOLDER_FLAG_PRIVATE = 0x00000001;
}
