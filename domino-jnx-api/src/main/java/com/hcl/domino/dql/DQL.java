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
package com.hcl.domino.dql;

import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Formula;

/**
 * Utility class to programmatically compose syntactically correct
 * DQL queries and prevent malicous content injection (we hopefully
 * catch all possible threats like using quote characters in search
 * values).<br>
 * <br>
 * For best code quality, use a static import like this<br>
 * <br>
 * <code>
 * import static com.hcl.domino.dql.DQL.*;
 * </code>
 * <br>
 * <br>
 * to import all static methods of this class.<br>
 * Then you can use methods like {@link #item(String)}, {@link #in(String...)},
 * {@link #inAll(String...)} or {@link #view(String)}
 * directly without the prefix <code>DQL."</code>:<br>
 * <br>
 * <code>
 * DQLTerm dqlQuery = and(<br>
 * &nbsp;&nbsp;item("Lastname").isEqualTo("Abbott"),<br>
 * &nbsp;&nbsp;item("Firstname").isGreaterThan("B")<br>
 * );<br>
 * </code>
 * <br>
 * To see the resulting DQL query string, simple call
 * {@link DQLTerm#toString()}.
 */
public class DQL {
  public static class AllTerm extends DQLTerm {

    @Override
    public String toString() {
      return "@all"; //$NON-NLS-1$
    }
  }

  public static class AndTerm extends DQLTerm {
    private final DQLTerm[] m_terms;
    private String m_toString;

    private AndTerm(final DQLTerm[] terms) {
      if (terms == null) {
        throw new IllegalArgumentException("And arguments value is null");
      }
      if (terms.length == 0) {
        throw new IllegalArgumentException("And arguments value is empty");
      }
      this.m_terms = terms;
    }

    @Override
    public String toString() {
      if (this.m_terms.length == 1) {
        return this.m_terms[0].toString();
      }

      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.m_terms.length; i++) {
          if (i > 0) {
            sb.append(" and "); //$NON-NLS-1$
          }

          if (this.m_terms[i] instanceof OrTerm) {
            sb.append("("); //$NON-NLS-1$
            sb.append(this.m_terms[i].toString());
            sb.append(")"); //$NON-NLS-1$
          } else if (this.m_terms[i] instanceof AndTerm) {
            sb.append(this.m_terms[i].toString());
          } else {
            sb.append(this.m_terms[i]);
          }
        }

        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }
  }

  /**
   * Base class for a variety of DQL search terms
   */
  public static abstract class DQLTerm {

    /**
     * Returns the term content as DQL query
     *
     * @return DQL
     */
    @Override
    public abstract String toString();
  }

  public static class InViewsOrFoldersTerm extends DQLTerm {
    private final String[] m_viewNames;
    private final boolean m_matchAll;

    private String m_toString;

    private InViewsOrFoldersTerm(final String[] viewNames, final boolean matchAll) {
      this.m_viewNames = viewNames;
      this.m_matchAll = matchAll;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();
        sb.append("in "); //$NON-NLS-1$
        if (this.m_matchAll) {
          sb.append("all "); //$NON-NLS-1$
        }
        sb.append("("); //$NON-NLS-1$

        for (int i = 0; i < this.m_viewNames.length; i++) {
          if (i > 0) {
            sb.append(", "); //$NON-NLS-1$
          }
          sb.append("'"); //$NON-NLS-1$
          sb.append(DQL.escapeViewName(this.m_viewNames[i]));
          sb.append("'"); //$NON-NLS-1$
        }
        sb.append(")"); //$NON-NLS-1$
        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }
  }

  public static class NamedItem extends Subject {
    private final String m_itemName;

    private NamedItem(final String itemName) {
      this.m_itemName = itemName;
    }

    public String getName() {
      return this.m_itemName;
    }

    @Override
    public String toString() {
      return DQL.escapeItemName(this.m_itemName);
    }
  }

  public static class NamedView {
    private final String m_viewName;

    private NamedView(final String viewName) {
      this.m_viewName = viewName;
    }

    /**
     * Method to define the column for which we want to
     * filter the value
     *
     * @param columnName view column name
     * @return object to define the column value and relation (e.g. isEqualTo)
     */
    public NamedViewColumn column(final String columnName) {
      return new NamedViewColumn(this, columnName);
    }

    public String getViewName() {
      return this.m_viewName;
    }

    @Override
    public String toString() {
      return this.m_viewName;
    }

  }

  public static class NamedViewColumn extends Subject {
    private final NamedView m_view;
    private final String m_columnName;

    private String m_toString;

    private NamedViewColumn(final NamedView view, final String columnName) {
      this.m_view = view;
      this.m_columnName = columnName;
    }

    public String getColumnName() {
      return this.m_columnName;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        this.m_toString = MessageFormat.format("''{0}''.{1}", DQL.escapeViewName(this.m_view.getViewName()), //$NON-NLS-1$
            DQL.escapeColumnName(this.m_columnName));
      }
      return this.m_toString;
    }
  }

  public static class NoteContainsTerm extends DQLTerm {
    private final boolean m_containsAll;
    private final String[] m_values;
    private String m_toString;

    public NoteContainsTerm(final boolean containsAll, final String... values) {
      this.m_containsAll = containsAll;
      this.m_values = values;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();

        sb.append("contains "); //$NON-NLS-1$

        if (this.m_containsAll) {
          sb.append("all "); //$NON-NLS-1$
        }

        sb.append("("); //$NON-NLS-1$

        for (int i = 0; i < this.m_values.length; i++) {
          if (i > 0) {
            sb.append(", "); //$NON-NLS-1$
          }
          sb.append("'"); //$NON-NLS-1$
          sb.append(DQL.escapeStringValue(this.m_values[i]));
          sb.append("'"); //$NON-NLS-1$
        }

        sb.append(")"); //$NON-NLS-1$
        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }
  }

  public static class NotTerm extends DQLTerm {
    private final DQLTerm m_term;

    private String m_toString;

    private NotTerm(final DQLTerm term) {
      this.m_term = term;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        if (this.m_term instanceof AndTerm || this.m_term instanceof OrTerm) {
          this.m_toString = MessageFormat.format("not ({0})", this.m_term.toString()); //$NON-NLS-1$
        } else {
          this.m_toString = MessageFormat.format("not {0}", this.m_term.toString()); //$NON-NLS-1$
        }
      }
      return this.m_toString;
    }
  }

  public static class OrTerm extends DQLTerm {
    private final DQLTerm[] m_terms;
    private String m_toString;

    private OrTerm(final DQLTerm[] terms) {
      if (terms == null) {
        throw new IllegalArgumentException("Or arguments value is null");
      }
      if (terms.length == 0) {
        throw new IllegalArgumentException("Or arguments value is empty");
      }
      this.m_terms = terms;
    }

    @Override
    public String toString() {
      if (this.m_terms.length == 1) {
        return this.m_terms[0].toString();
      }

      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.m_terms.length; i++) {
          if (i > 0) {
            sb.append(" or "); //$NON-NLS-1$
          }

          if (this.m_terms[i] instanceof OrTerm) {
            sb.append(this.m_terms[i].toString());
          } else if (this.m_terms[i] instanceof AndTerm) {
            // sb.append("(");
            sb.append(this.m_terms[i].toString());
            // sb.append(")");
          } else {
            sb.append(this.m_terms[i]);
          }
        }
        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }

  }

  /**
   * @since 1.1.2
   */
  public static class FormulaTerm extends DQLTerm {
    private final String m_formula;

    private String m_toString;

    private FormulaTerm(final String formula) {
      if(formula == null || formula.isEmpty()) {
        throw new IllegalArgumentException("Formula expression cannot be empty");
      }
      this.m_formula = formula;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        String escapedFormula = m_formula.replace("'", "\\'"); //$NON-NLS-1$ //$NON-NLS-2$
        this.m_toString = MessageFormat.format("@formula(''{0}'')", escapedFormula); //$NON-NLS-1$
      }
      return this.m_toString;
    }
  }

  public static class SpecialValue extends NamedItem {
    private final SpecialValueType m_type;

    private SpecialValue(final SpecialValueType type) {
      super(type.getValue());
      this.m_type = type;
    }

    public SpecialValueType getType() {
      return this.m_type;
    }

    @Override
    public String toString() {
      return this.m_type.getValue();
    }
  }

  private enum SpecialValueType {
    MODIFIEDINTHISFLE("@ModifiedInThisFile"), //$NON-NLS-1$
    DOCUMENTUNIQUEID("@DocumentUniqueID"), //$NON-NLS-1$
    CREATED("@Created"); //$NON-NLS-1$

    private final String m_value;

    SpecialValueType(final String value) {
      this.m_value = value;
    }

    public String getValue() {
      return this.m_value;
    }
  }

  private static class Subject {
    /**
     * Returns a DQL term to run a FT search on the value of an item with one or
     * multiple search words.
     *
     * @param searchWords search words with optional wildcards like Lehm* or Lehman?
     * @return term
     * @since Domino R11
     */
    public ValueContainsTerm contains(final String... searchWords) {
      return new ValueContainsTerm(this, false, searchWords);
    }

    /**
     * Returns a DQL term to run a FT search on the value of an item with one or
     * multiple search words.<br>
     * All search words must exist in the note for it to be a match.
     *
     * @param searchWords search words with optional wildcards like Lehm* or Lehman?
     * @return term
     * @since Domino R11
     */
    public ValueContainsTerm containsAll(final String... searchWords) {
      return new ValueContainsTerm(this, true, searchWords);
    }

    public <T> ValueComparisonTerm in(final Collection<T> values, final Class<T> clazz) {
      if (Integer.class == clazz) {
        final int[] valuesArr = new int[values.size()];
        int idx = 0;
        for (final T currVal : values) {
          valuesArr[idx++] = ((Number) currVal).intValue();
        }
        return this.in(valuesArr);
      } else if (Double.class == clazz) {
        final double[] valuesArr = new double[values.size()];
        int idx = 0;
        for (final T currVal : values) {
          valuesArr[idx++] = ((Number) currVal).doubleValue();
        }
        return this.in(valuesArr);
      } else if (String.class == clazz) {
        final String[] valuesArr = new String[values.size()];
        int idx = 0;
        for (final T currVal : values) {
          valuesArr[idx++] = (String) currVal;
        }
        return this.in(valuesArr);
      } else {
        throw new IllegalArgumentException(
            MessageFormat.format("Unsupported class type: {0}. Try Integer, Double or String.", clazz.getName()));
      }
    }

    public ValueComparisonTerm in(final double... dblValues) {
      Objects.requireNonNull(dblValues, "Values list cannot be null");
      if (dblValues.length == 0) {
        throw new IllegalArgumentException("Values list cannot be empty");
      }

      return new ValueComparisonTerm(this, TermRelation.IN, dblValues);
    }

    public ValueComparisonTerm in(final int... intValues) {
      Objects.requireNonNull(intValues, "Values list cannot be null");
      if (intValues.length == 0) {
        throw new IllegalArgumentException("Values list cannot be empty");
      }

      return new ValueComparisonTerm(this, TermRelation.IN, intValues);
    }

    public ValueComparisonTerm in(final String... strValues) {
      Objects.requireNonNull(strValues, "Values list cannot be null");
      if (strValues.length == 0) {
        throw new IllegalArgumentException("Values list cannot be empty");
      }

      return new ValueComparisonTerm(this, TermRelation.IN, strValues);
    }

    public ValueComparisonTerm isEqualTo(final Date dtVal) {
      return new ValueComparisonTerm(this, TermRelation.EQUAL, dtVal);
    }

    public ValueComparisonTerm isEqualTo(final double numVal) {
      return new ValueComparisonTerm(this, TermRelation.EQUAL, Double.valueOf(numVal));
    }

    public ValueComparisonTerm isEqualTo(final int numVal) {
      return new ValueComparisonTerm(this, TermRelation.EQUAL, Integer.valueOf(numVal));
    }

    public ValueComparisonTerm isEqualTo(final String strVal) {
      return new ValueComparisonTerm(this, TermRelation.EQUAL, strVal);
    }

    public ValueComparisonTerm isEqualTo(final TemporalAccessor tdVal) {
      return new ValueComparisonTerm(this, TermRelation.EQUAL, tdVal);
    }

    public ValueComparisonTerm isGreaterThan(final Date dtVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, dtVal);
    }

    public ValueComparisonTerm isGreaterThan(final double numVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, Double.valueOf(numVal));
    }

    public ValueComparisonTerm isGreaterThan(final int numVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, Integer.valueOf(numVal));
    }

    public ValueComparisonTerm isGreaterThan(final String strVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, strVal);
    }

    public ValueComparisonTerm isGreaterThan(final TemporalAccessor tdVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, tdVal);
    }

    public ValueComparisonTerm isGreaterThanOrEqual(final Date dtVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, dtVal);
    }

    public ValueComparisonTerm isGreaterThanOrEqual(final double numVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, Double.valueOf(numVal));
    }

    public ValueComparisonTerm isGreaterThanOrEqual(final int numVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, Integer.valueOf(numVal));
    }

    public ValueComparisonTerm isGreaterThanOrEqual(final String strVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, strVal);
    }

    public ValueComparisonTerm isGreaterThanOrEqual(final TemporalAccessor tdVal) {
      return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, tdVal);
    }

    public ValueComparisonTerm isLessThan(final Date dtVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHAN, dtVal);
    }

    public ValueComparisonTerm isLessThan(final double numVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHAN, Double.valueOf(numVal));
    }

    public ValueComparisonTerm isLessThan(final int numVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHAN, Integer.valueOf(numVal));
    }

    public ValueComparisonTerm isLessThan(final String strVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHAN, strVal);
    }

    public ValueComparisonTerm isLessThan(final TemporalAccessor tdVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHAN, tdVal);
    }

    public ValueComparisonTerm isLessThanOrEqual(final Date dtVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, dtVal);
    }

    public ValueComparisonTerm isLessThanOrEqual(final double numVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, Double.valueOf(numVal));
    }

    public ValueComparisonTerm isLessThanOrEqual(final int numVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, Integer.valueOf(numVal));
    }

    public ValueComparisonTerm isLessThanOrEqual(final String strVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, strVal);
    }

    public ValueComparisonTerm isLessThanOrEqual(final TemporalAccessor tdVal) {
      return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, tdVal);
    }

  }

  private enum TermRelation {
    EQUAL("="), //$NON-NLS-1$
    LESSTHAN("<"), //$NON-NLS-1$
    LESSTHANOREQUAL("<="), //$NON-NLS-1$
    GREATERTHAN(">"), //$NON-NLS-1$
    GREATERTHANOREQUAL(">="), //$NON-NLS-1$
    IN("in"), //$NON-NLS-1$
    INALL("in all"), //$NON-NLS-1$
    CONTAINS("contains"); //$NON-NLS-1$

    private final String m_val;

    TermRelation(final String val) {
      this.m_val = val;
    }

    public String getValue() {
      return this.m_val;
    }
  }

  public static class ValueComparisonTerm extends DQLTerm {
    private final Subject m_subject;
    private final TermRelation m_relation;
    private final Object m_value;

    private String m_toString;

    private ValueComparisonTerm(final Subject item, final TermRelation relation, final Object value) {
      this.m_subject = item;
      this.m_relation = relation;
      this.m_value = value;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.m_subject.toString());

        sb.append(" "); //$NON-NLS-1$
        sb.append(this.m_relation.getValue());
        sb.append(" "); //$NON-NLS-1$

        if (this.m_relation == TermRelation.IN) {
          sb.append("("); //$NON-NLS-1$
        }

        if (this.m_value instanceof String[]) {
          final String[] strValues = (String[]) this.m_value;
          for (int i = 0; i < strValues.length; i++) {
            if (i > 0) {
              sb.append(", "); //$NON-NLS-1$
            }
            sb.append("'"); //$NON-NLS-1$
            sb.append(DQL.escapeStringValue(strValues[i]));
            sb.append("'"); //$NON-NLS-1$
          }
        } else if (this.m_value instanceof int[]) {
          final int[] intValues = (int[]) this.m_value;

          for (int i = 0; i < intValues.length; i++) {
            if (i > 0) {
              sb.append(", "); //$NON-NLS-1$
            }
            sb.append(Integer.toString(intValues[i]));
          }
        } else if (this.m_value instanceof double[]) {
          final double[] dblValues = (double[]) this.m_value;

          for (int i = 0; i < dblValues.length; i++) {
            if (i > 0) {
              sb.append(", "); //$NON-NLS-1$
            }
            sb.append(DQL.formatDoubleValue(dblValues[i]));
          }
        } else if (this.m_value instanceof Date[]) {
          final Date[] dateValues = (Date[]) this.m_value;

          for (int i = 0; i < dateValues.length; i++) {
            if (i > 0) {
              sb.append(", "); //$NON-NLS-1$
            }
            sb.append(DQL.formatDateValue(dateValues[i]));
          }
        } else if (this.m_value instanceof DominoDateTime[]) {
          final DominoDateTime[] tdValues = (DominoDateTime[]) this.m_value;

          for (int i = 0; i < tdValues.length; i++) {
            if (i > 0) {
              sb.append(", "); //$NON-NLS-1$
            }
            sb.append(DQL.formatDominoDateTimeValue(tdValues[i]));
          }
        } else if (this.m_value instanceof String) {
          sb
              .append("'") //$NON-NLS-1$
              .append(DQL.escapeStringValue((String) this.m_value))
              .append("'"); //$NON-NLS-1$
        } else if (this.m_value instanceof Integer) {
          sb.append(this.m_value);
        } else if (this.m_value instanceof Double) {
          sb.append(DQL.formatDoubleValue((Double) this.m_value));
        } else if (this.m_value instanceof Date) {
          sb.append(DQL.formatDateValue((Date) this.m_value));
        } else if (this.m_value instanceof DominoDateTime) {
          sb.append(DQL.formatDominoDateTimeValue((DominoDateTime) this.m_value));
        } else if(this.m_value instanceof TemporalAccessor) {
          sb.append(DQL.formatTemporalValue((TemporalAccessor) this.m_value));
        } else {
          throw new IllegalArgumentException(MessageFormat.format("Unknown value found: {0} (type={1})", this.m_value,
              this.m_value == null ? "null" : this.m_value.getClass().getName())); //$NON-NLS-1$
        }

        if (this.m_relation == TermRelation.IN) {
          sb.append(")"); //$NON-NLS-1$
        }

        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }
  }

  public static class ValueContainsTerm extends DQLTerm {
    private final Subject m_subject;
    private final boolean m_containsAll;
    private final String[] m_values;

    private String m_toString;

    private ValueContainsTerm(final Subject item, final boolean containsAll, final String[] values) {
      this.m_subject = item;
      this.m_containsAll = containsAll;
      this.m_values = values;
    }

    @Override
    public String toString() {
      if (this.m_toString == null) {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.m_subject.toString());

        sb.append(" contains "); //$NON-NLS-1$

        if (this.m_containsAll) {
          sb.append("all "); //$NON-NLS-1$
        }

        sb.append("("); //$NON-NLS-1$

        for (int i = 0; i < this.m_values.length; i++) {
          if (i > 0) {
            sb.append(", "); //$NON-NLS-1$
          }
          sb.append("'"); //$NON-NLS-1$
          sb.append(DQL.escapeStringValue(this.m_values[i]));
          sb.append("'"); //$NON-NLS-1$
        }

        sb.append(")"); //$NON-NLS-1$

        this.m_toString = sb.toString();
      }
      return this.m_toString;
    }
  }

  /**
   * Returns a DQL term that matches all documents
   *
   * @return term
   */
  public static DQLTerm all() {
    return new AllTerm();
  }

  /**
   * Returns a DQL term to do an AND operation on multiple other terms
   *
   * @param terms terms for AND operation
   * @return AND term
   */
  public static DQLTerm and(final DQLTerm... terms) {
    return new AndTerm(terms);
  }

  /**
   * Returns a DQL term to run a FT search on the whole note with one or multiple
   * search words.
   *
   * @param values search words with optional wildcards like Lehm* or Lehman?
   * @return term
   * @since Domino R11
   */
  public static DQLTerm contains(final String... values) {
    return new NoteContainsTerm(false, values);
  }

  /**
   * Returns a DQL term to run a FT search on the whole note with one or multiple
   * search words.<br>
   * All search words must exist in the note for it to be a match.
   *
   * @param values search words with optional wildcards like Lehm* or Lehman?
   * @return term
   * @since Domino R11
   */
  public static DQLTerm containsAll(final String... values) {
    return new NoteContainsTerm(true, values);
  }

  /**
   * Use this method to filter by @Created value
   *
   * @return object to define the creation date to compare with and relation (e.g.
   *         isGreaterThan)
   */
  public static SpecialValue created() {
    return new SpecialValue(SpecialValueType.CREATED);
  }

  /**
   * Use this method to filter by @DocumentUniqueId value
   *
   * @return object to define the UNID and isEqual relation
   */
  public static SpecialValue documentUniqueId() {
    return new SpecialValue(SpecialValueType.DOCUMENTUNIQUEID);
  }

  private static String escapeColumnName(final String columnName) {
    if (columnName.contains(" ")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected whitespace found in view name: {0}", columnName));
    }
    if (columnName.contains("'") || columnName.contains("\"")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected quote character in view name: {0}", columnName));
    }
    return columnName.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static String escapeItemName(final String itemName) {
    if (itemName.contains(" ")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected whitespace found in item name: {0}", itemName));
    }
    if (itemName.contains("'") || itemName.contains("\"")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected quote character in item name: {0}", itemName));
    }

    return itemName;
  }

  private static String escapeStringValue(final String strVal) {
    if (strVal.contains("\n")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected newline character in string value: {0}", strVal));
    }

    return strVal
        .replace("'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static String escapeViewName(final String viewName) {
    if (viewName.contains("'") || viewName.contains("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
      throw new IllegalArgumentException(MessageFormat.format("Unexpected quote character in view name: {0}", viewName));
    }
    return viewName.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static String formatDateValue(final Date dateValue) {
    //Domino can only store hundredth of a second
    long dateValueMS = dateValue.getTime();
    long millis = dateValue.getTime() % 1000;
    long millisRounded = 10 * (millis / 10);
    dateValueMS -= (millis-millisRounded);

    return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateValueMS), ZoneId.of("UTC")))); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static String formatDominoDateTimeValue(final DominoDateTime tdValue) {
    if (tdValue.hasDate()) {
      if (tdValue.hasTime()) {
        return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_DATE_TIME.format(tdValue.toOffsetDateTime())); //$NON-NLS-1$
      } else {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(tdValue.toOffsetDateTime().toInstant());
      }
    } else {
      if (tdValue.hasTime()) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(tdValue.toOffsetDateTime().toInstant());
      } else {
        throw new IllegalArgumentException("DominoDateTime has no date and no time");
      }
    }
  }

  private static String formatTemporalValue(final TemporalAccessor temporalValue) {
    // TODO see if there's a more-efficient way to identify the type
    try {
      OffsetDateTime dt = OffsetDateTime.from(temporalValue);
      LocalDate localDate = dt.toLocalDate();
      LocalTime localTime = toHundredths(dt.toLocalTime());
      OffsetDateTime rounded = OffsetDateTime.of(localDate, localTime, dt.getOffset());
      return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(rounded)); //$NON-NLS-1$
    } catch(DateTimeException e) {
      // Next
    }
    try {
      LocalDate dt = LocalDate.from(temporalValue);
      return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_LOCAL_DATE.format(dt)); //$NON-NLS-1$
    } catch(DateTimeException e) {
      // Next
    }
    try {
      LocalTime dt = LocalTime.from(temporalValue);
      LocalTime hundredths = toHundredths(dt);
      return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_LOCAL_TIME.format(hundredths)); //$NON-NLS-1$
    } catch(DateTimeException e) {
      // Next
    }
    Instant instant = Instant.from(temporalValue);
    return formatDateValue(Date.from(instant));
  }
  
  private static LocalTime toHundredths(LocalTime localTime) {
    int millis = localTime.getNano() / 1000 / 1000;
    millis = millis / 10 * 10;
    return LocalTime.of(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), millis * 1000 * 1000);
  }

  private static String formatDoubleValue(final double dblValue) {
    return Double.toString(dblValue);
  }

  /**
   * Creates a search term to find documents that exist in at least
   * one of the specified views.
   *
   * @param views view names (V10.0 does not support alias names)
   * @return DQL term
   */
  public static InViewsOrFoldersTerm in(final String... views) {
    return new InViewsOrFoldersTerm(views, false);
  }

  /**
   * Creates a search term to find documents that exist in multiple
   * views
   *
   * @param views view names (V10.0 does not support alias names)
   * @return DQL term
   */
  public static InViewsOrFoldersTerm inAll(final String... views) {
    return new InViewsOrFoldersTerm(views, true);
  }

  /**
   * Use this method to filter for documents with a specific item
   * value
   *
   * @param itemName item name
   * @return object to define the item value and relation (e.g. isEqual to)
   */
  public static NamedItem item(final String itemName) {
    return new NamedItem(itemName);
  }

  /**
   * Use this method to filter by @ModifiedInThisFile value
   *
   * @return object to define a date value and relation (e.g. isGreaterThan /
   *         isLess)
   */
  public static SpecialValue modifiedInThisFile() {
    return new SpecialValue(SpecialValueType.MODIFIEDINTHISFLE);
  }

  /**
   * Returns a DQL term to negate the specified term
   *
   * @param term term to negate
   * @return negated term
   */
  public static DQLTerm not(final DQLTerm term) {
    return new NotTerm(term);
  }

  /**
   * Returns a DQL term to do an OR operation on multiple other terms
   *
   * @param terms terms for OR operation
   * @return OR term
   */
  public static DQLTerm or(final DQLTerm... terms) {
    return new OrTerm(terms);
  }

  /**
   * Use this method to filter for documents with a specific view
   * column value
   *
   * @param viewName view name
   * @return object to define which column to filter
   */
  public static NamedView view(final String viewName) {
    return new NamedView(viewName);
  }
  
  /**
   * Use this method to filter documents based on a formula-language
   * expression
   * 
   * @param formula the formula expression to use
   * @return term representing the formula expression
   * @throws IllegalArgumentException if {@code formula} is empty
   * @since 1.1.2
   */
  public static FormulaTerm formula(String formula) {
    return new FormulaTerm(formula);
  }
  
  /**
   * Use this method to filter documents based on a formula-language
   * expression
   * 
   * @param formula the {@link Formula} objects to use
   * @return term representing the formula expression
   * @throws NullPointerException if {@code formula} is {@code null}
   * @since 1.1.2
   */
  public static FormulaTerm formula(Formula formula) {
    Objects.requireNonNull(formula, "formula cannot be null");
    return new FormulaTerm(formula.getFormula());
  }

}
