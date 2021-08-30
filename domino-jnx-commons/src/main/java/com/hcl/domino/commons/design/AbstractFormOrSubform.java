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
package com.hcl.domino.commons.design;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDColor;
import com.hcl.domino.richtext.records.CDDataFlags;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDEmbeddedControl;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDExtField;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDFieldHint;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHotspotEnd;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDKeyword;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.RecordType.Area;

public abstract class AbstractFormOrSubform<T extends GenericFormOrSubform<T>> extends AbstractNamedDesignElement<T>
    implements GenericFormOrSubform<T>, IDefaultActionBarElement {

  public AbstractFormOrSubform(final Document doc) {
    super(doc);
  }

  @Override
  public T addField() {
    throw new NotYetImplementedException();
  }

  @Override
  public List<String> getExplicitSubformRecursive() {
    throw new NotYetImplementedException();
  }

  @Override
  public List<FormField> getFields() {
    final List<FormField> result = new ArrayList<>();
    final Deque<RichTextRecord<?>> fieldStructs = new ArrayDeque<>();

    final Document doc = this.getDocument();
    if (doc.hasItem(NotesConstants.ITEM_NAME_TEMPLATE)) {
      final AtomicBoolean foundFieldBegin = new AtomicBoolean();
      doc.getRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE).forEach(record -> {
        if (record instanceof CDBegin) {
          // Check to see if it's the start of a field
          final CDBegin begin = (CDBegin) record;
          if (begin.getSignature() == RichTextConstants.SIG_CD_FIELD
              || begin.getSignature() == RichTextConstants.SIG_CD_EMBEDDEDCTL) {
            foundFieldBegin.set(true);
          }
        } else if (record instanceof CDField) {
          fieldStructs.add(record);
        } else if (record instanceof CDFieldHint) {
          fieldStructs.add(record);
        } else if (record instanceof CDExtField) {
          fieldStructs.add(record);
        } else if (record instanceof CDExt2Field) {
          fieldStructs.add(record);
        } else if (record instanceof CDIDName) {
          fieldStructs.add(record);
        } else if (record instanceof CDColor) {
          // Don't close fields on COLOR
        } else if (record instanceof CDDataFlags) {
          if (foundFieldBegin.get()) {
            fieldStructs.add(record);
          } else if (!fieldStructs.isEmpty() && !foundFieldBegin.get()) {
            this.flushField(fieldStructs, result);
          }
        } else if (record instanceof CDKeyword) {
          fieldStructs.add(record);
        } else if (record instanceof CDEmbeddedControl) {
          fieldStructs.add(record);
        } else if (record instanceof CDEnd) {
          // Check to see if it's ending an open field
          final CDEnd end = (CDEnd) record;
          if (end.getSignature() == RichTextConstants.SIG_CD_FIELD || end.getSignature() == RichTextConstants.SIG_CD_EMBEDDEDCTL) {
            this.flushField(fieldStructs, result);
            foundFieldBegin.set(false);
          }
        } else if (record instanceof CDHotspotBegin) {
          // First, flush any queued field structs, in the case of pre-R5 fields
          if (!fieldStructs.isEmpty() && !foundFieldBegin.get()) {
            this.flushField(fieldStructs, result);
          }

          final CDHotspotBegin hotspot = (CDHotspotBegin) record;
          if (hotspot.getHotspotType() == CDHotspotBegin.Type.BUNDLE
              || hotspot.getHotspotType() == CDHotspotBegin.Type.V4_SECTION) {
            // TODO add code if we want to handle sections here
          }
          // TODO check for subforms here if we decide we want to recurse field lookups
        } else if (record instanceof CDHotspotEnd) {
          // First, flush any queued field structs, in the case of pre-R5 fields
          if (!fieldStructs.isEmpty() && !foundFieldBegin.get()) {
            this.flushField(fieldStructs, result);
          }

          // TODO add code if we want to handle sections here
        } else {
          // First, flush any queued field structs, in the case of pre-R5 fields
          if (!fieldStructs.isEmpty() && !foundFieldBegin.get()) {
            this.flushField(fieldStructs, result);
          }

          // Ignore other record types
        }
      });

      if (!fieldStructs.isEmpty()) {
        this.flushField(fieldStructs, result);
      }
    }

    return result;
  }

  @Override
  public List<SubformReference> getSubforms() {
    final Document doc = this.getDocument();
    if (doc.hasItem(NotesConstants.ITEM_NAME_TEMPLATE)) {
      return doc.getRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE).stream()
          .filter(CDHotspotBegin.class::isInstance)
          .map(CDHotspotBegin.class::cast)
          .filter(hotspot -> hotspot.getHotspotType() == CDHotspotBegin.Type.SUBFORM)
          .map(hotspot -> {
            if (hotspot.getFlags().contains(CDHotspotBegin.Flag.FORMULA)) {
              return new SubformReference(SubformReference.Type.FORMULA, hotspot.getSubformValue());
            } else {
              return new SubformReference(SubformReference.Type.EXPLICIT, hotspot.getSubformValue());
            }
          })
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public T removeField() {
    throw new NotYetImplementedException();
  }

  @Override
  public void swapFields(final int indexA, final int indexB) {
    throw new NotYetImplementedException();
  }

  @Override
  public boolean isIncludeFieldsInIndex() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOADDFIELDNAMESTOINDEX);
  }
  
  @Override
  public boolean isRenderPassThroughHtmlInClient() {
    return getDocumentFlags3().contains(CDDocument.Flag3.RENDERPASSTHROUGH);
  }

  // *******************************************************************************
  // * Implementation utilities
  // *******************************************************************************

  private void flushField(final Deque<RichTextRecord<?>> structs, final List<FormField> result) {
    if (structs != null && !structs.isEmpty()) {
      // Make sure there's a CDField
      final boolean foundField = structs.stream().anyMatch(s -> s instanceof CDField);
      if (foundField) {
        result.add(new FormFieldImpl(structs));
      }
      structs.clear();
    }
  }
  
  protected RichTextRecordList getFormBodyItem() {
    return getDocument().getRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE);
  }
  
  protected Optional<CDDocument> getDocumentRecord() {
    return getDocument().getRichTextItem(NotesConstants.ITEM_NAME_DOCUMENT, Area.RESERVED_INTERNAL)
      .stream()
      .filter(CDDocument.class::isInstance)
      .map(CDDocument.class::cast)
      .findFirst();
  }
  
  protected Set<CDDocument.Flag> getDocumentFlags() {
    return getDocumentRecord()
      .map(CDDocument::getFlags)
      .orElseGet(Collections::emptySet);
  }
  
  // Later components of CDDOCUMENT were potentially invalid in ancient builds, so check this flag
  
  protected Set<CDDocument.Flag2> getDocumentFlags2() {
    if(!getDocumentFlags().contains(CDDocument.Flag.SPARESOK)) {
      return Collections.emptySet();
    }
    return getDocumentRecord()
      .map(CDDocument::getFlags2)
      .orElseGet(Collections::emptySet);
  }
  
  protected Set<CDDocument.Flag3> getDocumentFlags3() {
    if(!getDocumentFlags().contains(CDDocument.Flag.SPARESOK)) {
      return Collections.emptySet();
    }
    return getDocumentRecord()
      .map(CDDocument::getFlags3)
      .orElseGet(Collections::emptySet);
  }
}
