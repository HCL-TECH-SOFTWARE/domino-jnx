// Generated from DQL.g4 by ANTLR 4.10.1
package com.hcl.domino.commons.dql.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DQLParser}.
 */
public interface DQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DQLParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(DQLParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(DQLParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(DQLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(DQLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DQLParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DQLParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#operator_with_value}.
	 * @param ctx the parse tree
	 */
	void enterOperator_with_value(DQLParser.Operator_with_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#operator_with_value}.
	 * @param ctx the parse tree
	 */
	void exitOperator_with_value(DQLParser.Operator_with_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#operator_inall_list}.
	 * @param ctx the parse tree
	 */
	void enterOperator_inall_list(DQLParser.Operator_inall_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#operator_inall_list}.
	 * @param ctx the parse tree
	 */
	void exitOperator_inall_list(DQLParser.Operator_inall_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#operator_in_list}.
	 * @param ctx the parse tree
	 */
	void enterOperator_in_list(DQLParser.Operator_in_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#operator_in_list}.
	 * @param ctx the parse tree
	 */
	void exitOperator_in_list(DQLParser.Operator_in_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#contains_all_list}.
	 * @param ctx the parse tree
	 */
	void enterContains_all_list(DQLParser.Contains_all_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#contains_all_list}.
	 * @param ctx the parse tree
	 */
	void exitContains_all_list(DQLParser.Contains_all_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#contains_list}.
	 * @param ctx the parse tree
	 */
	void enterContains_list(DQLParser.Contains_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#contains_list}.
	 * @param ctx the parse tree
	 */
	void exitContains_list(DQLParser.Contains_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(DQLParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(DQLParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#boolean}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(DQLParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#boolean}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(DQLParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#escapedstring}.
	 * @param ctx the parse tree
	 */
	void enterEscapedstring(DQLParser.EscapedstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#escapedstring}.
	 * @param ctx the parse tree
	 */
	void exitEscapedstring(DQLParser.EscapedstringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#datetime}.
	 * @param ctx the parse tree
	 */
	void enterDatetime(DQLParser.DatetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#datetime}.
	 * @param ctx the parse tree
	 */
	void exitDatetime(DQLParser.DatetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(DQLParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(DQLParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#fieldname}.
	 * @param ctx the parse tree
	 */
	void enterFieldname(DQLParser.FieldnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#fieldname}.
	 * @param ctx the parse tree
	 */
	void exitFieldname(DQLParser.FieldnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#viewandcolumnname}.
	 * @param ctx the parse tree
	 */
	void enterViewandcolumnname(DQLParser.ViewandcolumnnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#viewandcolumnname}.
	 * @param ctx the parse tree
	 */
	void exitViewandcolumnname(DQLParser.ViewandcolumnnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#atfunction}.
	 * @param ctx the parse tree
	 */
	void enterAtfunction(DQLParser.AtfunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#atfunction}.
	 * @param ctx the parse tree
	 */
	void exitAtfunction(DQLParser.AtfunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#substitutionvar}.
	 * @param ctx the parse tree
	 */
	void enterSubstitutionvar(DQLParser.SubstitutionvarContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#substitutionvar}.
	 * @param ctx the parse tree
	 */
	void exitSubstitutionvar(DQLParser.SubstitutionvarContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#formulaexpression}.
	 * @param ctx the parse tree
	 */
	void enterFormulaexpression(DQLParser.FormulaexpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#formulaexpression}.
	 * @param ctx the parse tree
	 */
	void exitFormulaexpression(DQLParser.FormulaexpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DQLParser#docftterm}.
	 * @param ctx the parse tree
	 */
	void enterDocftterm(DQLParser.DocfttermContext ctx);
	/**
	 * Exit a parse tree produced by {@link DQLParser#docftterm}.
	 * @param ctx the parse tree
	 */
	void exitDocftterm(DQLParser.DocfttermContext ctx);
}