package com.hcl.domino.design.simplesearch;

import com.hcl.domino.misc.NotesConstants;

/**
 * Represents a search for documents with a given author.
 * 
 * <p>This is a specialization of {@link ByFieldTerm} that applies to specifically
 * the updated-by list (the {@value NotesConstants#FIELD_UPDATED_BY} item).</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByAuthorTerm extends ByFieldTerm {
}
