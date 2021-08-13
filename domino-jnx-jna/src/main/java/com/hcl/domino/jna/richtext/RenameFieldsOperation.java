package com.hcl.domino.jna.richtext;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.RichTextRecord;

public class RenameFieldsOperation implements RichTextBuilderOperation {
	private Map<String,String> newFieldNames;
	
	public RenameFieldsOperation(Map<String,String> newFieldNames) {
		//make field names case insensitive
		this.newFieldNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.newFieldNames.putAll(newFieldNames);
	}
	
	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		if (newFieldNames.containsKey("@firstfield")) {
			//resolve first field name
			String firstFieldName = doc.getRichTextItem("$body")
					.stream()
					.filter(CDField.class::isInstance)
					.map(CDField.class::cast)
					.map(record -> record.getName())
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unable to find any field in the form"));
			
			newFieldNames.put(firstFieldName, newFieldNames.remove("@firstfield"));
		}
		
		String rtItemName = ctx.getItemName();
		
		doc.convertRichTextItem(rtItemName, new IRichTextConversion() {
			
			@Override
			public void richTextNavigationStart() {
			}
			
			@Override
			public void richTextNavigationEnd() {
			}
			
			@Override
			public boolean isMatch(List<RichTextRecord<?>> nav) {
				return nav.stream().anyMatch((record) -> {
					return record instanceof CDField || record instanceof CDIDName;
				});
			}
			
			@Override
			public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
				source.forEach((record) -> {
					if (record instanceof CDField) {
						CDField fieldRecord = (CDField) record;
						String fieldName = fieldRecord.getName();
						
						String newFieldName = newFieldNames.get(fieldName);
						if (newFieldName!=null && !newFieldName.equalsIgnoreCase(fieldName)) {
							fieldRecord.setName(newFieldName);
							
							//TODO transform formula and LS code as well
						}
					}
					else if (record instanceof CDIDName) {
						CDIDName htmlAttrRecord = (CDIDName) record;
						
					}
					target.addRichTextRecord(record);
				});
			}
		});
		
		return doc;
	}

}
