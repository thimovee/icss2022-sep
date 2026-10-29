grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: (variableAssignment | stylerule)* EOF;

stylerule: selector OPEN_BRACE ruleBody CLOSE_BRACE;

//--- gedeelde body van stylerule, if en else, zo kan een if alleen binnen een rule staan ---
ruleBody: (declaration | variableAssignment | ifClause)*;

ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE ruleBody CLOSE_BRACE elseClause?;

elseClause: ELSE OPEN_BRACE ruleBody CLOSE_BRACE;

variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;

variableReference: CAPITAL_IDENT;
//--- selectors tags, elementen en klasses ---
selector: LOWER_IDENT #tagSelector
        | ID_IDENT #idSelector
        | CLASS_IDENT #classSelector
        ;
//--- css declaratie background: red; of iets anders  ---
declaration: propertyName COLON expression SEMICOLON;

propertyName: LOWER_IDENT;

//--- bovenste alternatief gaat voor: * voor + en -, + en - samen zodat ze van links naar rechts gaan ---
expression: expression MUL expression #multiplyOperation
          | expression operator=(PLUS | MIN) expression #additiveOperation
          | variableReference #variableExpression
          | literal #literalExpression
          ;

literal: COLOR #colorLiteral
       | PIXELSIZE #pixelLiteral
       | PERCENTAGE #percentageLiteral
       | SCALAR #scalarLiteral
       | (TRUE | FALSE) #boolLiteral
       ;

