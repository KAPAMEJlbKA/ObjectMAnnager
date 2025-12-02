package com.kapamejlbka.objectmanager.domain.calcengine.dsl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpressionEvaluator {

    private enum TokenType {
        NUMBER, VARIABLE, OPERATOR, FUNCTION, LEFT_PAREN
    }

    private record Token(TokenType type, String value) {
    }

    public double evaluate(String expression, Map<String, Object> context) {
        Objects.requireNonNull(expression, "expression must not be null");
        Map<String, Object> values = context == null ? Map.of() : context;

        List<Token> rpn = toRpn(expression);
        return evaluateRpn(rpn, values);
    }

    private List<Token> toRpn(String expression) {
        List<Token> output = new ArrayList<>();
        Deque<Token> operators = new ArrayDeque<>();

        Token previousToken = null;
        int index = 0;
        int length = expression.length();

        while (index < length) {
            char current = expression.charAt(index);

            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            if (Character.isDigit(current) || current == '.') {
                int end = index + 1;
                while (end < length && (Character.isDigit(expression.charAt(end)) || expression.charAt(end) == '.')) {
                    end++;
                }
                output.add(new Token(TokenType.NUMBER, expression.substring(index, end)));
                previousToken = output.get(output.size() - 1);
                index = end;
                continue;
            }

            if (Character.isLetter(current)) {
                int end = index + 1;
                while (end < length && (Character.isLetterOrDigit(expression.charAt(end)) || expression.charAt(end) == '_')) {
                    end++;
                }
                String identifier = expression.substring(index, end);
                int next = skipWhitespace(expression, end);
                if (next < length && expression.charAt(next) == '(') {
                    operators.push(new Token(TokenType.FUNCTION, identifier));
                } else {
                    output.add(new Token(TokenType.VARIABLE, identifier));
                    previousToken = output.get(output.size() - 1);
                }
                index = end;
                continue;
            }

            if (isOperator(current)) {
                boolean unary = previousToken == null
                        || previousToken.type() == TokenType.OPERATOR
                        || previousToken.type() == TokenType.LEFT_PAREN
                        || previousToken.type() == TokenType.FUNCTION;
                if (unary) {
                    output.add(new Token(TokenType.NUMBER, "0"));
                }

                Token operator = new Token(TokenType.OPERATOR, String.valueOf(current));
                while (!operators.isEmpty() && operators.peek().type() == TokenType.OPERATOR
                        && precedence(operators.peek().value()) >= precedence(operator.value())) {
                    output.add(operators.pop());
                }
                operators.push(operator);
                previousToken = operator;
                index++;
                continue;
            }

            if (current == '(') {
                Token leftParen = new Token(TokenType.LEFT_PAREN, "(");
                operators.push(leftParen);
                previousToken = leftParen;
                index++;
                continue;
            }

            if (current == ',') {
                while (!operators.isEmpty() && operators.peek().type() != TokenType.LEFT_PAREN) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException("Misplaced comma in expression");
                }
                index++;
                previousToken = null;
                continue;
            }

            if (current == ')') {
                while (!operators.isEmpty() && operators.peek().type() != TokenType.LEFT_PAREN) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses in expression");
                }
                operators.pop();
                if (!operators.isEmpty() && operators.peek().type() == TokenType.FUNCTION) {
                    output.add(operators.pop());
                }
                previousToken = new Token(TokenType.NUMBER, "");
                index++;
                continue;
            }

            throw new IllegalArgumentException("Unexpected character in expression: " + current);
        }

        while (!operators.isEmpty()) {
            Token token = operators.pop();
            if (token.type() == TokenType.LEFT_PAREN) {
                throw new IllegalArgumentException("Mismatched parentheses in expression");
            }
            output.add(token);
        }

        return output;
    }

    private int skipWhitespace(String expression, int index) {
        while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
            index++;
        }
        return index;
    }

    private boolean isOperator(char symbol) {
        return symbol == '+' || symbol == '-' || symbol == '*' || symbol == '/';
    }

    private int precedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }

    private double evaluateRpn(List<Token> tokens, Map<String, Object> context) {
        Deque<Double> stack = new ArrayDeque<>();

        for (Token token : tokens) {
            switch (token.type()) {
                case NUMBER -> stack.push(Double.parseDouble(token.value()));
                case VARIABLE -> stack.push(resolveVariable(token.value(), context));
                case OPERATOR -> applyOperator(stack, token.value());
                case FUNCTION -> applyFunction(stack, token.value());
                default -> throw new IllegalStateException("Unexpected token type: " + token.type());
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return stack.pop();
    }

    private double resolveVariable(String name, Map<String, Object> context) {
        Object value = context.get(name);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("Unknown or non-numeric variable: " + name);
    }

    private void applyOperator(Deque<Double> stack, String operator) {
        if (stack.size() < 2) {
            throw new IllegalArgumentException("Insufficient values for operator " + operator);
        }
        double right = stack.pop();
        double left = stack.pop();

        double result = switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };

        stack.push(result);
    }

    private void applyFunction(Deque<Double> stack, String function) {
        double result;
        switch (function) {
            case "ceil" -> {
                ensureStackSize(stack, 1, function);
                result = Math.ceil(stack.pop());
            }
            case "floor" -> {
                ensureStackSize(stack, 1, function);
                result = Math.floor(stack.pop());
            }
            case "max" -> {
                ensureStackSize(stack, 2, function);
                double b = stack.pop();
                double a = stack.pop();
                result = Math.max(a, b);
            }
            case "min" -> {
                ensureStackSize(stack, 2, function);
                double b = stack.pop();
                double a = stack.pop();
                result = Math.min(a, b);
            }
            default -> throw new IllegalArgumentException("Unsupported function: " + function);
        }
        stack.push(result);
    }

    private void ensureStackSize(Deque<Double> stack, int expected, String name) {
        if (stack.size() < expected) {
            throw new IllegalArgumentException("Insufficient values for function " + name);
        }
    }
}
