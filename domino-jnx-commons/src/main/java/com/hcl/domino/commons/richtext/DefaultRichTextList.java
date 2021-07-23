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

    public RichTextIterator(final int index) {
      this.index = index;
    }

    @Override
    public void add(final RichTextRecord<?> e) {
      DefaultRichTextList.this.add(this.index, e);
    }

    @Override
    public boolean hasNext() {
      // If we already hit the end, use that
      if (DefaultRichTextList.this.size > -1 && this.index >= DefaultRichTextList.this.size) {
        return false;
      }
      DefaultRichTextList.this.fillTo(this.index);
      return this.index < DefaultRichTextList.this.cache.size();
    }

    @Override
    public boolean hasPrevious() {
      return this.index >= 0;
    }

    @Override
    public RichTextRecord<?> next() {
      final RichTextRecord<?> r = DefaultRichTextList.this.get(this.index);
      this.index++;
      return r;
    }

    @Override
    public int nextIndex() {
      return this.index;
    }

    @Override
    public RichTextRecord<?> previous() {
      this.index--;
      return DefaultRichTextList.this.get(this.index);
    }

    @Override
    public int previousIndex() {
      return this.index - 1;
    }

    @Override
    public void remove() {
      DefaultRichTextList.this.remove(this.index);
    }

    @Override
    public void set(final RichTextRecord<?> e) {
      DefaultRichTextList.this.set(this.index, e);
    }

  }

  private final RichtextNavigator nav;
  private final List<RichtextPosition> cache = new ArrayList<>();
  private int size = -1;
  private final RecordType.Area area;

  public DefaultRichTextList(final RichtextNavigator nav, final RecordType.Area area) {
    this.nav = Objects.requireNonNull(nav, "nav cannot be null");
    this.area = area;
  }

  private void fillTo(final int index) {
    while (this.cache.size() <= index) {
      boolean moved;
      if (this.cache.size() == 0) {
        moved = this.nav.gotoFirst();
      } else {
        moved = this.nav.gotoNext();
      }
      if (!moved) {
        // Then we hit the end - mark it and end
        this.size = this.cache.size();
        return;
      }
      this.cache.add(this.nav.getCurrentPosition());
    }
  }

  @Override
  public RichTextRecord<?> get(final int index) {
    this.fillTo(index);
    if (index >= this.cache.size()) {
      throw new IndexOutOfBoundsException(MessageFormat.format("Index: {0}, Size: {1}", index, this.size));
    }
    final RichtextPosition cur = this.nav.getCurrentPosition();
    try {
      this.nav.restorePosition(this.cache.get(index));
      final RichTextRecord<?> record = this.nav.getCurrentRecord();
      if (this.area != null && record instanceof AbstractCDRecord) {
        final RecordType type = Stream.of(this.area, RecordType.Area.RESERVED_INTERNAL)
            .map(a -> RecordType.getRecordTypeForConstant(record.getTypeValue(), a))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        if (type != null) {
          final Class<? extends RichTextRecord<?>> encapsulation = type.getEncapsulation();
          if (encapsulation != null) {
            return RichTextUtil.reencapsulateRecord((AbstractCDRecord<?>) record, encapsulation);
          }
        }
      }
      return record;
    } finally {
      this.nav.restorePosition(cur);
    }
  }

  @Override
  public boolean isEmpty() {
    this.fillTo(0);
    return this.cache.isEmpty();
  }

  @Override
  public Iterator<RichTextRecord<?>> iterator() {
    return this.listIterator(0);
  }

  @Override
  public ListIterator<RichTextRecord<?>> listIterator(final int index) {
    return new RichTextIterator(index);
  }

  @Override
  public int size() {
    if (this.size == -1) {
      this.fillTo(Integer.MAX_VALUE);
    }

    return this.size;
  }

  @Override
  public Spliterator<RichTextRecord<?>> spliterator() {
    return Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED);
  }
}
