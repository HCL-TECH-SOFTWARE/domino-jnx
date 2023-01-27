package com.hcl.domino.commons.design;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.DesignEntry;

public class DesignEntryImpl<T extends DesignElement> implements DesignEntry<T> {
  private final int noteId;
  private final DocumentClass noteClass;
  private final CollectionEntry entry;

  public DesignEntryImpl(final int noteId, final DocumentClass noteClass, final CollectionEntry entry) {
    this.noteId = noteId;
    this.noteClass = noteClass;
    this.entry = entry;
  }

  @Override
  public String getComment() {
    return this.entry.get(7, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public String getFlags() {
    return this.entry.get(4, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public String getLanguage() {
    return this.entry.get(14, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public int getNoteId() {
    return this.noteId;
  }

  @Override
  public List<String> getTitles() {
    return DesignUtil.toTitlesList(this.entry.getAsList(0, String.class, Collections.emptyList()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public T toDesignElement(final Database database) {
    return (T)DesignUtil.createDesignElement(database, this.noteId, this.noteClass, this.getFlags(), Optional.empty());
  }

  @Override
  public String toString() {
    return String.format("DesignEntry [noteId=%s, noteClass=%s, entry=%s]", this.noteId, this.noteClass, this.entry); //$NON-NLS-1$
  }
}