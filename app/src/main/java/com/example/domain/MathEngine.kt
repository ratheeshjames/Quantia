package com.example.domain

import java.util.Locale
import kotlin.math.*

enum class AngleUnit(val label: String) {
    DEG("DEG"),
    RAD("RAD"),
    GRAD("GRAD")
}

enum class NumberFormatMode(val label: String) {
    DECIMAL("DEC"),
    FRACTION("FRAC"),
    SCIENTIFIC("SCI"),
    ENGINEERING("ENG"),
    DMS("DMS"),
    POLAR("POLAR")
}

data class ComplexVal(val re: Double, val im: Double = 0.0) {
    val isReal: Boolean get() = abs(im) < 1e-11
    val magnitude: Double get() = hypot(re, im)
    val phaseRad: Double get() = atan2(im, re)

    operator fun plus(other: ComplexVal) = ComplexVal(re + other.re, im + other.im)
    operator fun minus(other: ComplexVal) = ComplexVal(re - other.re, im - other.im)
    operator fun times(other: ComplexVal) = ComplexVal(
        re * other.re - im * other.im,
        re * other.im + im * other.re
    )
    operator fun div(other: ComplexVal): ComplexVal {
        val denom = other.re * other.re + other.im * other.im
        if (denom == 0.0) return ComplexVal(Double.NaN, Double.NaN)
        return ComplexVal(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }
    operator fun unaryMinus() = ComplexVal(-re, -im)

    fun conjugate() = ComplexVal(re, -im)

    fun pow(exp: ComplexVal): ComplexVal {
        if (re == 0.0 && im == 0.0) {
            return if (exp.re > 0) ComplexVal(0.0, 0.0) else ComplexVal(Double.NaN, Double.NaN)
        }
        if (isReal && exp.isReal && (re > 0 || exp.re == floor(exp.re))) {
            return ComplexVal(re.pow(exp.re), 0.0)
        }
        val r = magnitude
        val theta = phaseRad
        val lnR = ln(r)
        val newLnR = exp.re * lnR - exp.im * theta
        val newTheta = exp.re * theta + exp.im * lnR
        val mag = exp(newLnR)
        return ComplexVal(mag * cos(newTheta), mag * sin(newTheta))
    }
}

data class EvaluationOutput(
    val value: ComplexVal,
    val formattedPrimary: String,
    val formattedFraction: String,
    val formattedScientific: String,
    val formattedPolar: String,
    val formattedDms: String,
    val primeFactors: String = "",
    val error: String? = null
)

object MathEngine {

    fun evaluate(
        rawExpression: String,
        angleUnit: AngleUnit = AngleUnit.DEG,
        formatMode: NumberFormatMode = NumberFormatMode.DECIMAL,
        variables: Map<String, ComplexVal> = emptyMap()
    ): EvaluationOutput {
        val cleaned = rawExpression.trim()
        if (cleaned.isEmpty()) {
            return EvaluationOutput(
                value = ComplexVal(0.0),
                formattedPrimary = "0",
                formattedFraction = "0",
                formattedScientific = "0",
                formattedPolar = "0 ∠ 0°",
                formattedDms = "0° 0′ 0″"
            )
        }
        return try {
            val balanced = autoBalanceParentheses(cleaned)
            val parser = RecursiveParser(balanced, angleUnit, variables)
            val result = parser.parse()
            if (result.re.isNaN() && result.im.isNaN()) {
                return EvaluationOutput(
                    value = result,
                    formattedPrimary = "Math Error",
                    formattedFraction = "Undefined",
                    formattedScientific = "NaN",
                    formattedPolar = "NaN",
                    formattedDms = "NaN",
                    error = "Result is undefined or out of domain"
                )
            }
            val decStr = formatComplexDecimal(result)
            val fracStr = if (result.isReal) toRationalString(result.re) else decStr
            val sciStr = if (result.isReal) toScientificString(result.re) else decStr
            val engStr = if (result.isReal) toEngineeringString(result.re) else decStr
            val dmsStr = if (result.isReal) toDmsString(result.re) else decStr
            val polarStr = toPolarString(result, angleUnit)
            val factors = if (result.isReal && abs(result.re - round(result.re)) < 1e-9 && abs(result.re) in 2.0..1e12) {
                primeFactorization(round(result.re).toLong())
            } else ""

            val primary = when (formatMode) {
                NumberFormatMode.DECIMAL -> decStr
                NumberFormatMode.FRACTION -> fracStr
                NumberFormatMode.SCIENTIFIC -> sciStr
                NumberFormatMode.ENGINEERING -> engStr
                NumberFormatMode.DMS -> dmsStr
                NumberFormatMode.POLAR -> polarStr
            }
            EvaluationOutput(
                value = result,
                formattedPrimary = primary,
                formattedFraction = fracStr,
                formattedScientific = sciStr,
                formattedPolar = polarStr,
                formattedDms = dmsStr,
                primeFactors = factors
            )
        } catch (e: Exception) {
            EvaluationOutput(
                value = ComplexVal(Double.NaN),
                formattedPrimary = "…",
                formattedFraction = "",
                formattedScientific = "",
                formattedPolar = "",
                formattedDms = "",
                error = e.message ?: "Syntax error"
            )
        }
    }

    fun evaluateRealFunction(
        expression: String,
        xValue: Double,
        angleUnit: AngleUnit = AngleUnit.RAD,
        variables: Map<String, ComplexVal> = emptyMap()
    ): Double {
        return try {
            val varsWithX = variables.toMutableMap()
            varsWithX["x"] = ComplexVal(xValue)
            varsWithX["X"] = ComplexVal(xValue)
            val balanced = autoBalanceParentheses(expression)
            val res = RecursiveParser(balanced, angleUnit, varsWithX).parse()
            if (res.isReal) res.re else Double.NaN
        } catch (_: Exception) {
            Double.NaN
        }
    }

    /**
     * Numerical Derivative f'(x) using 5-point stencil central difference.
     */
    fun numericalDerivative(
        expression: String,
        atX: Double,
        angleUnit: AngleUnit = AngleUnit.RAD,
        variables: Map<String, ComplexVal> = emptyMap()
    ): Double {
        val h = 1e-5 * max(1.0, abs(atX))
        val f1 = evaluateRealFunction(expression, atX - 2 * h, angleUnit, variables)
        val f2 = evaluateRealFunction(expression, atX - h, angleUnit, variables)
        val f3 = evaluateRealFunction(expression, atX + h, angleUnit, variables)
        val f4 = evaluateRealFunction(expression, atX + 2 * h, angleUnit, variables)
        return (f1 - 8 * f2 + 8 * f3 - f4) / (12 * h)
    }

    /**
     * Numerical Definite Integral ∫[a, b] f(x) dx using Composite Simpson's 1/3 Rule.
     */
    fun numericalIntegral(
        expression: String,
        lowerA: Double,
        upperB: Double,
        steps: Int = 200,
        angleUnit: AngleUnit = AngleUnit.RAD,
        variables: Map<String, ComplexVal> = emptyMap()
    ): Double {
        if (lowerA == upperB) return 0.0
        val n = if (steps % 2 == 0) steps else steps + 1
        val h = (upperB - lowerA) / n
        var sum = evaluateRealFunction(expression, lowerA, angleUnit, variables) +
            evaluateRealFunction(expression, upperB, angleUnit, variables)
        for (i in 1 until n) {
            val x = lowerA + i * h
            val fx = evaluateRealFunction(expression, x, angleUnit, variables)
            if (fx.isNaN()) continue
            sum += if (i % 2 == 0) 2.0 * fx else 4.0 * fx
        }
        return sum * (h / 3.0)
    }

    fun autoBalanceParentheses(expr: String): String {
        var openCount = 0
        for (c in expr) {
            if (c == '(') openCount++
            else if (c == ')' && openCount > 0) openCount--
        }
        return if (openCount > 0) expr + ")".repeat(openCount) else expr
    }

    fun formatReal(v: Double): String {
        if (v.isNaN()) return "NaN"
        if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
        val clean = if (abs(v) < 1e-13) 0.0 else v
        if (abs(clean - round(clean)) < 1e-10 && abs(clean) < 1e15) {
            return String.format(Locale.US, "%,d", round(clean).toLong())
        }
        val absVal = abs(clean)
        return if (absVal >= 1e10 || (absVal > 0 && absVal < 1e-5)) {
            String.format(Locale.US, "%.6E", clean)
        } else {
            val raw = String.format(Locale.US, "%.8f", clean).trimEnd('0').trimEnd('.')
            val parts = raw.split(".")
            val intPart = parts[0].toLongOrNull()?.let { String.format(Locale.US, "%,d", it) } ?: parts[0]
            if (parts.size > 1) "$intPart.${parts[1]}" else intPart
        }
    }

    fun formatComplexDecimal(c: ComplexVal): String {
        if (c.re.isNaN() || c.im.isNaN()) return "Undefined"
        if (c.isReal) return formatReal(c.re)
        val reClean = if (abs(c.re) < 1e-12) 0.0 else c.re
        val imAbs = abs(c.im)
        val imStr = if (abs(imAbs - 1.0) < 1e-11) "i" else "${formatReal(imAbs)}i"
        return if (reClean == 0.0) {
            if (c.im < 0) "-$imStr" else imStr
        } else {
            val sign = if (c.im >= 0) " + " else " - "
            "${formatReal(reClean)}$sign$imStr"
        }
    }

    /**
     * Exact rational fraction approximation via Continued Fractions.
     */
    fun toRationalString(value: Double, maxDenom: Long = 1_000_000L): String {
        if (!value.isFinite()) return formatReal(value)
        val sign = if (value < 0) "-" else ""
        val x = abs(value)
        if (abs(x - round(x)) < 1e-10) return "$sign${round(x).toLong()}"

        var h1 = 1L
        var h2 = 0L
        var k1 = 0L
        var k2 = 1L
        var b = x
        for (iter in 0..35) {
            val a = floor(b).toLong()
            val h = a * h1 + h2
            val k = a * k1 + k2
            if (k > maxDenom) break
            h2 = h1; h1 = h
            k2 = k1; k1 = k
            val rem = b - a
            if (abs(x - h1.toDouble() / k1.toDouble()) < 1e-10 || rem < 1e-12) break
            b = 1.0 / rem
        }
        return if (k1 <= 1L || abs(x - h1.toDouble() / k1.toDouble()) > 1e-6) {
            formatReal(value)
        } else if (h1 > k1) {
            val whole = h1 / k1
            val remNum = h1 % k1
            "$sign$h1/$k1  ($sign$whole ${remNum}/$k1)"
        } else {
            "$sign$h1/$k1"
        }
    }

    fun toScientificString(value: Double): String {
        if (!value.isFinite()) return formatReal(value)
        if (value == 0.0) return "0 × 10⁰"
        val exp = floor(log10(abs(value))).toInt()
        val mantissa = value / 10.0.pow(exp)
        return String.format(Locale.US, "%.5f × 10^%d", mantissa, exp)
    }

    fun toEngineeringString(value: Double): String {
        if (!value.isFinite()) return formatReal(value)
        if (value == 0.0) return "0 × 10⁰"
        val exp = floor(log10(abs(value))).toInt()
        val engExp = exp - (exp.mod(3))
        val mantissa = value / 10.0.pow(engExp)
        return String.format(Locale.US, "%.4f × 10^%d", mantissa, engExp)
    }

    fun toDmsString(degrees: Double): String {
        if (!degrees.isFinite()) return formatReal(degrees)
        val sign = if (degrees < 0) "-" else ""
        val total = abs(degrees)
        val d = floor(total).toLong()
        val minFull = (total - d) * 60.0
        val m = floor(minFull).toLong()
        val s = (minFull - m) * 60.0
        return String.format(Locale.US, "%s%d° %02d′ %05.2f″", sign, d, m, s)
    }

    fun toPolarString(c: ComplexVal, angleUnit: AngleUnit): String {
        val r = c.magnitude
        val angleRad = c.phaseRad
        val angleConverted = when (angleUnit) {
            AngleUnit.DEG -> Math.toDegrees(angleRad)
            AngleUnit.RAD -> angleRad
            AngleUnit.GRAD -> angleRad * (200.0 / PI)
        }
        val unitSym = when (angleUnit) {
            AngleUnit.DEG -> "°"
            AngleUnit.RAD -> " rad"
            AngleUnit.GRAD -> " gon"
        }
        return "${formatReal(r)} ∠ ${formatReal(angleConverted)}$unitSym"
    }

    fun primeFactorization(n: Long): String {
        val num = abs(n)
        if (num < 2L) return "$n"
        var temp = num
        val factors = mutableMapOf<Long, Int>()
        var d = 2L
        while (d * d <= temp) {
            while (temp % d == 0L) {
                factors[d] = (factors[d] ?: 0) + 1
                temp /= d
            }
            d += if (d == 2L) 1L else 2L
        }
        if (temp > 1L) {
            factors[temp] = (factors[temp] ?: 0) + 1
        }
        if (factors.size == 1 && factors.values.first() == 1) {
            return "$num is Prime"
        }
        return factors.entries.joinToString(" × ") { (p, exp) ->
            if (exp == 1) "$p" else "$p^$exp"
        }
    }

    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a / gcd(a, b) * b)
    }

    /**
     * Lanczos approximation for Gamma function Γ(z) and non-integer factorials.
     */
    fun gamma(z: Double): Double {
        if (z <= 0.0 && z == floor(z)) return Double.NaN
        val p = doubleArrayOf(
            676.5203681218851,
            -1259.1392167224028,
            771.3234287776531,
            -176.6150291621406,
            12.507343278686905,
            -0.13857109526572012,
            9.984369578019572e-6,
            1.5056327351493116e-7
        )
        if (z < 0.5) {
            return PI / (sin(PI * z) * gamma(1.0 - z))
        }
        val shifted = z - 1.0
        var x = 0.9999999999998099
        for (i in p.indices) {
            x += p[i] / (shifted + i + 1.0)
        }
        val t = shifted + p.size - 0.5
        return sqrt(2.0 * PI) * t.pow(shifted + 0.5) * exp(-t) * x
    }

    fun factorial(x: Double): Double {
        if (x < 0.0) return Double.NaN
        if (abs(x - round(x)) < 1e-10 && x <= 170.0) {
            var res = 1.0
            val n = round(x).toInt()
            for (i in 2..n) res *= i.toDouble()
            return res
        }
        return gamma(x + 1.0)
    }

    fun nCr(n: Double, r: Double): Double {
        if (r < 0 || n < 0 || r > n) return 0.0
        return factorial(n) / (factorial(r) * factorial(n - r))
    }

    fun nPr(n: Double, r: Double): Double {
        if (r < 0 || n < 0 || r > n) return 0.0
        return factorial(n) / factorial(n - r)
    }

    private class RecursiveParser(
        raw: String,
        private val angleUnit: AngleUnit,
        private val variables: Map<String, ComplexVal>
    ) {
        private val str = preprocessExpression(raw)
        private var pos = -1
        private var ch = 0

        init {
            nextChar()
        }

        private fun preprocessExpression(input: String): String {
            return input
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("π", "pi")
                .replace("√(", "sqrt(")
                .replace("∛(", "cbrt(")
                .replace("φ", "phi")
                .replace(" ", "")
        }

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): ComplexVal {
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected '${str[pos]}'")
            return x
        }

        private fun parseExpression(): ComplexVal {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): ComplexVal {
            var x = parseCombinatorics()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseCombinatorics()
                    eat('/'.code) -> x /= parseCombinatorics()
                    eat('∠'.code) -> {
                        val angle = parseCombinatorics().re
                        val rad = toRadiansByUnit(angle)
                        x = ComplexVal(x.re * cos(rad), x.re * sin(rad))
                    }
                    // Implicit multiplication e.g. 2pi, 3sin(x), (2)(3), 4i, 5A
                    isImplicitMultNext() -> x *= parseCombinatorics()
                    else -> return x
                }
            }
        }

        private fun isImplicitMultNext(): Boolean {
            if (ch == '('.code) return true
            if (ch.toChar().isLetter()) {
                val rem = str.substring(pos)
                if (rem.startsWith("C(") || rem.startsWith("P(") || rem.startsWith("mod")) return false
                return true
            }
            return false
        }

        private fun parseCombinatorics(): ComplexVal {
            var x = parsePower()
            while (true) {
                val rem = if (pos < str.length) str.substring(pos) else ""
                when {
                    rem.startsWith("nCr") -> {
                        repeat(3) { nextChar() }
                        val r = parsePower()
                        x = ComplexVal(nCr(x.re, r.re))
                    }
                    rem.startsWith("nPr") -> {
                        repeat(3) { nextChar() }
                        val r = parsePower()
                        x = ComplexVal(nPr(x.re, r.re))
                    }
                    rem.startsWith("mod") -> {
                        repeat(3) { nextChar() }
                        val m = parsePower()
                        x = ComplexVal(x.re.mod(m.re))
                    }
                    else -> return x
                }
            }
        }

        private fun parsePower(): ComplexVal {
            var x = parsePostfix()
            if (eat('^'.code)) {
                val exp = parsePower() // Right-associative
                x = x.pow(exp)
            }
            return x
        }

        private fun parsePostfix(): ComplexVal {
            var x = parseFactor()
            while (true) {
                when {
                    eat('!'.code) -> x = ComplexVal(factorial(x.re))
                    eat('%'.code) -> x *= ComplexVal(0.01)
                    eat('²'.code) -> x = x * x
                    eat('³'.code) -> x = x * x * x
                    else -> return x
                }
            }
        }

        private fun parseFactor(): ComplexVal {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()
            if (eat('√'.code)) {
                val arg = parseFactor()
                return arg.pow(ComplexVal(0.5))
            }

            val startPos = pos
            if (eat('('.code)) {
                val x = parseExpression()
                eat(')'.code)
                return x
            }

            if (ch in '0'.code..'9'.code || ch == '.'.code) {
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                if (ch == 'E'.code && pos + 1 < str.length && (str[pos + 1] == '+' || str[pos + 1] == '-' || str[pos + 1].isDigit())) {
                    nextChar()
                    if (ch == '+'.code || ch == '-'.code) nextChar()
                    while (ch in '0'.code..'9'.code) nextChar()
                }
                val num = str.substring(startPos, pos).toDouble()
                return ComplexVal(num, 0.0)
            }

            if (ch.toChar().isLetter() || ch == '_'.code) {
                while (ch.toChar().isLetterOrDigit() || ch == '_'.code) nextChar()
                val name = str.substring(startPos, pos)

                if (eat('('.code)) {
                    val args = mutableListOf<ComplexVal>()
                    if (!eat(')'.code)) {
                        do {
                            args.add(parseExpression())
                        } while (eat(','.code))
                        eat(')'.code)
                    }
                    return applyFunction(name.lowercase(Locale.US), args)
                }

                return resolveIdentifier(name)
            }

            throw IllegalArgumentException("Unexpected token")
        }

        private fun resolveIdentifier(name: String): ComplexVal {
            variables[name]?.let { return it }
            return when (name) {
                "pi", "PI" -> ComplexVal(PI)
                "e" -> ComplexVal(E)
                "phi", "PHI" -> ComplexVal((1.0 + sqrt(5.0)) / 2.0)
                "i", "j" -> ComplexVal(0.0, 1.0)
                "c" -> ComplexVal(299792458.0)
                "g" -> ComplexVal(9.80665)
                "h" -> ComplexVal(6.62607015e-34)
                "G" -> ComplexVal(6.67430e-11)
                "NA" -> ComplexVal(6.02214076e23)
                "kB" -> ComplexVal(1.380649e-23)
                "Ans", "ans" -> variables["Ans"] ?: ComplexVal(0.0)
                else -> variables[name.uppercase(Locale.US)] ?: ComplexVal(0.0)
            }
        }

        private fun toRadiansByUnit(v: Double): Double = when (angleUnit) {
            AngleUnit.DEG -> Math.toRadians(v)
            AngleUnit.RAD -> v
            AngleUnit.GRAD -> v * (PI / 200.0)
        }

        private fun fromRadiansByUnit(rad: Double): Double = when (angleUnit) {
            AngleUnit.DEG -> Math.toDegrees(rad)
            AngleUnit.RAD -> rad
            AngleUnit.GRAD -> rad * (200.0 / PI)
        }

        private fun applyFunction(fn: String, args: List<ComplexVal>): ComplexVal {
            val a0 = args.firstOrNull() ?: ComplexVal(0.0)
            val a1 = args.getOrNull(1) ?: ComplexVal(0.0)
            val x = a0.re

            return when (fn) {
                "sin" -> if (a0.isReal) ComplexVal(sin(toRadiansByUnit(x))) else ComplexVal(sin(a0.re) * cosh(a0.im), cos(a0.re) * sinh(a0.im))
                "cos" -> if (a0.isReal) ComplexVal(cos(toRadiansByUnit(x))) else ComplexVal(cos(a0.re) * cosh(a0.im), -sin(a0.re) * sinh(a0.im))
                "tan" -> if (a0.isReal) ComplexVal(tan(toRadiansByUnit(x))) else applyFunction("sin", args) / applyFunction("cos", args)
                "cot" -> ComplexVal(1.0 / tan(toRadiansByUnit(x)))
                "sec" -> ComplexVal(1.0 / cos(toRadiansByUnit(x)))
                "csc" -> ComplexVal(1.0 / sin(toRadiansByUnit(x)))
                "asin", "arcsin" -> ComplexVal(fromRadiansByUnit(asin(x)))
                "acos", "arccos" -> ComplexVal(fromRadiansByUnit(acos(x)))
                "atan", "arctan" -> ComplexVal(fromRadiansByUnit(atan(x)))
                "atan2" -> ComplexVal(fromRadiansByUnit(atan2(x, a1.re)))
                "sinh" -> ComplexVal(sinh(x))
                "cosh" -> ComplexVal(cosh(x))
                "tanh" -> ComplexVal(tanh(x))
                "asinh" -> ComplexVal(asinh(x))
                "acosh" -> ComplexVal(acosh(x))
                "atanh" -> ComplexVal(atanh(x))
                "sqrt" -> if (a0.isReal && x >= 0) ComplexVal(sqrt(x)) else if (a0.isReal) ComplexVal(0.0, sqrt(-x)) else a0.pow(ComplexVal(0.5))
                "cbrt" -> ComplexVal(cbrt(x))
                "nroot", "root" -> a0.pow(ComplexVal(1.0 / a1.re))
                "ln" -> if (a0.isReal && x > 0) ComplexVal(ln(x)) else ComplexVal(ln(a0.magnitude), a0.phaseRad)
                "log", "log10" -> if (args.size >= 2) ComplexVal(ln(x) / ln(a1.re)) else ComplexVal(log10(x))
                "log2" -> ComplexVal(ln(x) / ln(2.0))
                "exp" -> ComplexVal(exp(a0.re) * cos(a0.im), exp(a0.re) * sin(a0.im))
                "abs" -> ComplexVal(a0.magnitude)
                "arg", "phase" -> ComplexVal(fromRadiansByUnit(a0.phaseRad))
                "conj" -> a0.conjugate()
                "re" -> ComplexVal(a0.re)
                "im" -> ComplexVal(a0.im)
                "polar" -> {
                    val rad = toRadiansByUnit(a1.re)
                    ComplexVal(x * cos(rad), x * sin(rad))
                }
                "floor" -> ComplexVal(floor(x))
                "ceil" -> ComplexVal(ceil(x))
                "round" -> ComplexVal(round(x))
                "frac" -> ComplexVal(x - truncate(x))
                "sgn", "sign" -> ComplexVal(sign(x))
                "fact", "gamma" -> ComplexVal(if (fn == "fact") factorial(x) else gamma(x))
                "ncr" -> ComplexVal(nCr(x, a1.re))
                "npr" -> ComplexVal(nPr(x, a1.re))
                "gcd" -> ComplexVal(gcd(round(x).toLong(), round(a1.re).toLong()).toDouble())
                "lcm" -> ComplexVal(lcm(round(x).toLong(), round(a1.re).toLong()).toDouble())
                "mod" -> ComplexVal(x.mod(a1.re))
                "deg" -> ComplexVal(Math.toDegrees(x))
                "rad" -> ComplexVal(Math.toRadians(x))
                "hypot" -> ComplexVal(hypot(x, a1.re))
                else -> throw IllegalArgumentException("Unknown function: $fn")
            }
        }
    }
}
