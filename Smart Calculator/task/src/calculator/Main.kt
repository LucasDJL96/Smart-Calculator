package calculator

import java.util.Stack
import java.math.BigInteger

/** Max parenthesis depth the calculator can handle */
const val maxParenthesisDepth = 5

/** Regex matching spaces */
val spaces = Regex("""\s++""")

/** Regex matching sequences of letters */
val letters = Regex("""[A-Za-z]++""")

/** String representing either sequences of letters or sequences of digits */
const val words = """[A-Za-z]++|\d++"""

/** String representing the possible operators, with possible unary minus afterwards */
const val operators = """\+-?|--?|\^-?|\*-?|/-?|%-?"""

/** Regex matching integers */
val numbers = Regex("""-?\d++""")

/** Regex matching sequences of + and - */
val signs = Regex("""[+-]++""")

/** String representing a full operation */
val expressionRegex by lazy {
    var expr = """-?($words)(($operators)($words))*+"""
    for (i in 0 until maxParenthesisDepth) {
        expr = """-?($words|\(${expr}\))(($operators)($words|\(${expr}\)))*+"""
    }
    expr
}

/** Regex matching a full operation */
val calculationRegex = Regex(expressionRegex)

/** Regex matching integers or operators (not unary minus) in a calculation*/
val splitter = Regex("""((?<=[*/^%(])-\d++|(?<!.)-\d++|\d++|[*+-/^%()])""")

/** Map from variable names to their values */
val variables = mutableMapOf<String, BigInteger>()

fun main() {
    while (true) {
        val input = readln()
        if (input.startsWith('/')) {
            if (input == "/exit") {
                println("Bye!")
                break
            } else if (input == "/help") {
                println("The program performs addition, subtraction, multiplication, " +
                    "integer division, modulo and exponentiation operation. " +
                    "It can also work with variables.")
                continue
            } else {
                println("Unknown command")
                continue
            }
        }
        if (input == "") continue
        try {
            if (input.contains('=')) parseAssignment(input)
            else parseCalculation(formatOperation(input))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }
}

/**
 * Gets the value of a variable or throws an IllegalArgumentException if no such variable exists
 *
 * @param variable the name of the variable
 *
 * @return BigInteger the value of the variable
 *
 * @throws IllegalArgumentException if no such variable exists
 */
fun varToInt(variable: String): BigInteger =
    variables.getOrElse(variable) { throw IllegalArgumentException("Unknown variable $variable") }

/**
 * Handles the control of the input in the case where it's an assignment
 *
 * @param input the input
 */
fun parseAssignment(input: String) {
    val sides = input.replace(spaces, "").split(Regex("="), 2)
    if (sides[0].matches(letters) && sides[1].matches(calculationRegex)) {
        variables[sides[0]] = calculate(formatOperation(sides[1]))
    } else if (!sides[0].matches(letters)) {
        println("Invalid identifier")
        return
    } else {
        println("Invalid assignment")
    }
}

/**
 * Handles the control of the input in the case where it's a calculation
 *
 * @param input the input
 */
fun parseCalculation(input: String) {
    if (input.matches(calculationRegex)) {
        println(calculate(input))
    } else {
        println("Invalid expression")
    }
}

/**
 * Performs the calculation described by the input. The input must conform to that specified
 * by the method formatCalculation
 *
 * @param input the input
 *
 * @return BigInteger the result of the calculation
 */
fun calculate(input: String): BigInteger {
    val infix = splitter.findAll(input).map { it.value }.toList()

    val postfix = transformInfixToPostfix(infix)

    return computeFromPostfix(postfix)
}

/**
 * Transforms a list representing an operation in infix notation to a list
 * representing the operation in postfix notation
 *
 * @param infix the list representing the operation in infix notation
 *
 * @return List<String> a list representing the operation in postfix notation
 */
fun transformInfixToPostfix(infix: List<String>): List<String> {
    val opStack = Stack<String>()
    val postfix = mutableListOf<String>()
    for (i in infix) {
        if (i.matches(numbers)) {
            postfix.add(i)
        } else {
            getNextPostfixStep(i, opStack, postfix)
        }
    }
    while (opStack.isNotEmpty()) {
        postfix.add(opStack.pop())
    }
    return postfix
}


/**
 * Controls how a not number is added to the postfix notation
 *
 * @param i the new element
 * @param opStack the current operator stack
 * @param postfix the list representing the postfix notation
 */
fun getNextPostfixStep(i: String, opStack: Stack<String>, postfix: MutableList<String>) {
    when {
        i == "(" || opStack.isEmpty() || opStack.peek() == "(" -> opStack.push(i)
        i == ")" -> {
            while (opStack.peek() != "(") {
                postfix.add(opStack.pop())
            }
            opStack.pop()
        }
        Operator.symbolToOperator[i]!!
            .compareWith(Operator.symbolToOperator[opStack.peek()]!!) > 0 -> opStack.push(i)
        else -> {
            val op = Operator.symbolToOperator[i]!!
            while (opStack.isNotEmpty() && opStack.peek() != "(" &&
                    op.compareWith(Operator.symbolToOperator[opStack.peek()]!!) <= 0) {
                postfix.add(opStack.pop())
            }
            opStack.push(i)
        }
    }
}

/**
 * Computes the result of an operation represented in postfix notation
 *
 * @param postfix: the list representing the operation in postfix notation
 *
 * @return BigInteger: the result of the calculation
 */
fun computeFromPostfix(postfix: List<String>): BigInteger {
    val result = Stack<BigInteger>()
    for (i in postfix) {
        if (i.matches(numbers)) {
            result.push(i.toBigInteger())
        } else {
            val op = Operator.symbolToOperator[i]!!
            result.push(op.act(result.pop(), result.pop()))
        }
    }

    return result.pop()
}

/**
 * Formats the given input to replace spaces, variables by their values,
 * and remove unnecessary signs
 *
 * @param input: the string to format
 *
 * @return String: the input formatted
 */
fun formatOperation(input: String): String =
    input.replace(spaces, "")
        .replace(letters) { varToInt(it.value).toString() }
        .replace(signs) { if (it.value.count('-') % 2 == 0) "+" else "-" }
        .replace("*+", "*")
        .replace("/+", "/")
        .replace("(+", "(")

/**
 * Counts the number of characters in this string matching the given character
 *
 * @param char the character to match
 *
 * @return Int the number of matches
 */
fun String.count(char: Char): Int = count { it == char }
