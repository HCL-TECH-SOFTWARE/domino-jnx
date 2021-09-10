package com.hcl.domino.design.simplesearch;

import java.util.Optional;
import java.util.OptionalDouble;

import com.hcl.domino.misc.Pair;

/**
 * Represents a search for documents by number fields.
 * 
 * <p>This is a specialization of {@link ByFieldTerm} that applies to number
 * fields. In such cases, {@link ByFieldTerm#getTextValue()} will be set
 * to a text version of the number, but should be ignored in preference to the
 * methods found in this type.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByNumberFieldTerm extends ByFieldTerm {
  enum NumberRule {
    EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, BETWEEN, NOT_BETWEEN
  }
  
  /**
   * Determines the type of number query this term represents.
   * 
   * @return a {@link NumberRule} instance
   */
  NumberRule getNumberRule();
  
  /**
   * Retrieves the number component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getNumberRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link NumberRule#EQUAL EQUAL}</li>
   *   <li>{@link NumberRule#NOT_EQUAL NOT_EQUAL}</li>
   *   <li>{@link NumberRule#GREATER_THAN GREATER_THAN}</li>
   *   <li>{@link NumberRule#LESS_THAN LESS_THAN}</li>
   * </ul>
   * 
   * @return an {@link OptionalDouble} describing the number component for the term,
   *         or an empty one if that is not applicable
   */
  OptionalDouble getNumber();
  
  /**
   * Retrieves the number range component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getNumberRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link NumberRule#BETWEEN BETWEEN}</li>
   *   <li>{@link NumberRule#NOT_BETWEEN NOT_BETWEEN}</li>
   * </ul>
   * 
   * @return an {@link Optional} describing the number range component for
   *         the term as a {@link Pair}, or an empty one if that is not
   *         applicable
   */
  Optional<Pair<Double, Double>> getNumberRange();
}
