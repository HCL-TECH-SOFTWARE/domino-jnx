/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jna.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.misc.LMBCSCharsetProvider.LMBCSCharset;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.sun.jna.Memory;
import com.sun.jna.ptr.ShortByReference;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class JNANativeItemCoder implements NativeItemCoder {

	@SuppressWarnings("unchecked")
	@Override
	public List<String> decodeStringList(byte[] buf) {
		Memory mem = new Memory(buf.length);
		ByteBuffer dest = mem.getByteBuffer(0, mem.size());
		dest.put(buf);
		return (List<String>)(List<?>)ItemDecoder.decodeTextListValue(mem, false);
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public List<Object> decodeItemValue(byte[] buf, RecordType.Area area) {
	  Memory mem = new Memory(buf.length);
    ByteBuffer dest = mem.getByteBuffer(0, mem.size());
    dest.put(buf);
    Object val = ItemDecoder.readItemValue(mem.share(2), mem.getShort(0), buf.length-2, area);
    return val instanceof List ? (List<Object>)val : Arrays.asList(val);
	}

	@Override
	public byte[] encodeStringList(List<String> values) {
		if(values == null || values.isEmpty()) {
			return new byte[] { 0, 0 };
		}
		else if (values.size()>65535) {
      throw new DominoException(MessageFormat.format("List size exceeds max value of 65535 entries: {0}", values.size()));
    }
		
		DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
		ShortByReference retListSize = new ShortByReference();

		NotesErrorUtils.checkResult(
			NotesCAPI.get().ListAllocate((short) 0, 
				(short) 0,
				0, rethList, null, retListSize)
		);

		return LockUtil.lockHandle(rethList, (handleByVal) -> {
			for (int i=0; i < values.size(); i++) {
				String currStr = values.get(i);
				Memory currStrMem = NotesStringUtils.toLMBCS(currStr, false);
				
	      if (currStrMem!=null && currStrMem.size() > 65535) {
	        throw new DominoException(MessageFormat.format("List item at position {0} exceeds max lengths of 65535 bytes", i));
	      }

	      char textSize = currStrMem==null ? 0 : (char) currStrMem.size();

	      NotesErrorUtils.checkResult(
	          NotesCAPI.get().ListAddEntry(handleByVal, 0, retListSize,
	              (char)i, currStrMem, textSize)
	          );
			}

			
			int size = Short.toUnsignedInt(retListSize.getValue());
			try {
				return Mem.OSLockObject(handleByVal, ptr -> {
					return ptr.getByteArray(0, size);
				});
			} finally {
				Mem.OSMemFree(handleByVal);
			}
		});
	}
	
	@Override
	public Charset getLmbcsCharset(LmbcsVariant variant) {
	  switch(variant) {
      case KEEPNEWLINES:
        return LMBCSCharset.INSTANCE_KEEPNEWLINES;
      case NULLTERM:
        return LMBCSCharset.INSTANCE_NULLTERM;
      case NULLTERM_KEEPNEWLINES:
        return LMBCSCharset.INSTANCE_NULLTERM_KEEPNEWLINES;
      case NORMAL:
      default:
        return LMBCSCharset.INSTANCE;
	  }
	}
	
	@Override
	public List<RichTextRecord<?>> readMemoryRecords(ByteBuffer data, Area area) {
	  if(data.remaining() < 1) {
	    return Collections.emptyList();
	  }
	  
	  byte[] val = new byte[data.remaining()];
	  data.get(val);
	  return RichTextUtil.readMemoryRecords(val, area);
	}
}
