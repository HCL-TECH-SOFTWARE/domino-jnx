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

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Form;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.records.CDDocument;

public class FormImpl extends AbstractFormOrSubform<Form> implements Form, IDefaultAutoFrameElement {

  public FormImpl(final Document doc) {
    super(doc);
  }

  @Override
  public Optional<String> getNotesXPageAlternative() {
    final String val = this.getDocument().get(DesignConstants.XPAGE_ALTERNATE_CLIENT, String.class, null);
    if (val == null || val.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(val);
    }
  }

  @Override
  public Optional<String> getWebXPageAlternative() {
    final String val = this.getDocument().get(DesignConstants.XPAGE_ALTERNATE, String.class, null);
    if (val == null || val.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(val);
    }
  }

  @Override
  public void initializeNewDesignNote() {

  }

  @Override
  public Type getType() {
    return getDocumentRecord()
      .map(CDDocument::getFlags)
      .map(flags -> {
        return flags.contains(CDDocument.Flag.NOTEREF_MAIN) ? Type.RESPONSE
            : flags.contains(CDDocument.Flag.NOTEREF) ? Type.RESPONSE_TO_RESPONSE
            : Type.DOCUMENT;
      })
      .orElse(Type.DOCUMENT);
  }

  @Override
  public MenuInclusion getMenuInclusionMode() {
    String flags = getFlags();
    if(flags.contains(NotesConstants.DESIGN_FLAG_OTHER_DLG)) {
      return MenuInclusion.CREATE_OTHER;
    } else if(flags.contains(NotesConstants.DESIGN_FLAG_NO_COMPOSE)) {
      return MenuInclusion.NONE;
    } else {
      return MenuInclusion.CREATE;
    }
  }

  @Override
  public boolean isIncludeInSearchBuilder() {
    return !getFlags().contains(NotesConstants.DESIGN_FLAG_NO_QUERY);
  }

  @Override
  public boolean isIncludeInPrint() {
    return getFlags().contains(NotesConstants.DESIGN_FLAG_PRINTFORM);
  }

  @Override
  public VersioningBehavior getVersioningBehavior() {
    return getDocumentRecord()
      .map(rec -> {
        Set<CDDocument.Flag> flags = rec.getFlags();
        Set<CDDocument.Flag2> flags2 = getDocumentFlags2();
        if(flags2.contains(CDDocument.Flag2.UPDATE_SIBLING)) {
          return VersioningBehavior.NEW_AS_SIBLINGS;
        } else if(flags.contains(CDDocument.Flag.UPDATE_RESPONSE)) {
          return VersioningBehavior.NEW_AS_RESPONSES;
        } else if(flags.contains(CDDocument.Flag.UPDATE_PARENT)) {
          return VersioningBehavior.PRIOR_AS_RESPONSES;
        } else {
          return VersioningBehavior.NONE;
        }
      })
      .orElse(VersioningBehavior.NONE);
  }

  @Override
  public boolean isVersionCreationAutomatic() {
    return !getDocumentFlags2().contains(CDDocument.Flag2.MANVCREATE);
  }

  @Override
  public boolean isDefaultForm() {
    return getDocument().getDocumentClass().contains(DocumentClass.DEFAULT);
  }

  @Override
  public boolean isStoreFormInDocument() {
    return getDocumentFlags().contains(CDDocument.Flag.BOILERPLATE);
  }

  @Override
  public boolean isAllowFieldExchange() {
    return !getDocumentFlags2().contains(CDDocument.Flag2.DISABLE_FX);
  }

  @Override
  public boolean isAutomaticallyRefreshFields() {
    return getDocumentFlags().contains(CDDocument.Flag.RECALC);
  }

  @Override
  public boolean isAnonymousForm() {
    return getDocumentFlags2().contains(CDDocument.Flag2.ANONYMOUS);
  }

  @Override
  public boolean isUseInitialFocus() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOINITIALFOCUS);
  }

  @Override
  public boolean isFocusOnF6() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOFOCUSWHENF6);
  }

  @Override
  public boolean isSignDocuments() {
    return getDocumentFlags3().contains(CDDocument.Flag3.SIGNWHENSAVED);
  }

  @Override
  public boolean isAllowAutosave() {
    return getDocumentFlags3().contains(CDDocument.Flag3.CANAUTOSAVE);
  }

  @Override
  public ConflictBehavior getConflictBehavior() {
    String action = getDocument().getAsText(NotesConstants.ITEM_CONFLICT_ACTION, ' ');
    switch(action) {
    case NotesConstants.CONFLICT_AUTOMERGE:
      return ConflictBehavior.MERGE_CONFLICTS;
    case NotesConstants.CONFLICT_NONE:
      return ConflictBehavior.MERGE_NO_CONFLICTS;
    case NotesConstants.CONFLICT_BEST_MERGE:
      return ConflictBehavior.MERGE_NO_CONFLICTS;
    default:
      return ConflictBehavior.CREATE_CONFLICTS;
    }
  }
}
