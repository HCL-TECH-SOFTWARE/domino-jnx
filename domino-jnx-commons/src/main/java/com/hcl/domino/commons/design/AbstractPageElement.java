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
package com.hcl.domino.commons.design;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.GenericPageElement;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDTarget;
import com.hcl.domino.richtext.records.RecordType.Area;

public abstract class AbstractPageElement<T extends GenericPageElement<T>> extends AbstractDesignElement<T>
  implements GenericPageElement.ScriptablePageElement<T>, IDefaultActionBarElement, IDefaultNamedDesignElement {

  public AbstractPageElement(Document doc) {
    super(doc);
  }
  
  @Override
  public List<?> getBody() {
    return DesignUtil.encapsulateRichTextBody(getDocument(), NotesConstants.ITEM_NAME_TEMPLATE);
  }

  @Override
  public boolean isRenderPassThroughHtmlInClient() {
    return getDocumentFlags3().contains(CDDocument.Flag3.RENDERPASSTHROUGH);
  }
  
  @Override
  public Collection<ScriptEvent> getJavaScriptEvents() {
    return RichTextUtil.readJavaScriptEvents(getHtmlCodeItem());
  }
  
  @Override
  public String getLotusScript() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem(NotesConstants.FORM_SCRIPT_ITEM_NAME, (item, loop) -> {
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
  
  @Override
  public String getLotusScriptGlobals() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem(NotesConstants.DOC_SCRIPT_ITEM, (item, loop) -> {
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
  
  @Override
  public Optional<String> getWindowTitleFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_WINDOWTITLE)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_WINDOWTITLE, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public Optional<String> getTargetFrameFormula() {
    return getHtmlCodeItem().stream()
      .filter(CDTarget.class::isInstance)
      .map(CDTarget.class::cast)
      .findFirst()
      .flatMap(target -> target.getFlags().contains(CDTarget.Flag.IS_FORMULA) ? target.getTargetFormula() : target.getTargetString());
  }
  
  @Override
  public Optional<String> getHtmlBodyAttributesFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_HTMLBODYTAG)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_HTMLBODYTAG, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public Optional<String> getHtmlHeadContentFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_HTMLHEADTAG)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_HTMLHEADTAG, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public List<CDResource> getIncludedStyleSheets() {
    return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_STYLESHEETLIST)
      .stream()
      .filter(CDResource.class::isInstance)
      .map(CDResource.class::cast)
      .collect(Collectors.toList());
  }
  
  @Override
  public Map<EventId, String> getFormulaEvents() {
    Document doc = getDocument();
    return Arrays.stream(EventId.values())
      .filter(id -> id.getItemName() != null)
      .filter(id -> doc.hasItem(id.getItemName()))
      .collect(Collectors.toMap(
        Function.identity(),
        id -> doc.get(id.getItemName(), String.class, "") //$NON-NLS-1$
      ));
  }

  // *******************************************************************************
  // * Implementation utilities
  // *******************************************************************************

  
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
  
  protected RichTextRecordList getFormBodyItem() {
    return getDocument().getRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE);
  }
  
  protected RichTextRecordList getHtmlCodeItem() {
    return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE);
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
