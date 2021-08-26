package com.hcl.domino.commons.design;

import java.util.List;
import java.util.Optional;

import com.hcl.domino.design.DesignElement.ReadersRestrictedElement;
import com.hcl.domino.misc.NotesConstants;

public interface IDefaultReadersRestrictedElement extends ReadersRestrictedElement {
  @Override
  default Optional<List<String>> getReaders() {
    return getDocument().getAsListOptional(NotesConstants.DESIGN_READERS, String.class);
  }
}
