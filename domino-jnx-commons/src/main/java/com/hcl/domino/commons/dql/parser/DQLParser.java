// Generated from DQL.g4 by ANTLR 4.10.1
package com.hcl.domino.commons.dql.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WS=1, CONTAINSALL_VALUELIST=2, CONTAINS_VALUELIST=3, INALL_VALUELIST=4, 
		IN_VALUELIST=5, FORMULA=6, DATETIMEVAL=7, VIEWANDCOLUMNNAMEVAL=8, NUMBERVAL=9, 
		ATFUNCTIONVAL=10, SUBSTITUTIONVAR=11, NL=12, BROPEN=13, BRCLOSE=14, LESS=15, 
		GREATER=16, LESSEQUAL=17, GREATEREQUAL=18, EQUAL=19, ANDNOT=20, ORNOT=21, 
		AND=22, OR=23, ESCAPEDSTRINGVAL=24, FIELDNAME=25;
	public static final int
		RULE_start = 0, RULE_term = 1, RULE_identifier = 2, RULE_operator_with_value = 3, 
		RULE_operator_inall_list = 4, RULE_operator_in_list = 5, RULE_contains_all_list = 6, 
		RULE_contains_list = 7, RULE_value = 8, RULE_boolean = 9, RULE_escapedstring = 10, 
		RULE_datetime = 11, RULE_number = 12, RULE_fieldname = 13, RULE_viewandcolumnname = 14, 
		RULE_atfunction = 15, RULE_substitutionvar = 16, RULE_formulaexpression = 17, 
		RULE_docftterm = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "term", "identifier", "operator_with_value", "operator_inall_list", 
			"operator_in_list", "contains_all_list", "contains_list", "value", "boolean", 
			"escapedstring", "datetime", "number", "fieldname", "viewandcolumnname", 
			"atfunction", "substitutionvar", "formulaexpression", "docftterm"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"'\\n'", "'('", "')'", "'<'", "'>'", "'<='", "'>='", "'='", null, null, 
			"'and'", "'or'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WS", "CONTAINSALL_VALUELIST", "CONTAINS_VALUELIST", "INALL_VALUELIST", 
			"IN_VALUELIST", "FORMULA", "DATETIMEVAL", "VIEWANDCOLUMNNAMEVAL", "NUMBERVAL", 
			"ATFUNCTIONVAL", "SUBSTITUTIONVAR", "NL", "BROPEN", "BRCLOSE", "LESS", 
			"GREATER", "LESSEQUAL", "GREATEREQUAL", "EQUAL", "ANDNOT", "ORNOT", "AND", 
			"OR", "ESCAPEDSTRINGVAL", "FIELDNAME"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "DQL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode EOF() { return getToken(DQLParser.EOF, 0); }
		public List<TerminalNode> NL() { return getTokens(DQLParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(DQLParser.NL, i);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			term(0);
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(39);
				match(NL);
				}
				}
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(45);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public DocfttermContext docftterm() {
			return getRuleContext(DocfttermContext.class,0);
		}
		public TerminalNode BROPEN() { return getToken(DQLParser.BROPEN, 0); }
		public TerminalNode BRCLOSE() { return getToken(DQLParser.BRCLOSE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Operator_with_valueContext operator_with_value() {
			return getRuleContext(Operator_with_valueContext.class,0);
		}
		public FormulaexpressionContext formulaexpression() {
			return getRuleContext(FormulaexpressionContext.class,0);
		}
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<BooleanContext> boolean_() {
			return getRuleContexts(BooleanContext.class);
		}
		public BooleanContext boolean_(int i) {
			return getRuleContext(BooleanContext.class,i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		return term(0);
	}

	private TermContext term(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermContext _localctx = new TermContext(_ctx, _parentState);
		TermContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_term, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(48);
				docftterm();
				}
				break;
			case 2:
				{
				setState(49);
				match(BROPEN);
				setState(50);
				docftterm();
				setState(51);
				match(BRCLOSE);
				}
				break;
			case 3:
				{
				setState(53);
				identifier();
				setState(54);
				operator_with_value();
				}
				break;
			case 4:
				{
				setState(56);
				match(BROPEN);
				setState(57);
				identifier();
				setState(58);
				operator_with_value();
				setState(59);
				match(BRCLOSE);
				}
				break;
			case 5:
				{
				setState(61);
				formulaexpression();
				}
				break;
			case 6:
				{
				setState(62);
				match(BROPEN);
				setState(63);
				formulaexpression();
				setState(64);
				match(BRCLOSE);
				}
				break;
			case 7:
				{
				setState(66);
				match(BROPEN);
				setState(67);
				term(0);
				setState(71); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(68);
					boolean_();
					setState(69);
					term(0);
					}
					}
					setState(73); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANDNOT) | (1L << ORNOT) | (1L << AND) | (1L << OR))) != 0) );
				setState(75);
				match(BRCLOSE);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(89);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_term);
					setState(79);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(83); 
					_errHandler.sync(this);
					_alt = 1;
					do {
						switch (_alt) {
						case 1:
							{
							{
							setState(80);
							boolean_();
							setState(81);
							term(0);
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(85); 
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
					} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
					}
					} 
				}
				setState(91);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public ViewandcolumnnameContext viewandcolumnname() {
			return getRuleContext(ViewandcolumnnameContext.class,0);
		}
		public AtfunctionContext atfunction() {
			return getRuleContext(AtfunctionContext.class,0);
		}
		public FormulaexpressionContext formulaexpression() {
			return getRuleContext(FormulaexpressionContext.class,0);
		}
		public FieldnameContext fieldname() {
			return getRuleContext(FieldnameContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_identifier);
		try {
			setState(96);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VIEWANDCOLUMNNAMEVAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(92);
				viewandcolumnname();
				}
				break;
			case ATFUNCTIONVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				atfunction();
				}
				break;
			case FORMULA:
				enterOuterAlt(_localctx, 3);
				{
				setState(94);
				formulaexpression();
				}
				break;
			case FIELDNAME:
				enterOuterAlt(_localctx, 4);
				{
				setState(95);
				fieldname();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Operator_with_valueContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(DQLParser.EQUAL, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode GREATER() { return getToken(DQLParser.GREATER, 0); }
		public TerminalNode LESS() { return getToken(DQLParser.LESS, 0); }
		public TerminalNode GREATEREQUAL() { return getToken(DQLParser.GREATEREQUAL, 0); }
		public TerminalNode LESSEQUAL() { return getToken(DQLParser.LESSEQUAL, 0); }
		public Operator_inall_listContext operator_inall_list() {
			return getRuleContext(Operator_inall_listContext.class,0);
		}
		public Operator_in_listContext operator_in_list() {
			return getRuleContext(Operator_in_listContext.class,0);
		}
		public Contains_all_listContext contains_all_list() {
			return getRuleContext(Contains_all_listContext.class,0);
		}
		public Contains_listContext contains_list() {
			return getRuleContext(Contains_listContext.class,0);
		}
		public Operator_with_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator_with_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterOperator_with_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitOperator_with_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitOperator_with_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Operator_with_valueContext operator_with_value() throws RecognitionException {
		Operator_with_valueContext _localctx = new Operator_with_valueContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_operator_with_value);
		try {
			setState(112);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQUAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(98);
				match(EQUAL);
				setState(99);
				value();
				}
				break;
			case GREATER:
				enterOuterAlt(_localctx, 2);
				{
				setState(100);
				match(GREATER);
				setState(101);
				value();
				}
				break;
			case LESS:
				enterOuterAlt(_localctx, 3);
				{
				setState(102);
				match(LESS);
				setState(103);
				value();
				}
				break;
			case GREATEREQUAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(104);
				match(GREATEREQUAL);
				setState(105);
				value();
				}
				break;
			case LESSEQUAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(106);
				match(LESSEQUAL);
				setState(107);
				value();
				}
				break;
			case INALL_VALUELIST:
				enterOuterAlt(_localctx, 6);
				{
				setState(108);
				operator_inall_list();
				}
				break;
			case IN_VALUELIST:
				enterOuterAlt(_localctx, 7);
				{
				setState(109);
				operator_in_list();
				}
				break;
			case CONTAINSALL_VALUELIST:
				enterOuterAlt(_localctx, 8);
				{
				setState(110);
				contains_all_list();
				}
				break;
			case CONTAINS_VALUELIST:
				enterOuterAlt(_localctx, 9);
				{
				setState(111);
				contains_list();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Operator_inall_listContext extends ParserRuleContext {
		public TerminalNode INALL_VALUELIST() { return getToken(DQLParser.INALL_VALUELIST, 0); }
		public Operator_inall_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator_inall_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterOperator_inall_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitOperator_inall_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitOperator_inall_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Operator_inall_listContext operator_inall_list() throws RecognitionException {
		Operator_inall_listContext _localctx = new Operator_inall_listContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_operator_inall_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			match(INALL_VALUELIST);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Operator_in_listContext extends ParserRuleContext {
		public TerminalNode IN_VALUELIST() { return getToken(DQLParser.IN_VALUELIST, 0); }
		public Operator_in_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator_in_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterOperator_in_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitOperator_in_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitOperator_in_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Operator_in_listContext operator_in_list() throws RecognitionException {
		Operator_in_listContext _localctx = new Operator_in_listContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_operator_in_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			match(IN_VALUELIST);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Contains_all_listContext extends ParserRuleContext {
		public TerminalNode CONTAINSALL_VALUELIST() { return getToken(DQLParser.CONTAINSALL_VALUELIST, 0); }
		public Contains_all_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contains_all_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterContains_all_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitContains_all_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitContains_all_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contains_all_listContext contains_all_list() throws RecognitionException {
		Contains_all_listContext _localctx = new Contains_all_listContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_contains_all_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			match(CONTAINSALL_VALUELIST);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Contains_listContext extends ParserRuleContext {
		public TerminalNode CONTAINS_VALUELIST() { return getToken(DQLParser.CONTAINS_VALUELIST, 0); }
		public Contains_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contains_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterContains_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitContains_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitContains_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contains_listContext contains_list() throws RecognitionException {
		Contains_listContext _localctx = new Contains_listContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_contains_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			match(CONTAINS_VALUELIST);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public DatetimeContext datetime() {
			return getRuleContext(DatetimeContext.class,0);
		}
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public SubstitutionvarContext substitutionvar() {
			return getRuleContext(SubstitutionvarContext.class,0);
		}
		public EscapedstringContext escapedstring() {
			return getRuleContext(EscapedstringContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_value);
		try {
			setState(126);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATETIMEVAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				datetime();
				}
				break;
			case NUMBERVAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
				number();
				}
				break;
			case SUBSTITUTIONVAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(124);
				substitutionvar();
				}
				break;
			case ESCAPEDSTRINGVAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(125);
				escapedstring();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanContext extends ParserRuleContext {
		public TerminalNode ANDNOT() { return getToken(DQLParser.ANDNOT, 0); }
		public TerminalNode ORNOT() { return getToken(DQLParser.ORNOT, 0); }
		public TerminalNode AND() { return getToken(DQLParser.AND, 0); }
		public TerminalNode OR() { return getToken(DQLParser.OR, 0); }
		public BooleanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterBoolean(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitBoolean(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitBoolean(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanContext boolean_() throws RecognitionException {
		BooleanContext _localctx = new BooleanContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_boolean);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANDNOT) | (1L << ORNOT) | (1L << AND) | (1L << OR))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EscapedstringContext extends ParserRuleContext {
		public TerminalNode ESCAPEDSTRINGVAL() { return getToken(DQLParser.ESCAPEDSTRINGVAL, 0); }
		public EscapedstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escapedstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterEscapedstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitEscapedstring(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitEscapedstring(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EscapedstringContext escapedstring() throws RecognitionException {
		EscapedstringContext _localctx = new EscapedstringContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_escapedstring);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(ESCAPEDSTRINGVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DatetimeContext extends ParserRuleContext {
		public TerminalNode DATETIMEVAL() { return getToken(DQLParser.DATETIMEVAL, 0); }
		public DatetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterDatetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitDatetime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitDatetime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatetimeContext datetime() throws RecognitionException {
		DatetimeContext _localctx = new DatetimeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_datetime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(DATETIMEVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode NUMBERVAL() { return getToken(DQLParser.NUMBERVAL, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(NUMBERVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldnameContext extends ParserRuleContext {
		public TerminalNode FIELDNAME() { return getToken(DQLParser.FIELDNAME, 0); }
		public FieldnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterFieldname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitFieldname(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitFieldname(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldnameContext fieldname() throws RecognitionException {
		FieldnameContext _localctx = new FieldnameContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_fieldname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(FIELDNAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ViewandcolumnnameContext extends ParserRuleContext {
		public TerminalNode VIEWANDCOLUMNNAMEVAL() { return getToken(DQLParser.VIEWANDCOLUMNNAMEVAL, 0); }
		public ViewandcolumnnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_viewandcolumnname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterViewandcolumnname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitViewandcolumnname(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitViewandcolumnname(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ViewandcolumnnameContext viewandcolumnname() throws RecognitionException {
		ViewandcolumnnameContext _localctx = new ViewandcolumnnameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_viewandcolumnname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(VIEWANDCOLUMNNAMEVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtfunctionContext extends ParserRuleContext {
		public TerminalNode ATFUNCTIONVAL() { return getToken(DQLParser.ATFUNCTIONVAL, 0); }
		public AtfunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atfunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterAtfunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitAtfunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitAtfunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtfunctionContext atfunction() throws RecognitionException {
		AtfunctionContext _localctx = new AtfunctionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_atfunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(ATFUNCTIONVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubstitutionvarContext extends ParserRuleContext {
		public TerminalNode SUBSTITUTIONVAR() { return getToken(DQLParser.SUBSTITUTIONVAR, 0); }
		public SubstitutionvarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substitutionvar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterSubstitutionvar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitSubstitutionvar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitSubstitutionvar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstitutionvarContext substitutionvar() throws RecognitionException {
		SubstitutionvarContext _localctx = new SubstitutionvarContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_substitutionvar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(SUBSTITUTIONVAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FormulaexpressionContext extends ParserRuleContext {
		public TerminalNode FORMULA() { return getToken(DQLParser.FORMULA, 0); }
		public FormulaexpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formulaexpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterFormulaexpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitFormulaexpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitFormulaexpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormulaexpressionContext formulaexpression() throws RecognitionException {
		FormulaexpressionContext _localctx = new FormulaexpressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_formulaexpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(FORMULA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DocfttermContext extends ParserRuleContext {
		public TerminalNode CONTAINSALL_VALUELIST() { return getToken(DQLParser.CONTAINSALL_VALUELIST, 0); }
		public TerminalNode CONTAINS_VALUELIST() { return getToken(DQLParser.CONTAINS_VALUELIST, 0); }
		public DocfttermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_docftterm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).enterDocftterm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DQLListener ) ((DQLListener)listener).exitDocftterm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DQLVisitor ) return ((DQLVisitor<? extends T>)visitor).visitDocftterm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocfttermContext docftterm() throws RecognitionException {
		DocfttermContext _localctx = new DocfttermContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_docftterm);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			_la = _input.LA(1);
			if ( !(_la==CONTAINSALL_VALUELIST || _la==CONTAINS_VALUELIST) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return term_sempred((TermContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean term_sempred(TermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0019\u0095\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0001\u0000\u0001\u0000\u0005\u0000)\b\u0000\n\u0000\f\u0000,\t"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0004\u0001H\b\u0001\u000b\u0001\f\u0001"+
		"I\u0001\u0001\u0001\u0001\u0003\u0001N\b\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0004\u0001T\b\u0001\u000b\u0001\f\u0001U\u0005"+
		"\u0001X\b\u0001\n\u0001\f\u0001[\t\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002a\b\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003q\b\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0003\b\u007f\b\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0000\u0001\u0002\u0013\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$\u0000"+
		"\u0002\u0001\u0000\u0014\u0017\u0001\u0000\u0002\u0003\u0099\u0000&\u0001"+
		"\u0000\u0000\u0000\u0002M\u0001\u0000\u0000\u0000\u0004`\u0001\u0000\u0000"+
		"\u0000\u0006p\u0001\u0000\u0000\u0000\br\u0001\u0000\u0000\u0000\nt\u0001"+
		"\u0000\u0000\u0000\fv\u0001\u0000\u0000\u0000\u000ex\u0001\u0000\u0000"+
		"\u0000\u0010~\u0001\u0000\u0000\u0000\u0012\u0080\u0001\u0000\u0000\u0000"+
		"\u0014\u0082\u0001\u0000\u0000\u0000\u0016\u0084\u0001\u0000\u0000\u0000"+
		"\u0018\u0086\u0001\u0000\u0000\u0000\u001a\u0088\u0001\u0000\u0000\u0000"+
		"\u001c\u008a\u0001\u0000\u0000\u0000\u001e\u008c\u0001\u0000\u0000\u0000"+
		" \u008e\u0001\u0000\u0000\u0000\"\u0090\u0001\u0000\u0000\u0000$\u0092"+
		"\u0001\u0000\u0000\u0000&*\u0003\u0002\u0001\u0000\')\u0005\f\u0000\u0000"+
		"(\'\u0001\u0000\u0000\u0000),\u0001\u0000\u0000\u0000*(\u0001\u0000\u0000"+
		"\u0000*+\u0001\u0000\u0000\u0000+-\u0001\u0000\u0000\u0000,*\u0001\u0000"+
		"\u0000\u0000-.\u0005\u0000\u0000\u0001.\u0001\u0001\u0000\u0000\u0000"+
		"/0\u0006\u0001\uffff\uffff\u00000N\u0003$\u0012\u000012\u0005\r\u0000"+
		"\u000023\u0003$\u0012\u000034\u0005\u000e\u0000\u00004N\u0001\u0000\u0000"+
		"\u000056\u0003\u0004\u0002\u000067\u0003\u0006\u0003\u00007N\u0001\u0000"+
		"\u0000\u000089\u0005\r\u0000\u00009:\u0003\u0004\u0002\u0000:;\u0003\u0006"+
		"\u0003\u0000;<\u0005\u000e\u0000\u0000<N\u0001\u0000\u0000\u0000=N\u0003"+
		"\"\u0011\u0000>?\u0005\r\u0000\u0000?@\u0003\"\u0011\u0000@A\u0005\u000e"+
		"\u0000\u0000AN\u0001\u0000\u0000\u0000BC\u0005\r\u0000\u0000CG\u0003\u0002"+
		"\u0001\u0000DE\u0003\u0012\t\u0000EF\u0003\u0002\u0001\u0000FH\u0001\u0000"+
		"\u0000\u0000GD\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000IG\u0001"+
		"\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000"+
		"KL\u0005\u000e\u0000\u0000LN\u0001\u0000\u0000\u0000M/\u0001\u0000\u0000"+
		"\u0000M1\u0001\u0000\u0000\u0000M5\u0001\u0000\u0000\u0000M8\u0001\u0000"+
		"\u0000\u0000M=\u0001\u0000\u0000\u0000M>\u0001\u0000\u0000\u0000MB\u0001"+
		"\u0000\u0000\u0000NY\u0001\u0000\u0000\u0000OS\n\u0002\u0000\u0000PQ\u0003"+
		"\u0012\t\u0000QR\u0003\u0002\u0001\u0000RT\u0001\u0000\u0000\u0000SP\u0001"+
		"\u0000\u0000\u0000TU\u0001\u0000\u0000\u0000US\u0001\u0000\u0000\u0000"+
		"UV\u0001\u0000\u0000\u0000VX\u0001\u0000\u0000\u0000WO\u0001\u0000\u0000"+
		"\u0000X[\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000"+
		"\u0000\u0000Z\u0003\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000"+
		"\\a\u0003\u001c\u000e\u0000]a\u0003\u001e\u000f\u0000^a\u0003\"\u0011"+
		"\u0000_a\u0003\u001a\r\u0000`\\\u0001\u0000\u0000\u0000`]\u0001\u0000"+
		"\u0000\u0000`^\u0001\u0000\u0000\u0000`_\u0001\u0000\u0000\u0000a\u0005"+
		"\u0001\u0000\u0000\u0000bc\u0005\u0013\u0000\u0000cq\u0003\u0010\b\u0000"+
		"de\u0005\u0010\u0000\u0000eq\u0003\u0010\b\u0000fg\u0005\u000f\u0000\u0000"+
		"gq\u0003\u0010\b\u0000hi\u0005\u0012\u0000\u0000iq\u0003\u0010\b\u0000"+
		"jk\u0005\u0011\u0000\u0000kq\u0003\u0010\b\u0000lq\u0003\b\u0004\u0000"+
		"mq\u0003\n\u0005\u0000nq\u0003\f\u0006\u0000oq\u0003\u000e\u0007\u0000"+
		"pb\u0001\u0000\u0000\u0000pd\u0001\u0000\u0000\u0000pf\u0001\u0000\u0000"+
		"\u0000ph\u0001\u0000\u0000\u0000pj\u0001\u0000\u0000\u0000pl\u0001\u0000"+
		"\u0000\u0000pm\u0001\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000po\u0001"+
		"\u0000\u0000\u0000q\u0007\u0001\u0000\u0000\u0000rs\u0005\u0004\u0000"+
		"\u0000s\t\u0001\u0000\u0000\u0000tu\u0005\u0005\u0000\u0000u\u000b\u0001"+
		"\u0000\u0000\u0000vw\u0005\u0002\u0000\u0000w\r\u0001\u0000\u0000\u0000"+
		"xy\u0005\u0003\u0000\u0000y\u000f\u0001\u0000\u0000\u0000z\u007f\u0003"+
		"\u0016\u000b\u0000{\u007f\u0003\u0018\f\u0000|\u007f\u0003 \u0010\u0000"+
		"}\u007f\u0003\u0014\n\u0000~z\u0001\u0000\u0000\u0000~{\u0001\u0000\u0000"+
		"\u0000~|\u0001\u0000\u0000\u0000~}\u0001\u0000\u0000\u0000\u007f\u0011"+
		"\u0001\u0000\u0000\u0000\u0080\u0081\u0007\u0000\u0000\u0000\u0081\u0013"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0005\u0018\u0000\u0000\u0083\u0015"+
		"\u0001\u0000\u0000\u0000\u0084\u0085\u0005\u0007\u0000\u0000\u0085\u0017"+
		"\u0001\u0000\u0000\u0000\u0086\u0087\u0005\t\u0000\u0000\u0087\u0019\u0001"+
		"\u0000\u0000\u0000\u0088\u0089\u0005\u0019\u0000\u0000\u0089\u001b\u0001"+
		"\u0000\u0000\u0000\u008a\u008b\u0005\b\u0000\u0000\u008b\u001d\u0001\u0000"+
		"\u0000\u0000\u008c\u008d\u0005\n\u0000\u0000\u008d\u001f\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0005\u000b\u0000\u0000\u008f!\u0001\u0000\u0000\u0000"+
		"\u0090\u0091\u0005\u0006\u0000\u0000\u0091#\u0001\u0000\u0000\u0000\u0092"+
		"\u0093\u0007\u0001\u0000\u0000\u0093%\u0001\u0000\u0000\u0000\b*IMUY`"+
		"p~";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}