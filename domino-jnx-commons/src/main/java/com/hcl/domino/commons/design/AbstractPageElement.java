package com.hcl.domino.commons.design;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.GenericPageElement;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.RecordType.Area;

public abstract class AbstractPageElement<T extends GenericPageElement<T>> extends AbstractNamedDesignElement<T>
  implements GenericPageElement.ScriptablePageElement<T>, IDefaultActionBarElement {

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
