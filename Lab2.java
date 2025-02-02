package Calculator;

import java.util.*;
import java.lang.Math;

/**
 * For this lab we are using the functionality such as +,-,*,/,^, sin, cos, tan, cot, log, lg to calculate the expression is valiadated and then 
 * try to check it with that one
 * @author Howey
 *
 */
public class Lab2 {
    // Use HashMap to define operator precedence
    private static final Map<String, Integer> current = new HashMap<>();

    static {
        current.put("+", 1);
        current.put("-", 1);
        current.put("*", 2);
        current.put("/", 2);
        current.put("^", 3);
        current.put("sin", 4);
        current.put("cos", 4);
        current.put("tan", 4);
        current.put("cot", 4);
        current.put("ln", 4);
        current.put("log", 4);
        current.put("(", 0);
        current.put(")", 0);
        current.put("{", 0);
        current.put("}", 0);
    }
/**
 * This is the main method to start the program. It tries to ask the user for an arithmetic expression and then use to evaluate it
 * @param args command-line arguments
 */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please insert your arithmetic equation: ");
        String number = scanner.nextLine();

        try {
            //use to validate the Expression for more 
            validateExpression(number);
            double result = evaluateExpression(number);
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
    }
    /**
     * THis one use to given the arithemtic to makesure it is correct for the math format
     * if this is not have the balance parentheses it will invalid
     * @param express the arithmetic expression to validate
     * @throws IllegalArgumentException if the expression is invalid
     */
    private static void validateExpression(String expression) {
        // Try to remove all the space at first so start will have the cleaner
        expression = expression.replaceAll("\\s+", "");
        //Check if it is empty give an output
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }

        // Check for the parathensis and curly bracket
        Stack<Character> par = new Stack<>();
        for (char a : expression.toCharArray()) {
            if (a == '(' || a == '{') {
                par.push(a);
            } else if (a == ')' || a == '}') {
                if (par.isEmpty()) {
                    throw new IllegalArgumentException("Unmatched closing parenthesis");
                }
                //Check for the openning for the parenthesis and the curly bracket
                char open = par.pop();
                if ((a == ')' && open != '(') || (a == '}' && open != '{')) {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            }
        }
        if (!par.isEmpty()) {
            throw new IllegalArgumentException("Unclosed parentheses");
        }

        // try to splut for the token to make sure it is divide 
        String[] tokens = expression.split("(?<=[()+\\-*/^}{])|(?=[()+\\-*/^}{])|\\s+");
        boolean expect = true;
        boolean lastOp = true;
//use the for loop to see the token for that
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) continue;

            // Updated pattern to properly handle decimal numbers and more than that with the sin and the cos for that
            if (!token.matches("-?\\d*\\.?\\d+|[(){}+\\-*/^]|sin|cos|tan|cot|ln|log")) {
                throw new IllegalArgumentException("Invalid character: " + token);
            }

            // Rest of the validation remains the same...
            boolean isOperator = "+-*/^".contains(token);
            if (isOperator && lastOp && !token.equals("-")) {
                throw new IllegalArgumentException("Consecutive operators are not allowed");
            }

            if (isOperator && i == tokens.length - 1) {
                throw new IllegalArgumentException("Expression cannot end with an operator");
            }

            if ((token.equals(")") || token.equals("}")) && i < tokens.length - 1) {
                String nextToken = tokens[i + 1].trim();
                if (isNumeric(nextToken)) {
                    throw new IllegalArgumentException("Missing operator between parenthesis and number");
                }
            }
//check the . forto make sure it is a decimal number
            if (token.contains(".")) {
                int decimalPoints = token.length() - token.replace(".", "").length();
                if (decimalPoints > 1) {
                    throw new IllegalArgumentException("Invalid number format: multiple decimal points");
                }
                if (!isNumeric(token)) {
                    throw new IllegalArgumentException("Invalid number format: " + token);
                }
            }

            if (isOperator) {
                lastOp = true;
            } else if (token.equals("(") || token.equals("{")) {
                lastOp = true;
            } else {
                lastOp = false;
            }
        }
    }
    
    /**
     * Evaluates the given arithemetic expression by using stacks to store the values and operators
     * It processes token in a left to right, applying the operator based on precedence
     * @param express the arithmetic expression to evaluate
     * 
     * @return the result as aspect with all the expression
     * @throws IllegalArgumentException if there is an error in the expression
     * @throws ArithmeticException if there is a division by zero or invalid operation
     */
    private static double evaluateExpression(String express) {
        Stack<Double> values = new Stack<>();
        Stack<String> op = new Stack<>();

        // Use express to split thhe token
        String[] tokens = express.split("(?<=[()+\\-*/^}{])|(?=[()+\\-*/^}{])|\\s+");
        boolean expect = true;

        for (int i = 0; i < tokens.length; i++) {
            String now = tokens[i].trim();
            if (now.isEmpty()) continue;

            if (isNumeric(now)) {
                double num = Double.parseDouble(now);
                values.push(num);
                expect = false;
            } else if (now.equals("(") || now.equals("{")) {
                op.push(now);
                expect = true;
            } else if (now.equals(")")) {
                while (!op.isEmpty() && !op.peek().equals("(")) {
                    compute(values, op.pop());
                }
                if (!op.isEmpty()) {
                    op.pop(); // Remove "("
                }
                expect = false;
            } else if (now.equals("}")) {
                while (!op.isEmpty() && !op.peek().equals("{")) {
                    compute(values, op.pop());
                }
                if (!op.isEmpty()) {
                    op.pop(); // Remove "{"
                }
                expect = false;
            } else if (current.containsKey(now)) {
                if (now.equals("-")) {
                    if (expect) {
                        // Handle unary minus
                        if (i + 1 < tokens.length) {
                            String nextToken = tokens[i + 1].trim();
                            if (isNumeric(nextToken)) {
                                // If next token is a number
                                values.push(-Double.parseDouble(nextToken));
                                i++;
                                expect = false;
                            } else if (nextToken.equals("(") || nextToken.equals("{")) {
                                // Handle cases like "-(..." by pushing -1 and multiplication
                                values.push(-1.0);
                                op.push("*");
                            } else if (nextToken.equals("+") || nextToken.equals("-")) {
                                // Skip this minus and let the next iteration handle the following operator
                                continue;
                            }
                        }
                    } else {
                        // Binary minus
                        while (!op.isEmpty() && current.get(op.peek()) >= current.get(now)
                                && !op.peek().equals("(") && !op.peek().equals("{")) {
                            compute(values, op.pop());
                        }
                        op.push(now);
                        expect = true;
                    }
                } else if (now.equals("+") && expect) {
                    // Unary plus - just skip it
                    continue;
                } else {
                    // Other operators
                    while (!op.isEmpty() && current.get(op.peek()) >= current.get(now)
                            && !op.peek().equals("(") && !op.peek().equals("{")) {
                        compute(values, op.pop());
                    }
                    op.push(now);
                    expect = true;
                }
            } else if (now.matches("sin|cos|tan|cot|ln|log")) {
                op.push(now);
                expect = true;
            } else {
                throw new IllegalArgumentException("Invalid Input: " + now);
            }
        }

        while (!op.isEmpty()) {
            compute(values, op.pop());
        }

        return values.pop();
    }

/**
 * Applies the operator to the top two values on the stack first of all to calculate the sin cos tan with one value and then 
 * add those two number
 * @param values the stack of operand values
 * @param op the operator to apply
 * @throws ArithmeticException if division by zero or invalid operations occur
 */
    private static void compute(Stack<Double> values, String op) {
        if (op.equals("sin") || op.equals("cos") || op.equals("tan") ||
                op.equals("cot") || op.equals("ln") || op.equals("log")) {
            if (values.isEmpty()) {
                throw new IllegalArgumentException("Invalid Expression: " + op);
            }
            double a = values.pop();
            switch (op) {
                case "sin":
                    values.push(Math.sin(Math.toRadians(a)));
                    break;
                case "cos":
                    values.push(Math.cos(Math.toRadians(a)));
                    break;
                case "tan":
                    values.push(Math.tan(Math.toRadians(a)));
                    break;
                case "cot":
                    values.push(1 / Math.tan(Math.toRadians(a))); // cot(x) = 1 / tan(x)
                    break;
                case "ln":
                    if (a <= 0) {
                        throw new ArithmeticException("Error: Natural logarithm is undefined for non-positive numbers");
                    }
                    values.push(Math.log(a));
                    break;
                case "log":
                    if (a <= 0) {
                        throw new ArithmeticException("Error: Logarithm is undefined for non-positive numbers");
                    }
                    values.push(Math.log10(a));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid expression: " + op);
            }
        } else {
            if (values.size() < 2) {
                throw new IllegalArgumentException("Invalid expression: " + op);
            }

            double b = values.pop();
            double a = values.pop();

            switch (op) {
                case "+":
                    values.push(a + b);
                    break;
                case "-":
                    values.push(a - b);
                    break;
                case "*":
                    values.push(a * b);
                    break;
                case "/":
                    if (b == 0) {
                        throw new ArithmeticException("Error: Division by zero");
                    }
                    values.push(a / b);
                    break;
                case "^":
                    values.push(Math.pow(a, b));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Operator: " + op);
            }
        }
    }
/**
 * Checks if the given string is a valid number
 * @param str the string to check 
 * @return true if the string represent a valid number, false otherwise
 * 
 */
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}