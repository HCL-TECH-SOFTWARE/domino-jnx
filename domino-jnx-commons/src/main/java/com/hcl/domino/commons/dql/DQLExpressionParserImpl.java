package com.hcl.domino.commons.dql;

import static java.text.MessageFormat.format;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.hcl.domino.commons.dql.parser.DQLLexer;
import com.hcl.domino.commons.dql.parser.DQLListener;
import com.hcl.domino.commons.dql.parser.DQLParser;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLExpressionParser;

public class DQLExpressionParserImpl implements DQLExpressionParser {

  @Override
  public DQL parseDQL(String dql) throws IllegalArgumentException {
    DQLLexer dqlLexer = new DQLLexer(CharStreams.fromString(dql));

    CommonTokenStream tokens = new CommonTokenStream(dqlLexer);
    DQLParser parser = new DQLParser(tokens);
    ParseTree tree = parser.start();

    DQLListenerImpl dqlListener = new DQLListenerImpl();
    ParseTreeWalker walker = new ParseTreeWalker();

    walker.walk(dqlListener, tree);

//    throw new IllegalArgumentException(format("Unable to parse DQL string: {0}", dql));
    return null;
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
