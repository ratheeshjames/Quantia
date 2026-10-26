package com.example.domain

import kotlin.math.*

data class DescriptiveStatsResult(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: String,
    val min: Double,
    val max: Double,
    val range: Double,
    val sampleStdDev: Double,
    val popStdDev: Double,
    val sampleVariance: Double,
    val popVariance: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double,
    val geometricMean: Double,
    val harmonicMean: Double
)

data class LinearRegressionResult(
    val slopeM: Double,
    val interceptB: Double,
    val correlationR: Double,
    val rSquared: Double,
    val equationText: String,
    val predictedYAtTarget: Double
)

data class DistributionResult(
    val title: String,
    val exactProb: Double,
    val cumulativeLower: Double,
    val cumulativeUpper: Double,
    val expectedMean: Double,
    val variance: Double
)

data class FinancialAmortizationResult(
    val monthlyPayment: Double,
    val totalPayment: Double,
    val totalInterest: Double,
    val principalRatio: Float,
    val futureValueCompound: Double,
    val effectiveAnnualRate: Double
)

object StatsAndFinanceEngine {

    fun analyzeDataset(values: List<Double>): DescriptiveStatsResult? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val n = sorted.size
        val sum = sorted.sum()
        val mean = sum / n
        val median = percentile(sorted, 0.5)
        val q1 = percentile(sorted, 0.25)
        val q3 = percentile(sorted, 0.75)
        val iqr = q3 - q1

        val freqMap = sorted.groupingBy { (it * 1e6).roundToLong() }.eachCount()
        val maxFreq = freqMap.values.maxOrNull() ?: 1
        val modeStr = if (maxFreq <= 1) {
            "No distinct mode"
        } else {
            freqMap.filterValues { it == maxFreq }.keys
                .map { it / 1e6 }
                .joinToString(", ") { MathEngine.formatReal(it) }
        }

        val sqDiffSum = sorted.sumOf { (it - mean) * (it - mean) }
        val popVar = sqDiffSum / n
        val sampleVar = if (n > 1) sqDiffSum / (n - 1) else 0.0

        val geoMean = if (sorted.all { it > 0 }) {
            exp(sorted.sumOf { ln(it) } / n)
        } else Double.NaN

        val harmMean = if (sorted.all { it != 0.0 }) {
            n / sorted.sumOf { 1.0 / it }
        } else Double.NaN

        return DescriptiveStatsResult(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            mode = modeStr,
            min = sorted.first(),
            max = sorted.last(),
            range = sorted.last() - sorted.first(),
            sampleStdDev = sqrt(sampleVar),
            popStdDev = sqrt(popVar),
            sampleVariance = sampleVar,
            popVariance = popVar,
            q1 = q1,
            q3 = q3,
            iqr = iqr,
            geometricMean = geoMean,
            harmonicMean = harmMean
        )
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        if (sorted.size == 1) return sorted[0]
        val pos = p * (sorted.size - 1)
        val lower = floor(pos).toInt()
        val upper = ceil(pos).toInt()
        if (lower == upper) return sorted[lower]
        val weight = pos - lower
        return sorted[lower] * (1.0 - weight) + sorted[upper] * weight
    }

    fun computeLinearRegression(
        xValues: List<Double>,
        yValues: List<Double>,
        targetX: Double = 10.0
    ): LinearRegressionResult? {
        val n = minOf(xValues.size, yValues.size)
        if (n < 2) return null
        val xs = xValues.take(n)
        val ys = yValues.take(n)
        val meanX = xs.average()
        val meanY = ys.average()
        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0
        for (i in 0 until n) {
            val dx = xs[i] - meanX
            val dy = ys[i] - meanY
            sxx += dx * dx
            syy += dy * dy
            sxy += dx * dy
        }
        val slope = if (sxx > 1e-12) sxy / sxx else 0.0
        val intercept = meanY - slope * meanX
        val r = if (sxx > 0 && syy > 0) (sxy / sqrt(sxx * syy)).coerceIn(-1.0, 1.0) else 0.0
        val sign = if (intercept >= 0) "+" else "−"
        val eq = "ŷ = ${MathEngine.formatReal(slope)}x $sign ${MathEngine.formatReal(abs(intercept))}"
        return LinearRegressionResult(
            slopeM = slope,
            interceptB = intercept,
            correlationR = r,
            rSquared = r * r,
            equationText = eq,
            predictedYAtTarget = slope * targetX + intercept
        )
    }

    // Normal Distribution N(μ, σ²)
    fun normalDistribution(x: Double, mean: Double, stdDev: Double): DistributionResult {
        val sigma = stdDev.coerceAtLeast(1e-9)
        val z = (x - mean) / sigma
        val pdf = (1.0 / (sigma * sqrt(2.0 * PI))) * exp(-0.5 * z * z)
        val cdf = 0.5 * (1.0 + erf(z / sqrt(2.0)))
        return DistributionResult(
            title = "Normal N(μ=${MathEngine.formatReal(mean)}, σ=${MathEngine.formatReal(sigma)})  [Z = ${MathEngine.formatReal(z)}]",
            exactProb = pdf,
            cumulativeLower = cdf.coerceIn(0.0, 1.0),
            cumulativeUpper = (1.0 - cdf).coerceIn(0.0, 1.0),
            expectedMean = mean,
            variance = sigma * sigma
        )
    }

    // Binomial Distribution B(n, p)
    fun binomialDistribution(kInput: Int, nInput: Int, pInput: Double): DistributionResult {
        val n = nInput.coerceIn(1, 160)
        val k = kInput.coerceIn(0, n)
        val p = pInput.coerceIn(0.0, 1.0)
        fun pmf(idx: Int): Double = MathEngine.nCr(n.toDouble(), idx.toDouble()) * p.pow(idx) * (1.0 - p).pow(n - idx)
        val exact = pmf(k)
        var cdf = 0.0
        for (i in 0..k) cdf += pmf(i)
        return DistributionResult(
            title = "Binomial B(n=$n, p=${MathEngine.formatReal(p)}) at k=$k",
            exactProb = exact.coerceIn(0.0, 1.0),
            cumulativeLower = cdf.coerceIn(0.0, 1.0),
            cumulativeUpper = (1.0 - cdf + exact).coerceIn(0.0, 1.0),
            expectedMean = n * p,
            variance = n * p * (1.0 - p)
        )
    }

    // Poisson Distribution Pois(λ)
    fun poissonDistribution(kInput: Int, lambdaInput: Double): DistributionResult {
        val k = kInput.coerceIn(0, 150)
        val lambda = lambdaInput.coerceAtLeast(0.01)
        fun pmf(idx: Int): Double = (lambda.pow(idx) * exp(-lambda)) / MathEngine.factorial(idx.toDouble())
        val exact = pmf(k)
        var cdf = 0.0
        for (i in 0..k) cdf += pmf(i)
        return DistributionResult(
            title = "Poisson Pois(λ=${MathEngine.formatReal(lambda)}) at k=$k",
            exactProb = exact.coerceIn(0.0, 1.0),
            cumulativeLower = cdf.coerceIn(0.0, 1.0),
            cumulativeUpper = (1.0 - cdf + exact).coerceIn(0.0, 1.0),
            expectedMean = lambda,
            variance = lambda
        )
    }

    // Abramowitz & Stegun approximation for error function erf(x)
    private fun erf(x: Double): Double {
        val sign = if (x < 0) -1.0 else 1.0
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911
        val absX = abs(x)
        val t = 1.0 / (1.0 + p * absX)
        val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-absX * absX)
        return sign * y
    }

    // Financial Amortization & Compound Growth
    fun computeLoanAndTvm(
        principal: Double,
        annualRatePercent: Double,
        years: Double,
        compoundsPerYear: Int = 12,
        monthlyContribution: Double = 0.0
    ): FinancialAmortizationResult {
        val p = principal.coerceAtLeast(0.0)
        val rAnnual = annualRatePercent / 100.0
        val months = (years * 12.0).coerceAtLeast(1.0)
        val rMonthly = rAnnual / 12.0

        val monthlyPayment = if (abs(rMonthly) < 1e-9) {
            p / months
        } else {
            p * (rMonthly * (1.0 + rMonthly).pow(months)) / ((1.0 + rMonthly).pow(months) - 1.0)
        }
        val totalPayment = monthlyPayment * months
        val totalInterest = (totalPayment - p).coerceAtLeast(0.0)
        val principalRatio = if (totalPayment > 0) (p / totalPayment).toFloat().coerceIn(0f, 1f) else 1f

        val m = compoundsPerYear.coerceAtLeast(1)
        val nPeriods = years * m
        val rPeriod = rAnnual / m
        val fvPrincipal = p * (1.0 + rPeriod).pow(nPeriods)
        val fvContrib = if (abs(rMonthly) < 1e-9) {
            monthlyContribution * months
        } else {
            monthlyContribution * (((1.0 + rMonthly).pow(months) - 1.0) / rMonthly)
        }
        val ear = ((1.0 + rPeriod).pow(m) - 1.0) * 100.0

        return FinancialAmortizationResult(
            monthlyPayment = monthlyPayment,
            totalPayment = totalPayment,
            totalInterest = totalInterest,
            principalRatio = principalRatio,
            futureValueCompound = fvPrincipal + fvContrib,
            effectiveAnnualRate = ear
        )
    }
}
