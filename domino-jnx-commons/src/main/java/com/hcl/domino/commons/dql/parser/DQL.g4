grammar DQL;

options { caseInsensitive = true; }

//*********************LEXER SPECIFICATION **************

fragment DIGIT
 : [0-9]
 ;

fragment VALUELIST
 : '(' ESCAPEDSTRINGVAL (',' ESCAPEDSTRINGVAL)* ')'
 ;


WS
 : [\t\r ]+ -> skip
 ;

CONTAINSALL_VALUELIST
    : 'contains all' VALUELIST
    ;

CONTAINS_VALUELIST
    : 'contains' VALUELIST
    ;

INALL_VALUELIST
    : 'in all' VALUELIST
    ;

IN_VALUELIST
    : 'in' VALUELIST
    ;

FORMULA
    : '@fl(' ESCAPEDSTRINGVAL ')' | '@formula(' ESCAPEDSTRINGVAL ')'
    ;

DATETIMEVAL
    : '@dt(\'' (~['])+ '\')'
    ;

VIEWANDCOLUMNNAMEVAL
    : '\'' (~['])+ '\'.' [a-z0-9]+
    ;

NUMBERVAL
 : '-'? DIGIT+ ('.' DIGIT+)? ('e' [+-] DIGIT+)?
 ;

ATFUNCTIONVAL
 : '@modifiedinthisfile' | '@documentuniqueid'| '@created'
 ;

SUBSTITUTIONVAR
: '?' [a-z]* [a-z0-9]*
;

NL
    : '\n'
    ;


BROPEN
    : '('
    ;

BRCLOSE
    : ')'
    ;

LESS
    : '<'
    ;

GREATER
    : '>'
    ;

LESSEQUAL
    : '<='
    ;

GREATEREQUAL
    : '>='
    ;

EQUAL
    : '='
    ;

ANDNOT
    : 'and' 'not'
    ;

ORNOT
    : 'or' 'not'
    ;

AND
    : 'and'
    ;

OR
    : 'or'
    ;

ESCAPEDSTRINGVAL
    : '\'' ('\\\'' | ~['])* '\''
    ;

FIELDNAME
    : [a-z0-9_]+
    ;

      //************** PARSER SPECIFICATION **************
start
 : term NL* EOF
 ;

term
 : docftterm | BROPEN docftterm BRCLOSE | identifier operator_with_value | BROPEN identifier operator_with_value BRCLOSE | formulaexpression | BROPEN formulaexpression BRCLOSE | term (boolean term)+ | BROPEN term (boolean term)+ BRCLOSE
 ;

identifier
 : viewandcolumnname | atfunction | formulaexpression | fieldname
 ;

operator_with_value 
 : EQUAL value  | GREATER value | LESS value | GREATEREQUAL value | LESSEQUAL value | operator_inall_list | operator_in_list | contains_all_list | contains_list
 ;

operator_inall_list
 : INALL_VALUELIST
 ;

operator_in_list
 : IN_VALUELIST
 ;

contains_all_list
 : CONTAINSALL_VALUELIST
 ;

contains_list
 : CONTAINS_VALUELIST
 ;

value
 : datetime | number | substitutionvar | escapedstring
 ;

boolean
 : ANDNOT | ORNOT | AND | OR
 ;

escapedstring
    : ESCAPEDSTRINGVAL
;

datetime
 : DATETIMEVAL
 ;

number
 : NUMBERVAL
 ;

fieldname
 : FIELDNAME
 ;

viewandcolumnname
 : VIEWANDCOLUMNNAMEVAL
 ;

atfunction
 : ATFUNCTIONVAL
 ;

substitutionvar
 : SUBSTITUTIONVAR
 ;

formulaexpression
 : FORMULA
 ;

docftterm
    : CONTAINSALL_VALUELIST | CONTAINS_VALUELIST
    ;
