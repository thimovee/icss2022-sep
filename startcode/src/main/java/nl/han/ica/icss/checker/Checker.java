package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                checkBody(((Stylerule) child).body);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkBody(ArrayList<ASTNode> body) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode child : body) {
            if (child instanceof Declaration) {
                checkExpression(((Declaration) child).expression);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkIfClause(IfClause ifClause) {
        checkExpression(ifClause.conditionalExpression);
        checkBody(ifClause.body);
        if (ifClause.elseClause != null) {
            checkBody(ifClause.elseClause.body);
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = checkExpression(assignment.expression);
        variableTypes.getFirst().put(assignment.name.name, type);
    }

    private ExpressionType checkExpression(Expression expression) {
        if (expression instanceof Literal) {
            return literalType((Literal) expression);
        }
        if (expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        }
        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            checkExpression(operation.lhs);
            checkExpression(operation.rhs);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference reference) {
        ExpressionType type = lookupVariable(reference.name);
        if (type == null) {
            reference.setError("Variable " + reference.name + " is not defined in this scope");
            return ExpressionType.UNDEFINED;
        }
        return type;
    }

    private ExpressionType lookupVariable(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            ExpressionType type = variableTypes.get(i).get(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    private ExpressionType literalType(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        if (literal instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (literal instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        if (literal instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        if (literal instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        return ExpressionType.UNDEFINED;
    }
}
