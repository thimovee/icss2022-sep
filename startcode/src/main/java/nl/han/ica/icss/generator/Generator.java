package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

import java.util.ArrayList;
import java.util.List;

public class Generator {

	private static final String INDENTATION = "  ";

	public String generate(AST ast) {
		List<String> rules = new ArrayList<>();
		for (ASTNode child : ast.root.body) {
			if (child instanceof Stylerule) {
				rules.add(generateStylerule((Stylerule) child, 0));
			}
		}
		return String.join("\n", rules);
	}

	private String generateStylerule(Stylerule stylerule, int level) {
		StringBuilder css = new StringBuilder();
		List<String> selectors = new ArrayList<>();
		for (Selector selector : stylerule.selectors) {
			selectors.add(selector.toString());
		}
		css.append(indent(level)).append(String.join(", ", selectors)).append(" {\n");
		for (ASTNode child : stylerule.body) {
			if (child instanceof Declaration) {
				css.append(generateDeclaration((Declaration) child, level + 1));
			}
		}
		css.append(indent(level)).append("}\n");
		return css.toString();
	}

	private String generateDeclaration(Declaration declaration, int level) {
		return indent(level) + declaration.property.name + ": " + generateLiteral((Literal) declaration.expression) + ";\n";
	}

	private String generateLiteral(Literal literal) {
		if (literal instanceof PixelLiteral) {
			return ((PixelLiteral) literal).value + "px";
		}
		if (literal instanceof PercentageLiteral) {
			return ((PercentageLiteral) literal).value + "%";
		}
		if (literal instanceof ColorLiteral) {
			return ((ColorLiteral) literal).value;
		}
		throw new IllegalArgumentException("Cannot generate CSS for " + literal.getNodeLabel());
	}

	private String indent(int level) {
		return INDENTATION.repeat(level);
	}
}
