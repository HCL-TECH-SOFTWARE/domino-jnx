package com.hcl.domino.commons.design;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
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
  public int getNoteID() {
    return this.noteId;
  }
  
  @Override
  public String getUNID() {
    return this.entry.getUNID();
  }
  
  @Override
  public String getTitle() {
    List<String> titles = getTitles();
    return titles.isEmpty() ? "" : titles.get(0);
  }

  @Override
  public List<String> getTitles() {
    return DesignUtil.toTitlesList(this.entry.getAsList(0, String.class, Collections.emptyList()));
  }

  @Override
  public DocumentClass getDocumentClass() {
    return this.noteClass;
  }

  @Override
  public long getFileSize() {
    return this.entry.get(18, long.class, 0l);
  }

  @Override
  public String getFlagsExt() {
    return this.entry.get(17, String.class, "");
  }

  @Override
  public String getMimeType() {
    return this.entry.get(19, String.class, "");
  }

  @Override
  public DominoDateTime getModified() {
    return this.entry.get(6, DominoDateTime.class, null);
  }

  @Override
  public String getUpdatedBy() {
    return this.entry.get(12, String.class, "");
  }
  
  @Override
  public boolean matchesTitleValue(String title) {
    return DesignUtil.matchesTitleValues(title, getTitles());
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