package Calculator;
import java.util.*;
import java.lang.Math;
public class Lab2 {
	//Use hashMap to put the arithmetic for the HashMap;
		private static final Map <String, Integer> current = new HashMap<>();
		
		static {
			current.put("+",1);
			current.put("-",1);
			current.put("*",2);
			current.put("/",2);
			current.put("^",3);
			current.put("sin",4);
			current.put("cos",4);
			current.put("tan",4);
			current.put("cot",4);
			current.put("ln",4);
			current.put("log",4);
			current.put("(", 0);
			current.put(")",0);
			current.put("{", 0);
			current.put("}",0);
			
		}
		/**
		 * 
		 * @param args
		 */
		
		
		public static void main (String[]args) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please insert your arithemetic equation: ");
			String number = scanner.nextLine();
			
			try {
				double result = evaluateExpression(number);
				System.out.println("Result: " + result);
			}catch (ArithmeticException e) {
				System.out.println(e.getMessage());
			}
			catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			scanner.close();
		}
		
		
		private static double evaluateExpression(String express) {
		    Stack<Double> values = new Stack<>();
		    Stack<String> op = new Stack<>();

		    StringTokenizer token = new StringTokenizer(express, "+-*/^(){} sin cos tan cot ln log", true);
		    boolean expectNegative = true; // If true, "-" should be treated as unary

		    while (token.hasMoreTokens()) {
		        String now = token.nextToken().trim();
		        if (now.isEmpty()) {
		            continue;
		        }

		        if (isNumeric(now)) {
		            double num = Double.parseDouble(now);
		            values.push(num);
		            expectNegative = false; // After a number, expect an operator
		        } 
		        else if (now.equals("(") || now.equals("{")) {
		            op.push(now);
		            expectNegative = true; // A number might follow, which can be negative
		        } 
		        else if (now.equals(")")) {
		            while (!op.isEmpty() && !op.peek().equals("(")) {
		                compute(values, op.pop());
		            }
		            if (!op.isEmpty()) {
		                op.pop(); // Remove "("
		            }
		        } 
		        else if (now.equals("}")) {
		            while (!op.isEmpty() && !op.peek().equals("{")) {
		                compute(values, op.pop());
		            }
		            if (!op.isEmpty()) {
		                op.pop(); // Remove "{"
		            }
		        } 
		        else if (current.containsKey(now)) {
		            if (now.equals("-") && expectNegative) {
		                // Handle multiple unary minuses
		                int minusCount = 1;
		                while (token.hasMoreTokens()) {
		                    String nextToken = token.nextToken().trim();
		                    if (nextToken.equals("-")) {
		                        minusCount++;
		                    } else {
		                        // Reconstruct the expression after counting minuses
		                        if (isNumeric(nextToken)) {
		                            double num = Double.parseDouble(nextToken);
		                            if (minusCount % 2 == 0) {
		                                values.push(num); // Even number of minuses: positive
		                            } else {
		                                values.push(-num); // Odd number of minuses: negative
		                            }
		                            expectNegative = false; // After a number, expect an operator
		                        } else {
		                            throw new IllegalArgumentException("Invalid expression: Unary minus must be followed by a number");
		                        }
		                        break;
		                    }
		                }
		            } else {
		                // Handle binary operators
		                while (!op.isEmpty() && current.get(op.peek()) >= current.get(now)
		                        && !op.peek().equals("(") && !op.peek().equals("{")) {
		                    compute(values, op.pop());
		                }
		                op.push(now);
		                expectNegative = true; // After an operator, expect a number
		            }
		        } 
		        else {
		            throw new IllegalArgumentException("Invalid Input: " + now);
		        }
		    }

		    while (!op.isEmpty()) {
		        compute(values, op.pop());
		    }

		    return values.pop();
		}
		

		
		
		private static void compute(Stack<Double> values, String op) {
			double b = values.pop();
			double a = values.isEmpty() ? 0 : values.pop();
			
			switch(op) {
			
			case "+" : values.push(a+b); break;
			case "-" : values.push(a-b); break;
			case "*" : values.push(a*b); break;
			case "/" : 
				if (b == 0) {
					throw new ArithmeticException("Error: b can not be 0 ");
				}else {
					values.push(a/b);
					break;
				}
			case "^":
				values.push(Math.pow(a, b));
				break;
				
			default: throw new IllegalArgumentException("Unknow Operator: " + op);
				
				
			}
		}
		
		
		
		private static boolean isNumeric(String str) {
			try {
				Double.parseDouble(str);
				return true;
			} catch(NumberFormatException e) {
				return false;
			}
		}
}
