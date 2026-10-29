package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

    // een HashMap per scope, de eerste in de lijst is de binnenste scope
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

    // body van stylerule, if of else, met een eigen scope die na de body weer verdwijnt
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
        ExpressionType conditionType = checkExpression(ifClause.conditionalExpression);
        // conditie moet een boolean zijn, UNDEFINED heeft al een eigen fout
        if (conditionType != ExpressionType.BOOL && conditionType != ExpressionType.UNDEFINED) {
            ifClause.setError("Condition of if-statement must be a boolean, found " + conditionType);
        }
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
            return checkOperation((Operation) expression);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType left = checkExpression(operation.lhs);
        ExpressionType right = checkExpression(operation.rhs);
        // UNDEFINED betekent dat er al een fout is gemeld, dus geen extra fout erbij
        if (left == ExpressionType.UNDEFINED || right == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }
        // kleuren en booleans eerst
        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Colors cannot be used in " + operation.getNodeLabel());
            return ExpressionType.UNDEFINED;
        }
        if (left == ExpressionType.BOOL || right == ExpressionType.BOOL) {
            operation.setError("Booleans cannot be used in " + operation.getNodeLabel());
            return ExpressionType.UNDEFINED;
        }
        if (operation instanceof MultiplyOperation) {
            return checkMultiplyOperation(operation, left, right);
        }
        // plus en min alleen met gelijke types
        if (left != right) {
            operation.setError("Operands of " + operation.getNodeLabel() + " must have the same type, found "
                    + left + " and " + right);
            return ExpressionType.UNDEFINED;
        }
        return left;
    }

    // minstens een scalar, de uitkomst krijgt het type van de andere kant
    private ExpressionType checkMultiplyOperation(Operation operation, ExpressionType left, ExpressionType right) {
        if (left == ExpressionType.SCALAR) {
            return right;
        }
        if (right == ExpressionType.SCALAR) {
            return left;
        }
        operation.setError("At least one operand of Multiply must be a scalar, found " + left + " and " + right);
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

    // zoekt van de binnenste naar de buitenste scope, null als de variabele niet in scope is
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
