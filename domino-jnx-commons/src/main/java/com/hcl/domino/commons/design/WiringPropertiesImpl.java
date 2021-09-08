package com.hcl.domino.commons.design;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.WiringProperties;
import com.hcl.domino.misc.NotesConstants;

public class WiringPropertiesImpl extends AbstractNamedFileElement<WiringProperties> implements WiringProperties {

  public WiringPropertiesImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub

  }
  
  @Override
  public String getCharsetName() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public String getMimeType() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMETYPE, String.class, ""); //$NON-NLS-1$
  }

}
