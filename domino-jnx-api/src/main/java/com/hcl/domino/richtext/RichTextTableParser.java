package com.hcl.domino.richtext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDPreTableBegin;
import com.hcl.domino.richtext.records.CDTableBegin;
import com.hcl.domino.richtext.records.CDTableCell;
import com.hcl.domino.richtext.records.CDTableDataExtension;
import com.hcl.domino.richtext.records.CDTableEnd;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Utility class that identifies tables in richtext and returns them row by row and cell by cell.<br>
 * The following methods will be called during parsing:<br>
 * <ul>
 * <li>{@link #nonTableRecordFound(RichTextRecord)} - for all CD records outside of tables (before and after)</li>
 * <li>{@link #tableBeginFound(int, Collection, CDTableBegin, Collection)} - when a table has been found</<li>
 * <li>{@link #rowFound(int, short, Collection)} - for each table row with all contained records</li>
 * <li>{@link #cellFound(int, short, short, CDTableCell, Collection)} - for each table cell with the table cell content</li>
 * <li>{@link #tableEndFound(int, CDTableEnd)} - when the table end has been reached</li>
 * </ul>
 * Nested tables in cells are reported as part of the row/cell content.<br><br>
 * 
 * @author Karsten Lehmann
 */
public abstract class RichTextTableParser {
	private ListIterator<RichTextRecord<?>> rtIterator;

	/**
	 * Creates a new parser instance
	 * 
	 * @param records list of richtext records
	 */
	public RichTextTableParser(RichTextRecordList records) {
		this(records.listIterator());
	}
	
	/**
	 * Creates a new parser instance
	 * 
	 * @param rtIterator list iterator of richtext records
	 */
	public RichTextTableParser(ListIterator<RichTextRecord<?>> rtIterator) {
		this.rtIterator = rtIterator;
	}
	
	/**
	 * Creates a new parser instance
	 * 
	 * @param formOrSubform form or subform design element
	 */
	public RichTextTableParser(GenericFormOrSubform<?> formOrSubform) {
		this(formOrSubform.getDocument().getRichTextItem("$body"));
	}
	
	/**
	 * call this method to start parsing
	 */
	public void parse() {
		//index of current table
		int tableIndex = 0;
		
		//optional CDBEGIN/CDEND block with CDPRETABLEBEGIN (R5+) and CDTABLEDATAEXTENSION (R6+)
		List<RichTextRecord<?>> tableHeaderRecords = new ArrayList<>();
		//all records belonging to a table enclosed in CDTABLEBEGIN / CDTABLEEND
		List<RichTextRecord<?>> tableContentRecords = new ArrayList<>();
		
		while (rtIterator.hasNext()) {
			RichTextRecord<?> record = rtIterator.next();

			if (record instanceof CDBegin && ((CDBegin)record).getSignature() == RichTextConstants.SIG_CD_PRETABLEBEGIN) {
				//CDBEGIN/CDEND block with CDPRETABLEBEGIN (R5+) and CDTABLEDATAEXTENSION (R6+)
				tableHeaderRecords.clear();
				tableHeaderRecords.add(record);
				
				//collect all table header records until CDEND
				while (rtIterator.hasNext()) {
					RichTextRecord<?> headerRecord = rtIterator.next();
					tableHeaderRecords.add(headerRecord);
					
					if (headerRecord instanceof CDEnd && ((CDEnd)headerRecord).getSignature() == RichTextConstants.SIG_CD_PRETABLEBEGIN) {
						break;
					}
				}
			}
			else if (record instanceof CDTableBegin) {
				tableContentRecords.clear();
				tableContentRecords.add(record);
				
				int nestedTableDepth = 1;
				
				//read all table content into tableContentRecords, ignore nested tables
				while (rtIterator.hasNext()) {
					RichTextRecord<?> recordInTable = rtIterator.next();
					tableContentRecords.add(recordInTable);
					
					if (recordInTable instanceof CDTableBegin) {
						nestedTableDepth++;
					}
					else if (recordInTable instanceof CDTableEnd) {
						nestedTableDepth--;
						
						if (nestedTableDepth==0) {
							break;
						}
					}
				}
				
				if (nestedTableDepth>0) {
					throw new IllegalStateException("Unexpected non-closed nested table(s) found");
				}
				
				parseTableContent(tableIndex,
						tableHeaderRecords,
						tableContentRecords);
				
				tableHeaderRecords.clear();
				tableContentRecords.clear();
				tableIndex++;
			}
			else {
				nonTableRecordFound(record);
			}
		}
	}
	
	/**
	 * Splits the table content into rows, reporting each row via {@link #rowFound(int, short, Collection)}
	 * 
	 * @param tableIndex index of table (0=first)
	 * @param tableHeaderRecords R5/R6 records with additional table infos, e.g. {@link CDPreTableBegin} and {@link CDTableDataExtension}
	 * @param tableContentRecords table content enclosed in {@link CDTableBegin} and {@link CDTableEnd}
	 * @return true to continue parsing
	 */
	private boolean parseTableContent(int tableIndex,
			List<RichTextRecord<?>> tableHeaderRecords,
			List<RichTextRecord<?>> tableContentRecords) {
		
		//find first cell
		CDTableCell firstCellRecord = null;
		//any records before the first cell
		List<RichTextRecord<?>> nonRowRecords = new ArrayList<>();
		
		ListIterator<RichTextRecord<?>> tableContentRecordsIt = tableContentRecords.listIterator();
		CDTableBegin tableBeginRecord = (CDTableBegin) tableContentRecordsIt.next();
		
		//report any content before the first cell, e.g. CDTABLELABEL
		while (tableContentRecordsIt.hasNext()) {
			RichTextRecord<?> record = tableContentRecordsIt.next();
			if (record instanceof CDTableCell) {
				firstCellRecord = (CDTableCell) record;
				break;
			}
			else {
				nonRowRecords.add(record);
			}
		}
		
		if (firstCellRecord!=null) {
			if (!tableBeginFound(tableIndex, tableHeaderRecords, tableBeginRecord, nonRowRecords)) {
				return false;
			}

			List<RichTextRecord<?>> rowRecords = new ArrayList<>();
			rowRecords.add(firstCellRecord);
			
			short currRowIdx = firstCellRecord.getRow();
			
			CDTableEnd tableEndRecord = null;
			
			while (tableContentRecordsIt.hasNext()) {
				RichTextRecord<?> record = tableContentRecordsIt.next();
				
				if (record instanceof CDTableBegin) {
					//nested table found, skip it until the end
					rowRecords.add(record);
					
					int nestedTableDepth = 0;

					while (tableContentRecordsIt.hasNext()) {
						RichTextRecord<?> nestedRecord = tableContentRecordsIt.next();
						rowRecords.add(nestedRecord);
						
						if (nestedRecord instanceof CDTableBegin) {
							nestedTableDepth++;
						}
						else if (nestedRecord instanceof CDTableEnd) {
							nestedTableDepth--;
							
							if (nestedTableDepth==0) {
								break;
							}
						}
					}
				}
				else if (record instanceof CDTableEnd) {
					//end of table found
					tableEndRecord = (CDTableEnd) record;
					break;
				}
				else if (record instanceof CDTableCell) {
					CDTableCell cellRecord = (CDTableCell) record;
					
					short cellRowIdx = cellRecord.getRow();
					if (cellRowIdx == currRowIdx) {
						//same row as before
						rowRecords.add(record);
					}
					else {
						//report data for whole row
						if (!rowFound(tableIndex, currRowIdx, rowRecords)) {
							return false;
						}
						
						//and split row into cells
						if (!parseRowContent(tableIndex, currRowIdx, rowRecords)) {
							return false;
						}
						
						//start recording records for the new row
						rowRecords.clear();
						rowRecords.add(cellRecord);
						
						currRowIdx = cellRowIdx;
					}
				}
				else {
					rowRecords.add(record);
				}
			}
			
			if (!rowRecords.isEmpty()) {
				//report last row
				if (!rowFound(tableIndex, currRowIdx, rowRecords)) {
					return false;
				}
				
				if (!parseRowContent(tableIndex, currRowIdx, rowRecords)) {
					return false;
				}
			}
			
			if (tableEndRecord!=null) {
				if (!tableEndFound(tableIndex, tableEndRecord)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Splits a row into cells, reporting the content of each cell via {@link #cellFound(int, CDTableCell, Collection)}
	 * 
	 * @param tableIndex table index in richtext (0=first table)
	 * @param rowIdx index of row (0=first row)
	 * @param rowRecords records in row starting with {@link CDTableCell}
	 * @return true to continue parsing
	 */
	private boolean parseRowContent(int tableIndex, short rowIdx, List<RichTextRecord<?>> rowRecords) {
		CDTableCell cellRecord = null;
		List<RichTextRecord<?>> cellContentRecords = new ArrayList<>();
		ListIterator<RichTextRecord<?>> rowRecordsIt = rowRecords.listIterator();
		
		while (rowRecordsIt.hasNext()) {
			RichTextRecord<?> record = rowRecordsIt.next();
			
			if (record instanceof CDTableCell) {
				//new cell has started, report content of last cell
				if (!cellContentRecords.isEmpty()) {
					if (cellRecord!=null && !cellFound(tableIndex, rowIdx, cellRecord.getColumn(), cellRecord, cellContentRecords)) {
						return false;
					}
					cellContentRecords.clear();
				}
				
				cellRecord = (CDTableCell) record;
			}
			else if (record instanceof CDTableBegin) {
				//nested table found; skip it
				cellContentRecords.add(record);
				int nestedTableDepth = 0;
				
				while (rowRecordsIt.hasNext()) {
					RichTextRecord<?> nestedRecord = rowRecordsIt.next();
					cellContentRecords.add(record);
					
					if (nestedRecord instanceof CDTableBegin) {
						nestedTableDepth++;
					}
					else if (nestedRecord instanceof CDTableEnd) {
						nestedTableDepth--;
						
						if (nestedTableDepth==0) {
							break;
						}
					}
				}
			}
			else {
				cellContentRecords.add(record);
			}
		}
		
		//report content of last cell
		if (cellRecord!=null && !cellContentRecords.isEmpty()) {
			if (!cellFound(tableIndex, rowIdx, cellRecord.getColumn(), cellRecord, cellContentRecords)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Method is invoked for CD records outside of a table
	 * 
	 * @param record record
	 * @return true to continue parsing
	 */
	public abstract boolean nonTableRecordFound(RichTextRecord<?> record);

	/**
	 * Method is invoked when a {@link CDTableBegin} record has been found
	 * 
	 * @param tableIndex table index in richtext (0=first table)
	 * @param tableHeaderRecords additional table infos like {@link CDPreTableBegin}/{@link CDTableDataExtension} enclosed in {@link CDBegin}/{@link CDEnd} or empty list for pre R5 tables
	 * @param tableBegin table begin record
	 * @param nonRowRecords records that are not part of the first cell in the first row
	 * @return true to continue parsing
	 */
	public abstract boolean tableBeginFound(
			int tableIndex, Collection<RichTextRecord<?>> tableHeaderRecords, CDTableBegin tableBegin,
			Collection<RichTextRecord<?>> nonRowRecords);
	
	/**
	 * Method is invoked with all CD records of a table row
	 * 
	 * @param tableIndex table index in richtext (0=first table)
	 * @param rowIndex row index (0=first row)
	 * @param records all records in row, each cell starting with {@link CDTableCell}
	 * @return true to continue parsing
	 */
	public abstract boolean rowFound(int tableIndex, short rowIndex, Collection<RichTextRecord<?>> records);

	/**
	 * Method is invoked for each cell of a table
	 * 
	 * @param tableIndex table index in richtext (0=first table)
	 * @param rowIndex row index (0=first row)
	 * @param colIndex column index
	 * @param cellRecord table cell record, e.g. to read spans via {@link CDTableCell#getRowSpan()} or {@link CDTableCell#getColumnSpan()}
	 * @param cellContent content of table cell
	 * @return true to continue parsing
	 */
	public abstract boolean cellFound(int tableIndex, short rowIndex, short colIndex, CDTableCell cellRecord, Collection<RichTextRecord<?>> cellContent);
	
	/**
	 * Method is invoked if {@link CDTableEnd} record was found
	 * 
	 * @param tableIndex table index in richtext (0=first table)
	 * @param tableEnd table end record
	 * @return true to continue parsing
	 */
	public abstract boolean tableEndFound(int tableIndex, CDTableEnd tableEnd);
	
}
