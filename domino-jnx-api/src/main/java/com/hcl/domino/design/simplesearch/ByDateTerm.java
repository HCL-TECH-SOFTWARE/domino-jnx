package com.hcl.domino.design.simplesearch;

import java.util.Optional;
import java.util.OptionalInt;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;

/**
 * Represents a search for documents by creation or modification date.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByDateTerm extends SimpleSearchTerm {
  enum DateType {
    CREATED, MODIFIED
  }
  enum Rule {
    ON, AFTER, BEFORE, NOT_ON, IN_LAST, IN_NEXT,
    OLDER_THAN, AFTER_NEXT, BETWEEN, NOT_BETWEEN
  }
  
  /**
   * Determines the date field this term operates on.
   * 
   * @return a {@link DateType} instance
   */
  DateType getDateType();
  
  /**
   * Determines the type of date query this term represents.
   * 
   * @return a {@link Rule} instance
   */
  Rule getRule();
  
  /**
   * Retrieves the date component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link Rule#ON ON}</li>
   *   <li>{@link Rule#AFTER AFTER}</li>
   *   <li>{@link Rule#BEFORE BEFORE}</li>
   *   <li>{@link Rule#NOT_ON NOT_ON}</li>
   * </ul>
   * 
   * @return an {@link Optional} describing the date component for the term,
   *         or an empty one if that is not applicable
   */
  Optional<DominoDateTime> getDate();
  
  /**
   * Retrieves the day count for the term, if applicable.
   * 
   * <p>This value applies when {@link #getRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link Rule#IN_LAST IN_LAST}</li>
   *   <li>{@link Rule#IN_NEXT IN_NEXT}</li>
   *   <li>{@link Rule#OLDER_THAN OLDER_THAN}</li>
   *   <li>{@link Rule#AFTER_NEXT AFTER_NEXT}</li>
   * </ul>
   * 
   * @return an {@link OptionalInt} describing the day count for the term,
   *         or an empty one if that is not applicable
   */
  OptionalInt getDayCount();
  
  /**
   * Retrieves the date range component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link Rule#BETWEEN BETWEEN}</li>
   *   <li>{@link Rule#NOT_BETWEEN NOT_BETWEEN}</li>
   * </ul>
   * 
   * @return an {@link Optional} describing the date range component for
   *         the term, or an empty one if that is not applicable
   */
  Optional<DominoDateRange> getDateRange();
}
