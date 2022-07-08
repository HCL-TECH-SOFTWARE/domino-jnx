package com.hcl.domino.commons.dql;

import static java.text.MessageFormat.format;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.hcl.domino.commons.dql.parser.DQLLexer;
import com.hcl.domino.commons.dql.parser.DQLListener;
import com.hcl.domino.commons.dql.parser.DQLParser;
import com.hcl.domino.commons.dql.parser.DQLParser.AtfunctionContext;
import com.hcl.domino.commons.dql.parser.DQLParser.BooleanContext;
import com.hcl.domino.commons.dql.parser.DQLParser.DatetimeContext;
import com.hcl.domino.commons.dql.parser.DQLParser.EscapedstringContext;
import com.hcl.domino.commons.dql.parser.DQLParser.FieldnameContext;
import com.hcl.domino.commons.dql.parser.DQLParser.IdentifierContext;
import com.hcl.domino.commons.dql.parser.DQLParser.NumberContext;
import com.hcl.domino.commons.dql.parser.DQLParser.Operator_with_valueContext;
import com.hcl.domino.commons.dql.parser.DQLParser.StartContext;
import com.hcl.domino.commons.dql.parser.DQLParser.SubstitutionvarContext;
import com.hcl.domino.commons.dql.parser.DQLParser.TermContext;
import com.hcl.domino.commons.dql.parser.DQLParser.ValueContext;
import com.hcl.domino.commons.dql.parser.DQLParser.ViewandcolumnnameContext;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLExpressionParser;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.dql.DQL.NamedItem;
import com.hcl.domino.dql.DQL.NamedViewColumn;
import com.hcl.domino.dql.DQL.SpecialValue;
import com.hcl.domino.dql.DQL.SpecialValueType;

public class DQLExpressionParserImpl implements DQLExpressionParser {

  @Override
  public DQLTerm parseDQL(String dql) throws IllegalArgumentException {
    DQLLexer dqlLexer = new DQLLexer(CharStreams.fromString(dql));

    CommonTokenStream tokens = new CommonTokenStream(dqlLexer);
    DQLParser parser = new DQLParser(tokens);
    ParseTree tree = parser.start();

    DQLTerm dqlTerm = null;
    if (tree instanceof StartContext) {
      dqlTerm = toTerm((StartContext)tree)
          .orElseThrow(() -> {
            return new IllegalArgumentException(format("Unable to parse StartContext to DQL term: {0}", tree));
          });
    }
    else if (tree instanceof TermContext) {
      dqlTerm = toTerm((StartContext)tree)
          .orElseThrow(() -> {
            return new IllegalArgumentException(format("Unable to parse TermContext to DQL term: {0}", tree));
          });
    }
    else {
      throw new IllegalArgumentException(format("Unable to part TermContext to DQL term: {0}", tree));
    }
    
    return dqlTerm;
  }

  private enum BooleanOp { and, or, andNot, orNot }

  private List<ParseTree> childrenNoEOF(ParseTree parent) {
    List<ParseTree> list = new ArrayList<>();
    
    int n = parent.getChildCount();
    for (int i=0; i<n; i++) {
     list.add(parent.getChild(i));
    }
    
    if (!list.isEmpty()) {
      if (parent instanceof StartContext) {
        if (list.get(list.size()-1) instanceof TerminalNode && "<EOF>".equals(list.get(list.size()-1).getText())) {
          list.remove(list.size()-1);
        }        
      }
    }

    if (!list.isEmpty()) {
      if (list.get(0) instanceof TerminalNode && "(".equals(list.get(0).getText())) {
        list.remove(0);
      }
    }
    
    if (!list.isEmpty()) {
      if (list.get(list.size()-1) instanceof TerminalNode && ")".equals(list.get(list.size()-1).getText())) {
        list.remove(list.size()-1);
      }
    }

    return list;
  }
  
  /**
   * Starting point to traverse the tree
   * 
   * @param startCtx start context
   * @return DQL term if found
   */
  private Optional<DQLTerm> toTerm(StartContext startCtx) {
    List<ParseTree> childrenNoEOF = childrenNoEOF(startCtx);
    
    if (childrenNoEOF.size()==1 && startCtx.getChild(0) instanceof TermContext) {
      return toTerm((TermContext) startCtx.getChild(0));
    }
    
    return toTermWithBooleanOps(startCtx);
  }

  private Optional<DQLTerm> toTerm(TermContext termCtx) {
    List<ParseTree> childrenNoEOF = childrenNoEOF(termCtx);
    if (childrenNoEOF.size()==1 && termCtx.getChild(0) instanceof TermContext) {
      return toTerm((TermContext) termCtx.getChild(0));
    }
    
    boolean hasBooleanCtx = termCtx.children.stream().anyMatch(BooleanContext.class::isInstance);

    if (hasBooleanCtx) {
      return toTermWithBooleanOps(termCtx);
    }
    else {
      Optional<DQLTerm> dqlTerm = toTermIdentifierEqualsValue(termCtx);
      
      return dqlTerm;
    }
  }
  
  private Optional<DQLTerm> toTermWithBooleanOps(ParseTree termCtx) {
    List<ParseTree> childrenNoEOF = childrenNoEOF(termCtx);
    
    if (childrenNoEOF.isEmpty()) {
      return Optional.empty();
    }
    else if (childrenNoEOF.size()==1 && childrenNoEOF.get(0) instanceof TermContext) {
      return toTerm((TermContext)childrenNoEOF.get(0));
    }
    
    boolean hasBooleanCtx = childrenNoEOF.stream().anyMatch(BooleanContext.class::isInstance);
    
    if (hasBooleanCtx) {
      //multiple DQL terms separated by and/or/and not/or not

      BooleanOp booleanOp = null;
      List<DQLTerm> nestedTerms = new ArrayList<>();

      AtomicReference<ParseTree> currChildRef = new AtomicReference<>();
      
      for (ParseTree currChild : childrenNoEOF) {
        currChildRef.set(currChild);
        
        if (currChild instanceof TermContext) {
          DQLTerm nestedTerm = toTerm((TermContext) currChild)
              .orElseThrow(() -> {
                return new IllegalArgumentException(format("Unable to parse TermContext to a DQLTerm: {0}", currChildRef.get()));
              });

          nestedTerms.add(nestedTerm);
        }
        else if (currChild instanceof BooleanContext) {
          BooleanOp currBooleanOp = toBooleanOp((BooleanContext) currChild)
              .orElseThrow(() -> {
                return new IllegalArgumentException(format("Unable to parse BooleanContext to a DQL boolean operator: {0}", currChildRef.get()));
              });

          if (booleanOp!=null && booleanOp!=currBooleanOp) {
            throw new IllegalArgumentException(format("DQL boolean operator cannot be mixed: {0}!={1}", booleanOp, currBooleanOp));
          }
          booleanOp = currBooleanOp;
        }
      }

      if (booleanOp==null) {
        throw new IllegalArgumentException(format("Unable to find DQL boolean operator", termCtx));
      }

      if (nestedTerms.isEmpty()) {
        throw new IllegalArgumentException(format("No nested DQL terms found", termCtx));
      }

      switch (booleanOp) {
      case and:
        return Optional.of(DQL.and(nestedTerms.toArray(new DQLTerm[nestedTerms.size()])));
      case or:
        return Optional.of(DQL.or(nestedTerms.toArray(new DQLTerm[nestedTerms.size()])));
      case andNot: {
        List<DQLTerm> notTerms = nestedTerms
            .stream()
            .map(DQL::not)
            .collect(Collectors.toList());

        return Optional.of(DQL.and(notTerms.toArray(new DQLTerm[notTerms.size()])));
      }
      case orNot: {
        List<DQLTerm> notTerms = nestedTerms
            .stream()
            .map(DQL::not)
            .collect(Collectors.toList());
        return Optional.of(DQL.or(notTerms.toArray(new DQLTerm[notTerms.size()])));
      }
      }
    }

    return Optional.empty();
  }

  private Optional<DQLTerm> toTermIdentifierEqualsValue(TermContext termCtx) {
    List<ParseTree> childrenNoEOF = childrenNoEOF(termCtx);
    
    if (childrenNoEOF.size()!=2) {
      return Optional.empty();
    }

    NamedItem namedItem = null;
    NamedViewColumn namedViewColumn = null;
    // missing @fl('xyz') > 0
    SpecialValue specialValue = null;
    
    
    if (childrenNoEOF.get(0) instanceof IdentifierContext) {
      IdentifierContext identifierCtx = (IdentifierContext) childrenNoEOF.get(0);
      
      if (identifierCtx.getChildCount()==1 && identifierCtx.getChild(0) instanceof FieldnameContext) {
        FieldnameContext fieldNameCtx = (FieldnameContext) identifierCtx.getChild(0);
        if (fieldNameCtx.getChildCount()==1 && fieldNameCtx.getChild(0) instanceof TerminalNode) {
          namedItem = DQL.item(fieldNameCtx.getChild(0).getText());
        }
      }
      else if(identifierCtx.getChildCount()==1 && identifierCtx.getChild(0) instanceof ViewandcolumnnameContext) {
        ViewandcolumnnameContext viewAndColCtx = (ViewandcolumnnameContext) identifierCtx.getChild(0);
        
      }
      else if(identifierCtx.getChildCount()==1 && identifierCtx.getChild(0) instanceof AtfunctionContext) {
        AtfunctionContext atFctCtx = (AtfunctionContext) identifierCtx.getChild(0);
        String atFctName = atFctCtx.getChild(0).getText();
        if ("@documentuniqueid".equalsIgnoreCase(atFctName)) {
          specialValue = DQL.documentUniqueId();
        }
        else if ("@created".equalsIgnoreCase(atFctName)) {
          specialValue = DQL.created();
        }
        else if ("@modifiedinthisfile".equalsIgnoreCase(atFctName)) {
          specialValue = DQL.modifiedInThisFile();
        }
        else {
          throw new IllegalArgumentException(format("Unknown @function in DQL expression: ", atFctName));
        }
      }
    }
    
    String opStr = null;
    if (childrenNoEOF.get(1) instanceof Operator_with_valueContext) {
      Operator_with_valueContext opWithValue = (Operator_with_valueContext) childrenNoEOF.get(1);
      if (opWithValue.getChildCount()>0) {
        opStr = opWithValue.getChild(0).getText();
      }
    }
    
    Number numberVal = null;
    String isoDateTimeStr = null;
    String escapedString = null;
    String substitutionVar = null;
    
    if (childrenNoEOF.get(1) instanceof Operator_with_valueContext) {
      Operator_with_valueContext opWithValue = (Operator_with_valueContext) childrenNoEOF.get(1);
      if (opWithValue.getChildCount()==2) {
       
        if (opWithValue.getChild(0) instanceof TerminalNode) {
          opStr = opWithValue.getChild(0).getText();
        }
        
        if (opStr!=null) {
          if (opWithValue.getChild(1) instanceof ValueContext) {
            ValueContext valueCtx = (ValueContext) opWithValue.getChild(1);
            if (valueCtx.getChildCount()==1) {
              if (valueCtx.getChild(0) instanceof NumberContext) {
                numberVal = Double.parseDouble(valueCtx.getChild(0).getText());
              }
              else if (valueCtx.getChild(0) instanceof DatetimeContext) {
                isoDateTimeStr = valueCtx.getChild(0).getText();
              }
              else if (valueCtx.getChild(0) instanceof EscapedstringContext) {
                escapedString = valueCtx.getChild(0).getText();
              }
              else if (valueCtx.getChild(0) instanceof SubstitutionvarContext) {
                substitutionVar = valueCtx.getChild(0).getText();
              }
            }
          }
        }
      }
    }
    
    if (namedItem!=null) {
      
      if (numberVal!=null) {
        if ("=".equals(opStr)) {
          return Optional.of(namedItem.isEqualTo(numberVal.doubleValue()));
        }
        else if (">".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThan(numberVal.doubleValue()));
        }
        else if (">=".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThanOrEqual(numberVal.doubleValue()));
        }
        else if ("<".equals(opStr)) {
          return Optional.of(namedItem.isLessThan(numberVal.doubleValue()));
        }
        else if ("<=".equals(opStr)) {
          return Optional.of(namedItem.isLessThanOrEqual(numberVal.doubleValue()));
        }
      }
      
      if (escapedString!=null) {
        if ("=".equals(opStr)) {
          return Optional.of(namedItem.isEqualTo(unescapeString(escapedString)));
        }
        else if (">".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThan(unescapeString(escapedString)));
        }
        else if (">=".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThanOrEqual(unescapeString(escapedString)));
        }
        else if ("<".equals(opStr)) {
          return Optional.of(namedItem.isLessThan(unescapeString(escapedString)));
        }
        else if ("<=".equals(opStr)) {
          return Optional.of(namedItem.isLessThanOrEqual(unescapeString(escapedString)));
        }
      }

      if (isoDateTimeStr!=null) {
        if (isoDateTimeStr.startsWith("@dt('")) {
          isoDateTimeStr = isoDateTimeStr.substring(5);
        }
        if (isoDateTimeStr.endsWith("')")) {
          isoDateTimeStr = isoDateTimeStr.substring(0, isoDateTimeStr.length()-2);
        }
        
        DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart() //time is optional
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter();

        TemporalAccessor dt = FMT.parseBest(isoDateTimeStr, LocalDateTime::from, LocalDate::from);        

        if ("=".equals(opStr)) {
          return Optional.of(namedItem.isEqualTo(dt));
        }
        else if (">".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThan(dt));
        }
        else if (">=".equals(opStr)) {
          return Optional.of(namedItem.isGreaterThanOrEqual(dt));
        }
        else if ("<".equals(opStr)) {
          return Optional.of(namedItem.isLessThan(dt));
        }
        else if ("<=".equals(opStr)) {
          return Optional.of(namedItem.isLessThanOrEqual(dt));
        }
      }

      if (substitutionVar!=null) {
        throw new IllegalArgumentException("Substitution variables in DQL are currently unsupported by the parser/JNX DQL API");
      }
    }

    if (specialValue!=null) {
      if (specialValue.getType()==SpecialValueType.DOCUMENTUNIQUEID) {
        if (escapedString!=null) {
          if ("=".equals(opStr)) {
            return Optional.of(specialValue.isEqualTo(unescapeString(escapedString)));
          }
          else if (">".equals(opStr)) {
            return Optional.of(specialValue.isGreaterThan(unescapeString(escapedString)));
          }
          else if (">=".equals(opStr)) {
            return Optional.of(specialValue.isGreaterThanOrEqual(unescapeString(escapedString)));
          }
          else if ("<".equals(opStr)) {
            return Optional.of(specialValue.isLessThan(unescapeString(escapedString)));
          }
          else if ("<=".equals(opStr)) {
            return Optional.of(specialValue.isLessThanOrEqual(unescapeString(escapedString)));
          }
        }
      }
      else if (specialValue.getType()==SpecialValueType.CREATED ||
          specialValue.getType()==SpecialValueType.MODIFIEDINTHISFLE) {
        
        if (isoDateTimeStr!=null) {
          if (isoDateTimeStr.startsWith("@dt('")) {
            isoDateTimeStr = isoDateTimeStr.substring(5);
          }
          if (isoDateTimeStr.endsWith("')")) {
            isoDateTimeStr = isoDateTimeStr.substring(0, isoDateTimeStr.length()-2);
          }
          
          DateTimeFormatter FMT = new DateTimeFormatterBuilder()
              .append(DateTimeFormatter.ISO_LOCAL_DATE)
              .optionalStart() //time is optional
              .appendLiteral('T')
              .append(DateTimeFormatter.ISO_LOCAL_TIME)
              .toFormatter();

          TemporalAccessor dt = FMT.parseBest(isoDateTimeStr, LocalDateTime::from, LocalDate::from);        

          if ("=".equals(opStr)) {
            return Optional.of(specialValue.isEqualTo(dt));
          }
          else if (">".equals(opStr)) {
            return Optional.of(specialValue.isGreaterThan(dt));
          }
          else if (">=".equals(opStr)) {
            return Optional.of(specialValue.isGreaterThanOrEqual(dt));
          }
          else if ("<".equals(opStr)) {
            return Optional.of(specialValue.isLessThan(dt));
          }
          else if ("<=".equals(opStr)) {
            return Optional.of(specialValue.isLessThanOrEqual(dt));
          }

        }
      }
    }

    return Optional.empty();
  }

  //  : EQUAL value  | GREATER value | LESS value | GREATEREQUAL value | LESSEQUAL value | operator_inall_list | operator_in_list | contains_all_list | contains_list

  private String unescapeString(String str) {
    if (!str.startsWith("'") || !str.endsWith("'")) {
      throw new IllegalArgumentException(format("String value to unescape does not seem to be escaped: {0}", str));
    }
    str = str.substring(1);
    str = str.substring(0, str.length()-1);
    
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<str.length(); i++) {
      char c = str.charAt(i);
      if (c == '\\') {
        i++;
        if (i<str.length()) {
          if (str.charAt(i) == '\'') {
            sb.append("'");
            continue;
          }
        }
        
        sb.append(c);
      }
      else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
  
  private Optional<BooleanOp> toBooleanOp(BooleanContext booleanCtx) {
    if (booleanCtx.getChildCount()==1 && booleanCtx.getChild(0) instanceof TerminalNode) {
      String booleanOpStr = ((TerminalNode)booleanCtx.getChild(0)).getText().trim();
      if ("and".equalsIgnoreCase(booleanOpStr)) {
        return Optional.of(BooleanOp.and);
      }
      else if ("or".equalsIgnoreCase(booleanOpStr)) {
        return Optional.of(BooleanOp.or);
      }
      else if ("and not".equalsIgnoreCase(booleanOpStr)) {
        return Optional.of(BooleanOp.andNot);
      }
      else if ("or not".equalsIgnoreCase(booleanOpStr)) {
        return Optional.of(BooleanOp.orNot);
      }
    }
    return Optional.empty();
  }

  private static class DQLListenerImpl implements DQLListener {


    /**
     * Enter a parse tree produced by {@link DQLParser#start}.
     * @param ctx the parse tree
     */
    public void enterStart(DQLParser.StartContext ctx) {
      System.out.println("enterStart "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#start}.
     * @param ctx the parse tree
     */
    public void exitStart(DQLParser.StartContext ctx) {
      System.out.println("exitStart "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#term}.
     * @param ctx the parse tree
     */
    public void enterTerm(DQLParser.TermContext ctx) {
      System.out.println("enterTerm "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#term}.
     * @param ctx the parse tree
     */
    public void exitTerm(DQLParser.TermContext ctx) {
      System.out.println("exitTerm "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#identifier}.
     * @param ctx the parse tree
     */
    public void enterIdentifier(DQLParser.IdentifierContext ctx) {
      System.out.println("enterIdentifier "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#identifier}.
     * @param ctx the parse tree
     */
    public void exitIdentifier(DQLParser.IdentifierContext ctx) {
      System.out.println("exitIdentifier "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#operator_with_value}.
     * @param ctx the parse tree
     */
    public void enterOperator_with_value(DQLParser.Operator_with_valueContext ctx) {
      System.out.println("enterOperator_with_value "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#operator_with_value}.
     * @param ctx the parse tree
     */
    public void exitOperator_with_value(DQLParser.Operator_with_valueContext ctx) {
      System.out.println("exitOperator_with_value "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#operator_inall_list}.
     * @param ctx the parse tree
     */
    public void enterOperator_inall_list(DQLParser.Operator_inall_listContext ctx) {
      System.out.println("enterOperator_inall_list "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#operator_inall_list}.
     * @param ctx the parse tree
     */
    public void exitOperator_inall_list(DQLParser.Operator_inall_listContext ctx) {
      System.out.println("exitOperator_inall_list "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#operator_in_list}.
     * @param ctx the parse tree
     */
    public void enterOperator_in_list(DQLParser.Operator_in_listContext ctx) {
      System.out.println("enterOperator_in_list "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#operator_in_list}.
     * @param ctx the parse tree
     */
    public void exitOperator_in_list(DQLParser.Operator_in_listContext ctx) {
      System.out.println("exitOperator_in_list "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#contains_all_list}.
     * @param ctx the parse tree
     */
    public void enterContains_all_list(DQLParser.Contains_all_listContext ctx) {
      System.out.println("enterContains_all_list "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#contains_all_list}.
     * @param ctx the parse tree
     */
    public void exitContains_all_list(DQLParser.Contains_all_listContext ctx) {
      System.out.println("exitContains_all_list "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#contains_list}.
     * @param ctx the parse tree
     */
    public void enterContains_list(DQLParser.Contains_listContext ctx) {
      System.out.println("enterContains_list "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#contains_list}.
     * @param ctx the parse tree
     */
    public void exitContains_list(DQLParser.Contains_listContext ctx) {
      System.out.println("exitContains_list "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#value}.
     * @param ctx the parse tree
     */
    public void enterValue(DQLParser.ValueContext ctx) {
      System.out.println("enterValue "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#value}.
     * @param ctx the parse tree
     */
    public void exitValue(DQLParser.ValueContext ctx) {
      System.out.println("exitValue "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#boolean}.
     * @param ctx the parse tree
     */
    public void enterBoolean(DQLParser.BooleanContext ctx) {
      System.out.println("enterBoolean "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#boolean}.
     * @param ctx the parse tree
     */
    public void exitBoolean(DQLParser.BooleanContext ctx) {
      System.out.println("exitBoolean "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#escapedstring}.
     * @param ctx the parse tree
     */
    public void enterEscapedstring(DQLParser.EscapedstringContext ctx) {
      System.out.println("enterEscapedstring "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#escapedstring}.
     * @param ctx the parse tree
     */
    public void exitEscapedstring(DQLParser.EscapedstringContext ctx) {
      System.out.println("exitEscapedstring "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#datetime}.
     * @param ctx the parse tree
     */
    public void enterDatetime(DQLParser.DatetimeContext ctx) {
      System.out.println("enterDatetime "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#datetime}.
     * @param ctx the parse tree
     */
    public void exitDatetime(DQLParser.DatetimeContext ctx) {
      System.out.println("exitDatetime "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#number}.
     * @param ctx the parse tree
     */
    public void enterNumber(DQLParser.NumberContext ctx) {
      System.out.println("enterNumber "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#number}.
     * @param ctx the parse tree
     */
    public void exitNumber(DQLParser.NumberContext ctx) {
      System.out.println("exitNumber "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#fieldname}.
     * @param ctx the parse tree
     */
    public void enterFieldname(DQLParser.FieldnameContext ctx) {
      System.out.println("enterFieldname "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#fieldname}.
     * @param ctx the parse tree
     */
    public void exitFieldname(DQLParser.FieldnameContext ctx) {
      System.out.println("exitFieldname "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#viewandcolumnname}.
     * @param ctx the parse tree
     */
    public void enterViewandcolumnname(DQLParser.ViewandcolumnnameContext ctx) {
      System.out.println("enterViewandcolumnname "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#viewandcolumnname}.
     * @param ctx the parse tree
     */
    public void exitViewandcolumnname(DQLParser.ViewandcolumnnameContext ctx) {
      System.out.println("exitViewandcolumnname "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#atfunction}.
     * @param ctx the parse tree
     */
    public void enterAtfunction(DQLParser.AtfunctionContext ctx) {
      System.out.println("enterAtfunction "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#atfunction}.
     * @param ctx the parse tree
     */
    public void exitAtfunction(DQLParser.AtfunctionContext ctx) {
      System.out.println("exitAtfunction "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#substitutionvar}.
     * @param ctx the parse tree
     */
    public void enterSubstitutionvar(DQLParser.SubstitutionvarContext ctx) {
      System.out.println("enterSubstitutionvar "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#substitutionvar}.
     * @param ctx the parse tree
     */
    public void exitSubstitutionvar(DQLParser.SubstitutionvarContext ctx) {
      System.out.println("exitSubstitutionvar "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#formulaexpression}.
     * @param ctx the parse tree
     */
    public void enterFormulaexpression(DQLParser.FormulaexpressionContext ctx) {
      System.out.println("enterFormulaexpression "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#formulaexpression}.
     * @param ctx the parse tree
     */
    public void exitFormulaexpression(DQLParser.FormulaexpressionContext ctx) {
      System.out.println("exitFormulaexpression "+ctx);
    }
    /**
     * Enter a parse tree produced by {@link DQLParser#docftterm}.
     * @param ctx the parse tree
     */
    public void enterDocftterm(DQLParser.DocfttermContext ctx) {
      System.out.println("enterDocftterm "+ctx);
    }
    /**
     * Exit a parse tree produced by {@link DQLParser#docftterm}.
     * @param ctx the parse tree
     */
    public void exitDocftterm(DQLParser.DocfttermContext ctx) {
      System.out.println("exitDocftterm "+ctx);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
      //      System.out.println("enterEveryRule "+ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
      //      System.out.println("exitEveryRule "+ctx);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
      System.out.println("visitErrorNode "+node);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
      System.out.println("visitTerminal "+node);
    }

  }
}
