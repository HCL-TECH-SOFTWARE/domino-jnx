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
package com.hcl.domino.commons.richtext;

import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import com.hcl.domino.commons.richtext.RichtextNavigator.RichtextPosition;
import com.hcl.domino.commons.richtext.records.AbstractCDRecord;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

public class DefaultRichTextList extends AbstractList<RichTextRecord<?>> implements RichTextRecordList {
	private class RichTextIterator implements ListIterator<RichTextRecord<?>> {
		private int index;
		
		public RichTextIterator(int index) {
			this.index = index;
		}

		@Override
		public boolean hasNext() {
			// If we already hit the end, use that
			if(size > -1 && this.index >= size) {
				return false;
			}
			fillTo(index);
			return index < cache.size();
		}

		@Override
		public RichTextRecord<?> next() {
			RichTextRecord<?> r = DefaultRichTextList.this.get(this.index);
			this.index++;
			return r;
		}

		@Override
		public boolean hasPrevious() {
			return index >= 0;
		}

		@Override
		public RichTextRecord<?> previous() {
			this.index--;
			return DefaultRichTextList.this.get(this.index);
		}

		@Override
		public int nextIndex() {
			return this.index;
		}

		@Override
		public int previousIndex() {
			return this.index-1;
		}

		@Override
		public void remove() {
			DefaultRichTextList.this.remove(this.index);
		}

		@Override
		public void set(RichTextRecord<?> e) {
			DefaultRichTextList.this.set(this.index, e);
		}

		@Override
		public void add(RichTextRecord<?> e) {
			DefaultRichTextList.this.add(this.index, e);
		}
		
	}
	
	private final RichtextNavigator nav;
	private final List<RichtextPosition> cache = new ArrayList<>();
	private int size = -1;
	private final RecordType.Area area;
	
	public DefaultRichTextList(RichtextNavigator nav, RecordType.Area area) {
		this.nav = Objects.requireNonNull(nav, "nav cannot be null");
		this.area = area;
	}

	@Override
	public RichTextRecord<?> get(int index) {
		fillTo(index);
		if(index >= cache.size()) {
			throw new IndexOutOfBoundsException(MessageFormat.format("Index: {0}, Size: {1}", index, size));
		}
		RichtextPosition cur = nav.getCurrentPosition();
		try {
			nav.restorePosition(cache.get(index));
			RichTextRecord<?> record = nav.getCurrentRecord();
			if(area != null && record instanceof AbstractCDRecord) {
				RecordType type = Stream.of(area, RecordType.Area.RESERVED_INTERNAL)
					.map(a -> RecordType.getRecordTypeForConstant(record.getTypeValue(), a))
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
				if(type != null) {
					Class<? extends RichTextRecord<?>> encapsulation = type.getEncapsulation();
					if(encapsulation != null) {
						return RichTextUtil.reencapsulateRecord((AbstractCDRecord<?>)record, encapsulation);
					}
				}
			}
			return record;
		} finally {
			nav.restorePosition(cur);
		}
	}

	@Override
	public int size() {
		if(this.size == -1) {
			fillTo(Integer.MAX_VALUE);
		}
		
		return this.size;
	}
	
	@Override
	public ListIterator<RichTextRecord<?>> listIterator(int index) {
		return new RichTextIterator(index);
	}
	
	@Override
	public Iterator<RichTextRecord<?>> iterator() {
		return listIterator(0);
	}
	
	@Override
	public Spliterator<RichTextRecord<?>> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
	}
	
	@Override
	public boolean isEmpty() {
		fillTo(0);
		return cache.isEmpty();
	}
	
	private void fillTo(int index) {
		while(cache.size() <= index) {
			boolean moved;
			if(cache.size() == 0) {
				moved = nav.gotoFirst();
			} else {
				moved = nav.gotoNext();
			}
			if(!moved) {
				// Then we hit the end - mark it and end
				this.size = cache.size();
				return;
			}
			cache.add(nav.getCurrentPosition());
		}
	}
}
