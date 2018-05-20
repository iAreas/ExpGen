package com.mz.expGen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

import com.mz.expGen.operator.OperatorDiv;
import com.mz.expGen.operator.OperatorMul;
import com.mz.expGen.operator.OperatorSub;
import com.mz.expGen.operator.OperatorSum;

public class ExpressionGenerator {

	private Random seed;
	private Random rnd;

	private Operator[] operators = { new OperatorSum(), new OperatorSub(), new OperatorMul(), new OperatorDiv() };

	private int length = 3;

	private int minNum = 1;
	private int maxNum = 100;

	private int minDecimalPlaces = 1;
	private int maxDecimalPlaces = 2;

	private int maxDecimalsInResult = -1;

	private boolean useDecimalNumbers = false;
    private boolean allowParentheses = true;

	/**
	 * Class used to generate random mathematical expressions
	 */
	public ExpressionGenerator() {
		this.rnd = new Random();
		this.seed = new Random();
	}

	/**
	 * Generates a random expression using applied preferences
	 * 
	 * @return generated expressions
	 * @throws IllegalArgumentException
	 *             if only available operator is divide and only number in bounds is
	 *             0
	 */
	public Expression generate() throws IllegalArgumentException {
		boolean notOnlyDiv = true;
		for (Operator checkOperator : this.operators) {
			if (!checkOperator.getClass().equals(OperatorDiv.class)) {
				notOnlyDiv = false;
			}
		}
		// Checks if only available operator is divide

		if (notOnlyDiv && this.maxNum == 0 && this.minNum == 0) {
			throw new IllegalArgumentException(
					"Could not generate solvable expression; only available operator is divide and only number in bounds is 0");
		}
		// Checks if generation of a solvable expression using current rules is possible

		Expression result = null;

		while (result == null) {
			StringBuilder sb = new StringBuilder();
			double first = randomNum();
			sb.append(first < 0 ? "(" + first + ")" : first);
			// Generates and appends the first number
			int brackets = 0;
			for (int i = 0; i < this.length; i++) {
				// Randomizes expression's length

				double number = randomNum();
				// Generates random number

				int operatorIndex = rnd.nextInt(this.operators.length);
				Operator operator = operators[operatorIndex];
				// Generates random operator

				if (operator.getClass().equals(OperatorDiv.class) && number == 0) {
					i--;
					continue;
				}

                sb.append(" ").append(operator.getCharacter()).append(" ");
				// Appends the generated operator

				boolean inBrackets = false;
                if (this.rnd.nextBoolean() && i != this.length - 1 && allowParentheses) {
					sb.append("(");
					inBrackets = true;
					brackets++;
				}
				// Creates a bracket

				sb.append((number < 0 ? "(" + number + ")" : number));
				// Appends the generated number

				if (brackets > 0 && !inBrackets && this.rnd.nextBoolean()) {
					sb.append(")");
					brackets--;
				}
				// Ends a bracket if exists
			}
			for (int i = 0; i < brackets; i++) {
				sb.append(")");
			}
			brackets = 0;
			// Ends all brackets

			try {
				Expression exp = new Expression(
						((sb.toString() + " ").replaceAll(".0 ", " ").replaceAll(".0\\)", ")").trim()));

				if (this.maxDecimalsInResult != -1) {
					BigDecimal solution = exp.getSolution();

					if (this.maxDecimalsInResult < decimalPlaces(solution)) {
						continue;
					}
					// Checks if there are

				}

				result = exp;
				// Finishes the expression

			} catch (Exception e) {
				e.printStackTrace();
            }
		}
		return result;
	}

	/**
	 * Sets operators that will be used in expression. Note that setting that to
	 * only divide might cause conflicts with the generator when some settings (for
	 * example allowDecimalResult to false) are applied, causing generator to take
	 * really long to generate expressions or even crash. Default operators are sum,
	 * subtract, multiply and divide
	 * 
	 * @param operators
	 *            array of operators
	 * @return self, allowing chain methods
	 * @throws IllegalArgumentException
	 *             if array of operators is empty
	 */
	public ExpressionGenerator setOperators(Operator[] operators) throws IllegalArgumentException {
		if (operators.length < 1) {
			throw new IllegalArgumentException("Array of operators can't be empty!");
		}

		this.operators = operators;
		return this;
	}

	/**
	 * Sets maximal and minimal random number used in expressions. Default max is
	 * 100 and min is 1
	 * 
	 * @param minNum
	 *            new maximal number
	 * @param maxNum
	 *            new maximal number
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator setBounds(int minNum, int maxNum) throws IllegalArgumentException {
		if (maxNum < minNum) {
			throw new IllegalArgumentException("Mininal number must be smaller than maximal number!");
		}

		this.maxNum = maxNum;
		this.minNum = minNum;

		return this;
	}

	/**
	 * Sets new expression length. Default is 3 <blockquote>
	 * <table cellpadding=1 cellspacing=0 summary="Split examples showing regex and
	 * result">
	 * <tr>
	 * <th>Length</th>
	 * <th>Result</th>
	 * </tr>
	 * <tr>
	 * <td align=center>1</td>
	 * <td>{@code 241}</td>
	 * </tr>
	 * <tr>
	 * <td align=center>2</td>
	 * <td>{@code 452 + 12}</td>
	 * </tr>
	 * <tr>
	 * <td align=center>3</td>
	 * <td>{@code 114 * 456 - 9}</td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 * 
	 * @param length
	 *            expression's length
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator setLength(int length) {
		this.length = length;
		return this;
	}

	/**
	 * Sets a new random length. This will override current length
	 * 
	 * @param minLength
	 *            minimal length
	 * @param maxLength
	 *            maximal length
	 * @return self, allowing chain methods
	 * @throws IllegalArgumentException
	 *             if maxLength is smaller than minLength or if one of the arguments
	 *             is lesser than 1
	 */
	public ExpressionGenerator setRandomLength(int minLength, int maxLength) throws IllegalArgumentException {
		if (maxLength < minLength) {
			throw new IllegalArgumentException("Mininal length must be smaller than maximal length!");
		}

		if (minLength < 1 || maxLength < 1) {
			throw new IllegalArgumentException("Maximal and minimal length must be bigger than 1!");
		}

		return setLength(this.rnd.nextInt((maxLength + 1) - minLength) + minLength);
	}

	/**
	 * Sets a new seed used in generating random values
	 * 
	 * @param seed
	 *            new seed to use
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator setSeed(long seed) {
		this.seed = new Random(seed);
		this.rnd = new Random(seed);
		return this;
	}

	/**
	 * This will remove the custom seed and set a random seed as new random
	 * generator seed. Default is random
	 * 
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator undoSeed() {
		this.seed = new Random();
		this.rnd = new Random();
		return this;
	}

	/**
	 * Allows usage of decimal numbers in generated expression. Default is false
	 * 
	 * @param useDecimalNumbers
	 *            true to allow decimal numbers, false to not
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator useDecimalNumbers(boolean useDecimalNumbers) {
		this.useDecimalNumbers = useDecimalNumbers;
		return this;
	}

	/**
	 * Sets decimal places bounds. This will have no effect if useDecimalNumbers is
	 * set to false. Set both to -1 to disable decimal place capping. Default max is
	 * 2 and min is 1
	 * 
	 * @param minDecimalPlaces
	 *            minimal decimal places
	 * @param maxDecimalPlaces
	 *            maximal decimal places
	 * @return self, allowing chain methods
	 */
	public ExpressionGenerator setDecimalPlacesBounds(int minDecimalPlaces, int maxDecimalPlaces)
			throws IllegalArgumentException {
		if (maxDecimalPlaces < minDecimalPlaces) {
			throw new IllegalArgumentException("Mininal decimal places must be smaller than maximal decimal places!");
		}

		if (maxDecimalPlaces == -1 || minDecimalPlaces == -1) {
			if (maxDecimalPlaces != -1 || minDecimalPlaces != -1) {
				throw new IllegalArgumentException("You cannot set only one of the bounds to -1, you must set both!");
			}
		}

		if (maxDecimalPlaces < -1 || minDecimalPlaces < -1) {
			throw new IllegalArgumentException("Minimal bounds are -1!");
		}

		this.maxDecimalPlaces = maxDecimalPlaces;
		this.minDecimalPlaces = minDecimalPlaces;

		return this;
	}

	/**
	 * Setting this to more than -1 will only allow numbers with a maximum of N
	 * numbers in expression's result. Generating such expressions might take more
	 * time as random expressions must be bruteforced. As a side-effect, expressions
	 * will also probably contain way less division operators. Default if true
	 *
	 * @param maxDecimalsInResult
	 *            number of maximum decimal places allowed in expression's result,
	 *            -1 to allow any number of decimal places
	 * @return self, allowing chain methods
	 * @throws IllegalArgumentException
	 *             if given parameter is less than -1
	 */
	public ExpressionGenerator setMaxDecimalsInResult(int maxDecimalsInResult) throws IllegalArgumentException {
		if (maxDecimalsInResult < -1) {
			throw new IllegalArgumentException("Minimal maxDecimalsInResult is -1!");
		}

		this.maxDecimalsInResult = maxDecimalsInResult;
		return this;
	}

    /**
     * Copy of {@link #setAllowBrackets(boolean)}, name changed.
     */
    public void setAllowParentheses(boolean allowallowParentheses) {
        this.allowParentheses = allowallowParentheses;
    }

	/**
	 * Set this to false to disable usage of brackets in generated expressions.
	 * Default is true
     *
     * @deprecated Use {@link #setAllowParentheses(boolean)} instead.
	 * @param allowBrackets
	 *            true to allow brackets, false to disallow brackets
	 */
	public void setAllowBrackets(boolean allowBrackets) {
        this.allowParentheses = allowBrackets;
	}

	public Operator[] getOperators() {
		return operators;
	}

	public int getMinNum() {
		return minNum;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public int getLength() {
		return length;
	}

	public boolean doesUseDecimalNumbers() {
		return useDecimalNumbers;
	}

	public int getMinDecimalPlaces() {
		return minDecimalPlaces;
	}

	public int getMaxDecimalPlaces() {
		return maxDecimalPlaces;
	}

	private static int decimalPlaces(BigDecimal n) {
		String text = n.stripTrailingZeros().toPlainString().replaceAll("-", "");
		// Converts number into text

		if (!text.contains(".")) {
			return 0;
		}
		// Checks if number is decimal

		int integerPlaces = text.indexOf('.');
		return text.length() - integerPlaces - 1;
		// Calculates decimal places

	}

	private double randomNum() {
		if (this.useDecimalNumbers) {
			return nextDouble(this.minNum, this.maxNum, nextInt(this.minDecimalPlaces, this.maxDecimalPlaces));

		}
		return nextInt(this.minNum, this.maxNum);
	}

    private double nextDouble(double origin, double bound, int decimalPlaces) {
		double r = (this.rnd.nextLong() >>> 11) * (0x1.0p-53);

        if (origin < bound) {
			r = r * (bound - origin) + origin;
			if (r >= bound)
				r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
		}

		if (decimalPlaces == -1) {
			return r;

		} else if (decimalPlaces == 0) {
			return Math.round(r);

		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < decimalPlaces; i++) {
				sb.append("#");
			}
			DecimalFormat df = new DecimalFormat("#." + sb.toString());
			df.setRoundingMode(RoundingMode.CEILING);
			return Double.valueOf(df.format(Double.valueOf(r)).replaceAll(",", "."));
		}
	}

    private int nextInt(int origin, int max) {
		int r = mix32(this.seed.nextLong());

		int bound = max + 1;
		if (origin < bound) {
			int n = bound - origin, m = n - 1;
			if ((n & m) == 0)
				r = (r & m) + origin;
			else if (n > 0) {
				for (int u = r >>> 1; u + m - (r = u % n) < 0; u = mix32(this.seed.nextLong()) >>> 1)
					;
				r += origin;
			} else {
				while (r < origin || r >= bound)
					r = mix32(this.seed.nextLong());
			}
		}
		return r;
	}

	private static int mix32(long z) {
		z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
		return (int) (((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32);
	}
}
