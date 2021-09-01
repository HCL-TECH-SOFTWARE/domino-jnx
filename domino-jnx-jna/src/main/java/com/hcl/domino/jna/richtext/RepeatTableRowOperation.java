package com.hcl.domino.jna.richtext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.richtext.conversion.PatternBasedTextReplacementConversion;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Item;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextTableParser;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDPreTableBegin;
import com.hcl.domino.richtext.records.CDTableBegin;
import com.hcl.domino.richtext.records.CDTableCell;
import com.hcl.domino.richtext.records.CDTableEnd;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;

public class RepeatTableRowOperation implements RichTextBuilderOperation {
	private String itemName;
	private int reqRowIdx;
	private int nrOfRows;
	private Map<Pattern, BiFunction<Integer, Matcher, Object>> rowFunctions;
	
	public RepeatTableRowOperation(String itemName, int rowIdx, int nrOfRows,
			Map<Pattern, BiFunction<Integer, Matcher, Object>> rowFunctions) {
		this.itemName = itemName;
		this.reqRowIdx = rowIdx;
		this.nrOfRows = nrOfRows;
		this.rowFunctions = rowFunctions==null ? Collections.emptyMap() : rowFunctions;
	}
	
	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		//create a copy of the existing richtext for iteration
		Document docCopy = doc.copyToDatabase(doc.getParentDatabase());
		
		//remove richtext item from work doc
		while (doc.hasItem(itemName)) {
			doc.removeItem(itemName);
		}
		
		//rebuild richtext item in work doc
		try (RichTextWriter rtWriter = doc.createRichTextItem(itemName);) {
			RichTextRecordList rt = docCopy.getRichTextItem(itemName);
			ListIterator<RichTextRecord<?>> rtIterator = rt.listIterator();
			
			RichTextTableParser tableParser = new RichTextTableParser(rtIterator) {

				@Override
				public boolean cellFound(int tableIndex, short rowIdx, short colIdx, CDTableCell cellRecord,
						Collection<RichTextRecord<?>> cellContent) {
					//cell parsing not required here
					return true;
				}

				@Override
				public boolean tableBeginFound(int tableIndex, Collection<RichTextRecord<?>> tableHeaderRecords,
						CDTableBegin tableBegin, Collection<RichTextRecord<?>> nonRowRecords) {
					
					CDPreTableBegin preTableBegin = tableHeaderRecords
							.stream()
							.filter(CDPreTableBegin.class::isInstance)
							.map(CDPreTableBegin.class::cast)
							.findFirst()
							.orElse(null);
					
					if (preTableBegin!=null) {
						if (preTableBegin.getRows() > reqRowIdx) {
							//correct total number of rows if there enough
							preTableBegin.setRows((short) (preTableBegin.getRows() + nrOfRows - 1));
						}
					}
					
					tableHeaderRecords.forEach(rtWriter::addRichTextRecord);
					rtWriter.addRichTextRecord(tableBegin);
					
					nonRowRecords.forEach(rtWriter::addRichTextRecord);
					return true;
				}
				
				@Override
				public boolean rowFound(int tableIndex, short rowIndex, Collection<RichTextRecord<?>> records) {
					if (rowIndex < reqRowIdx) {
						//row is before repeat
						records.forEach(rtWriter::addRichTextRecord);
					}
					else if (rowIndex == reqRowIdx) {
						for (int i=0; i<nrOfRows; i++) {
							addRowRecordsWithPlaceholders(i, records, rtWriter);
						}
					}
					else {
						//row is after repeat, we need to correct the row indexes
						int nestedTableDepth = 0;
						
						for (RichTextRecord<?> rowRecord : records) {
							if (rowRecord instanceof CDTableBegin) {
								nestedTableDepth++;
							}
							else if (rowRecord instanceof CDTableEnd) {
								nestedTableDepth--;
							}
							else if (nestedTableDepth==0 && rowRecord instanceof CDTableCell) {
								CDTableCell cellRecord = (CDTableCell) rowRecord;
								short newRowNumber = (short) (rowIndex + nrOfRows - 1);
								cellRecord.setRow(newRowNumber);
								rtWriter.addRichTextRecord(rowRecord);
							}
							else {
								rtWriter.addRichTextRecord(rowRecord);
							}
						}
						
					}
					return true;
				}

				private void addRowRecordsWithPlaceholders(int idx, Collection<RichTextRecord<?>> records,
						RichTextWriter rtWriter) {
					
					Document docTmp = doc.getParentDatabase().createDocument();
					try (RichTextWriter tmpWriter = docTmp.createRichTextItem("body")) {
						records.forEach(tmpWriter::addRichTextRecord);
					}
					
					rowFunctions.entrySet()
					.stream()
					.forEach((entry) -> {
						Pattern pattern = entry.getKey();
						BiFunction<Integer, Matcher, Object> fct = entry.getValue();
						
						docTmp.convertRichTextItem("body", new PatternBasedTextReplacementConversion(pattern,
								(matcher, fontStyle, tmpRichTextWriter) -> {
							
									Object newContent = fct.apply(idx, matcher);
									if (newContent==null) {
										newContent = "";
									}

									if (newContent instanceof String) {
										String newContentStr = (String) newContent;
										if (newContentStr.length()>0) {
							        		tmpRichTextWriter.addRichTextRecord(CDText.class, (newContentTxtRecord) -> {
							        			newContentTxtRecord.setStyle(fontStyle);
							        			newContentTxtRecord.setText(newContentStr);
							        		});
										}
									}
									else if (newContent instanceof RichTextBuilderContext<?>) {
										Document docToInsert = null;
										Object result = ((RichTextBuilderContext<?>)newContent).build();
										if (result instanceof Document) {
											docToInsert = (Document) result;
										}
										else if (result instanceof DesignElement) {
											docToInsert  = ((DesignElement)result).getDocument();
										}
										
										if (docToInsert!=null) {
//											tmpRichTextWriter.addRichText(docToInsert, ((RichTextBuilderContext<?>)newContent).getItemName());
											
											RichTextUtil.addOtherRichTextItem(docTmp, tmpRichTextWriter, docToInsert,
													((RichTextBuilderContext<?>)newContent).getItemName(), true);
										}
									}
									else if (newContent instanceof GenericFormOrSubform<?>) {
										Document docToInsert = ((GenericFormOrSubform<?>)newContent).getDocument();
//										tmpRichTextWriter.addRichText(docToInsert, "$body");
										
										RichTextUtil.addOtherRichTextItem(docTmp, tmpRichTextWriter, docToInsert, "$body", true);

									}
									else if (newContent instanceof Document) {
										Set<DocumentClass> docClass = ((Document)newContent).getDocumentClass();
										String otherRTItemName = docClass.contains(DocumentClass.DATA) ? "body" : "$body";
//										tmpRichTextWriter.addRichText((Document) newContent, otherRTItemName);
										
										RichTextUtil.addOtherRichTextItem(docTmp, tmpRichTextWriter, (Document) newContent, otherRTItemName, true);
									}
									else {
										throw new IllegalArgumentException(MessageFormat.format("Invalid replacement value type: {0}", newContent.getClass().getName()));
									}
						}));
					});
					
					List<RichTextRecord<?>> fixedRecords = new ArrayList<>();
					
					docTmp.getRichTextItem("body")
					.stream()
					.forEach((record) -> {
						if (record instanceof CDTableCell) {
							CDTableCell cellRecord = (CDTableCell) record;
							//fix row index
							short oldRowIndex = cellRecord.getRow();
							short newRowIndex = (short) (oldRowIndex + idx);
							cellRecord.setRow(newRowIndex);
						}
						fixedRecords.add(record);
					});
					
					fixedRecords.forEach(rtWriter::addRichTextRecord);
				}

				@Override
				public boolean tableEndFound(int tableIndex, CDTableEnd tableEnd) {
					rtWriter.addRichTextRecord(tableEnd);
					
					//stop parsing after first table:
					return false;
				}

				@Override
				public boolean nonTableRecordFound(RichTextRecord<?> record) {
					rtWriter.addRichTextRecord(record);
					return true;
				}
				
			};
			tableParser.parse();
			
			//flush remaining records
			rtIterator.forEachRemaining(rtWriter::addRichTextRecord);
			
		}
		
		return doc;
	}
	
}
