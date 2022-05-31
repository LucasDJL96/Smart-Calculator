package calculator

import java.math.BigInteger

/**
 * Enum class representing an operation
 *
 * @param symbol the symbol representing the operator
 * @param precedence the precedence of the operator
 * @param act a function that represents the action of the operator. The first
 *            argument corresponds to the right and the second argument to
 *            the left operand
 */
enum class Operator(val symbol: String, private val precedence: Int, val act: (BigInteger, BigInteger) -> BigInteger) {

    /** Addition */
    PLUS("+", 0, { b, a -> a + b }),

    /** Subtraction */
    MINUS("-", 0, { b, a -> a - b }),

    /** Multiplication */
    TIMES("*", 1, { b, a -> a * b }),

    /** Division */
    DIVIDED_BY("/", 1, { b, a -> a / b }),

    /** Exponentiation */
    TO_THE("^", 2, { b, a -> a.pow(b.toInt()) }),

    /** Modulo division */
    MODULO("%", 1, { b, a -> a % b})
    ;

    /**
     * Compares operators by their precedence
     *
     * @param other the operator to compare this with
     *
     * @return Int: positive if this has greater precedence than other, 0 if their
     *              precedence is equal and negative otherwise
     */
    fun compareWith(other: Operator): Int = this.precedence - other.precedence

    companion object {

        /** Map mapping a symbol to its corresponding operator */
        val symbolToOperator = buildMap {
            for (value in Operator.values()) {
                put(value.symbol, value)
            }
        }
    }
}
