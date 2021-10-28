/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.jna.internal.converters;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Pointer;

/**
 * {@link DocumentValueConverter} implementation that supports read/write access to item data as
 * a {@link ByteBuffer}, including the type flag (first WORD value).
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class ByteBufferDocumentValueConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return ByteBuffer.class.equals(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
    return ByteBuffer.class.isAssignableFrom(valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		JNAItem item = (JNAItem)obj.getFirstItem(itemName).orElse(null);
		if(itemName == null) {
			return defaultValue;
		}
		
		DisposableMemory itemVal = item.getValueRaw(true);
		return (T)itemVal.getByteBuffer(0, itemVal.size());
	}

	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		throw new UnsupportedOperationException();
	}

	/**
   * Implement this method to write a value to the object
   *
   * @param <T>      value type
   * @param obj      object
   * @param itemFlags item flags
   * @param itemName name of item to write
   * @param newValue new value
	 * 
	 * @since 1.0.43
	 */
	@Override
	public <T> void setValue(Document obj, Set<ItemFlag> itemFlags, String itemName, T newValue) {
	  ByteBuffer buf = (ByteBuffer) newValue;
	  JNADocument jnaDoc = (JNADocument) obj;
	  
    //date type + binary value
	  int valueSize = buf.remaining();
	  
	  short dataTypeShort = buf.getShort();
	  byte[] valueData = new byte[valueSize - 2];
	  buf.get(valueData);
	  
    DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
    short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
    NotesErrorUtils.checkResult(result);
    
    LockUtil.lockHandle(rethItem, (hItemByVal) -> {
      Pointer valuePtr = Mem.OSLockObject(hItemByVal);
      
      try {
        valuePtr.setShort(0, dataTypeShort);
        valuePtr = valuePtr.share(2);
        
        valuePtr.write(0, valueData, 0, valueData.length);

        jnaDoc.appendItemValue(itemName, itemFlags, dataTypeShort, hItemByVal, valueSize);
        return null;
      }
      finally {
        Mem.OSUnlockObject(hItemByVal);
      }
    
    });

	}

}
