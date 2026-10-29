package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        transformStylesheet(ast.root);
    }

    private void transformStylesheet(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode child : stylesheet.body) {
            if (child instanceof VariableAssignment) {
                transformVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) child;
                stylerule.body = transformBody(stylerule.body);
                newBody.add(stylerule);
            }
        }
        stylesheet.body = newBody;
        variableValues.removeFirst();
    }

    private ArrayList<ASTNode> transformBody(ArrayList<ASTNode> body) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode child : body) {
            if (child instanceof Declaration) {
                Declaration declaration = (Declaration) child;
                declaration.expression = evaluateExpression(declaration.expression);
                newBody.add(declaration);
            } else if (child instanceof VariableAssignment) {
                transformVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                newBody.addAll(transformIfClause((IfClause) child));
            }
        }
        variableValues.removeFirst();
        return newBody;
    }

    private ArrayList<ASTNode> transformIfClause(IfClause ifClause) {
        BoolLiteral condition = (BoolLiteral) evaluateExpression(ifClause.conditionalExpression);
        if (condition.value) {
            return transformBody(ifClause.body);
        }
        if (ifClause.elseClause != null) {
            return transformBody(ifClause.elseClause.body);
        }
        return new ArrayList<>();
    }

    private void transformVariableAssignment(VariableAssignment assignment) {
        variableValues.getFirst().put(assignment.name.name, evaluateExpression(assignment.expression));
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        }
        if (expression instanceof VariableReference) {
            return lookupVariable(((VariableReference) expression).name);
        }
        return evaluateOperation((Operation) expression);
    }

    private Literal evaluateOperation(Operation operation) {
        Literal left = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);
        if (operation instanceof MultiplyOperation) {
            Literal unitLiteral = left instanceof ScalarLiteral ? right : left;
            return createLiteral(unitLiteral, valueOf(left) * valueOf(right));
        }
        if (operation instanceof AddOperation) {
            return createLiteral(left, valueOf(left) + valueOf(right));
        }
        return createLiteral(left, valueOf(left) - valueOf(right));
    }

    private Literal lookupVariable(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            Literal value = variableValues.get(i).get(name);
            if (value != null) {
                return value;
            }
        }
        throw new IllegalStateException("Variable " + name + " is not defined");
    }

    private int valueOf(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        }
        if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        }
        if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }
        throw new IllegalArgumentException("Cannot calculate with " + literal.getNodeLabel());
    }

    private Literal createLiteral(Literal type, int value) {
        if (type instanceof PixelLiteral) {
            return new PixelLiteral(value);
        }
        if (type instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        }
        return new ScalarLiteral(value);
    }
}
