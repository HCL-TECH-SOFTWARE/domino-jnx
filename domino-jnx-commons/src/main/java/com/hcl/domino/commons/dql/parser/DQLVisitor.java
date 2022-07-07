// Generated from DQL.g4 by ANTLR 4.10.1
package com.hcl.domino.commons.dql.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DQLParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(DQLParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(DQLParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DQLParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#operator_with_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_with_value(DQLParser.Operator_with_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#operator_inall_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_inall_list(DQLParser.Operator_inall_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#operator_in_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_in_list(DQLParser.Operator_in_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#contains_all_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContains_all_list(DQLParser.Contains_all_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#contains_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContains_list(DQLParser.Contains_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(DQLParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#boolean}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean(DQLParser.BooleanContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#escapedstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscapedstring(DQLParser.EscapedstringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#datetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetime(DQLParser.DatetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(DQLParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#fieldname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldname(DQLParser.FieldnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#viewandcolumnname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewandcolumnname(DQLParser.ViewandcolumnnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#atfunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtfunction(DQLParser.AtfunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#substitutionvar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstitutionvar(DQLParser.SubstitutionvarContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#formulaexpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormulaexpression(DQLParser.FormulaexpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DQLParser#docftterm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocftterm(DQLParser.DocfttermContext ctx);
}