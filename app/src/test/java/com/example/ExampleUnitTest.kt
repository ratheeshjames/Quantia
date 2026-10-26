package com.example

import com.example.domain.AngleUnit
import com.example.domain.CatalogAndConverterEngine
import com.example.domain.MathEngine
import com.example.domain.MatrixAndSolverEngine
import com.example.domain.StatsAndFinanceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun mathEngine_evaluatesScientificAndComplexExpressions() {
        val basic = MathEngine.evaluate("68+12.8-24.89")
        assertEquals(55.91, basic.value.re, 1e-6)

        val trig = MathEngine.evaluate("sin(30) + cos(60)", angleUnit = AngleUnit.DEG)
        assertEquals(1.0, trig.value.re, 1e-6)

        val complex = MathEngine.evaluate("(2+3i)*(1-2i)")
        assertEquals(8.0, complex.value.re, 1e-6)
        assertEquals(-1.0, complex.value.im, 1e-6)

        val comb = MathEngine.evaluate("10nCr3 + 5!")
        assertEquals(240.0, comb.value.re, 1e-6)
    }

    @Test
    fun calculusAndMatrixEngine_computesAccurately() {
        val deriv = MathEngine.numericalDerivative("x^3", 2.0)
        assertEquals(12.0, deriv, 1e-4)

        val integral = MathEngine.numericalIntegral("x^2", 0.0, 3.0)
        assertEquals(9.0, integral, 1e-4)

        val det = MatrixAndSolverEngine.determinant(
            listOf(
                listOf(2.0, 1.0),
                listOf(1.0, 3.0)
            )
        )
        assertEquals(5.0, det, 1e-6)
    }

    @Test
    fun converterAndStatsEngine_computesAccurately() {
        val tempCat = CatalogAndConverterEngine.unitCategories.first { it.id == "temperature" }
        val cUnit = tempCat.units.first { it.symbol == "°C" }
        val fUnit = tempCat.units.first { it.symbol == "°F" }
        val fahrenheit = CatalogAndConverterEngine.convertUnit(tempCat, cUnit, fUnit, 100.0)
        assertEquals(212.0, fahrenheit, 1e-6)

        val stats = StatsAndFinanceEngine.analyzeDataset(listOf(10.0, 20.0, 30.0, 40.0, 50.0))
        assertTrue(stats != null && kotlin.math.abs(stats.mean - 30.0) < 1e-6)
    }
}
