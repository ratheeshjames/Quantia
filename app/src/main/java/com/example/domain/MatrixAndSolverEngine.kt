package com.example.domain

import kotlin.math.*

data class MatrixResult(
    val title: String,
    val matrix: List<List<Double>>? = null,
    val scalarText: String? = null,
    val steps: List<String> = emptyList()
)

data class VectorResult(
    val dotProduct: Double,
    val crossProduct: List<Double>,
    val normU: Double,
    val normV: Double,
    val angleDegrees: Double,
    val angleRadians: Double,
    val unitU: List<Double>,
    val projectionUOnV: List<Double>
)

data class EquationSolution(
    val title: String,
    val roots: List<String>,
    val discriminantInfo: String,
    val steps: List<String>
)

object MatrixAndSolverEngine {

    fun add(a: List<List<Double>>, b: List<List<Double>>): MatrixResult {
        val n = a.size
        val res = List(n) { r -> List(n) { c -> a[r][c] + b[r][c] } }
        return MatrixResult("Matrix Addition (A + B)", matrix = res)
    }

    fun subtract(a: List<List<Double>>, b: List<List<Double>>): MatrixResult {
        val n = a.size
        val res = List(n) { r -> List(n) { c -> a[r][c] - b[r][c] } }
        return MatrixResult("Matrix Subtraction (A − B)", matrix = res)
    }

    fun multiply(a: List<List<Double>>, b: List<List<Double>>): MatrixResult {
        val n = a.size
        val res = List(n) { r ->
            List(n) { c ->
                var sum = 0.0
                for (k in 0 until n) sum += a[r][k] * b[k][c]
                sum
            }
        }
        return MatrixResult("Matrix Product (A × B)", matrix = res)
    }

    fun transpose(a: List<List<Double>>): MatrixResult {
        val n = a.size
        val res = List(n) { r -> List(n) { c -> a[c][r] } }
        return MatrixResult("Transpose (Aᵀ)", matrix = res)
    }

    fun power(a: List<List<Double>>, exp: Int): MatrixResult {
        var acc = a
        repeat((exp - 1).coerceAtLeast(1)) {
            acc = multiply(acc, a).matrix ?: a
        }
        return MatrixResult("Matrix Power (A^$exp)", matrix = acc)
    }

    fun trace(a: List<List<Double>>): Double {
        var sum = 0.0
        for (i in a.indices) sum += a[i][i]
        return sum
    }

    fun determinant(a: List<List<Double>>): Double {
        val n = a.size
        val m = Array(n) { r -> DoubleArray(n) { c -> a[r][c] } }
        var det = 1.0
        for (i in 0 until n) {
            var pivot = i
            for (r in i + 1 until n) {
                if (abs(m[r][i]) > abs(m[pivot][i])) pivot = r
            }
            if (abs(m[pivot][i]) < 1e-12) return 0.0
            if (pivot != i) {
                val tmp = m[i]; m[i] = m[pivot]; m[pivot] = tmp
                det = -det
            }
            det *= m[i][i]
            for (r in i + 1 until n) {
                val factor = m[r][i] / m[i][i]
                for (c in i until n) {
                    m[r][c] -= factor * m[i][c]
                }
            }
        }
        return if (abs(det) < 1e-12) 0.0 else det
    }

    fun rrefAndRank(a: List<List<Double>>): Pair<List<List<Double>>, Int> {
        val n = a.size
        val m = Array(n) { r -> DoubleArray(n) { c -> a[r][c] } }
        var lead = 0
        var rank = 0
        for (r in 0 until n) {
            if (lead >= n) break
            var i = r
            while (abs(m[i][lead]) < 1e-11) {
                i++
                if (i == n) {
                    i = r
                    lead++
                    if (lead == n) break
                }
            }
            if (lead == n) break
            val temp = m[r]; m[r] = m[i]; m[i] = temp
            val div = m[r][lead]
            if (abs(div) >= 1e-11) {
                for (j in 0 until n) m[r][j] /= div
                rank++
            }
            for (k in 0 until n) {
                if (k != r) {
                    val mult = m[k][lead]
                    for (j in 0 until n) m[k][j] -= mult * m[r][j]
                }
            }
            lead++
        }
        val list = m.map { row -> row.map { if (abs(it) < 1e-12) 0.0 else it } }
        return list to rank
    }

    fun inverse(a: List<List<Double>>): MatrixResult {
        val n = a.size
        val det = determinant(a)
        if (abs(det) < 1e-11) {
            return MatrixResult(
                title = "Inverse (A⁻¹)",
                scalarText = "Singular Matrix (det = 0) — Not Invertible"
            )
        }
        val aug = Array(n) { r ->
            DoubleArray(2 * n) { c ->
                if (c < n) a[r][c] else if (c - n == r) 1.0 else 0.0
            }
        }
        for (i in 0 until n) {
            var pivot = i
            for (r in i + 1 until n) {
                if (abs(aug[r][i]) > abs(aug[pivot][i])) pivot = r
            }
            val tmp = aug[i]; aug[i] = aug[pivot]; aug[pivot] = tmp
            val div = aug[i][i]
            for (c in 0 until 2 * n) aug[i][c] /= div
            for (r in 0 until n) {
                if (r != i) {
                    val factor = aug[r][i]
                    for (c in 0 until 2 * n) aug[r][c] -= factor * aug[i][c]
                }
            }
        }
        val inv = List(n) { r ->
            List(n) { c ->
                val v = aug[r][c + n]
                if (abs(v) < 1e-12) 0.0 else v
            }
        }
        return MatrixResult(
            title = "Inverse (A⁻¹)",
            matrix = inv,
            scalarText = "det(A) = ${MathEngine.formatReal(det)}"
        )
    }

    fun eigenvaluesSummary(a: List<List<Double>>): String {
        val n = a.size
        val tr = trace(a)
        val det = determinant(a)
        if (n == 2) {
            val disc = tr * tr - 4.0 * det
            return if (disc >= 0) {
                val l1 = (tr + sqrt(disc)) / 2.0
                val l2 = (tr - sqrt(disc)) / 2.0
                "λ₁ = ${MathEngine.formatReal(l1)},  λ₂ = ${MathEngine.formatReal(l2)}"
            } else {
                val re = tr / 2.0
                val im = sqrt(-disc) / 2.0
                "λ₁,₂ = ${MathEngine.formatReal(re)} ± ${MathEngine.formatReal(im)}i"
            }
        }
        // Power iteration dominant eigenvalue estimate for 3x3 / 4x4
        var v = DoubleArray(n) { 1.0 }
        var lambda = 0.0
        repeat(40) {
            val next = DoubleArray(n) { r ->
                var s = 0.0
                for (c in 0 until n) s += a[r][c] * v[c]
                s
            }
            val maxAbs = next.maxByOrNull { abs(it) } ?: 1.0
            if (abs(maxAbs) > 1e-12) {
                lambda = maxAbs
                for (i in 0 until n) v[i] = next[i] / maxAbs
            }
        }
        return "Dominant |λ_max| ≈ ${MathEngine.formatReal(lambda)}  •  tr(A) = ${MathEngine.formatReal(tr)}  •  det(A) = ${MathEngine.formatReal(det)}"
    }

    fun computeVectors(u: List<Double>, v: List<Double>): VectorResult {
        val u3 = listOf(u.getOrElse(0) { 0.0 }, u.getOrElse(1) { 0.0 }, u.getOrElse(2) { 0.0 })
        val v3 = listOf(v.getOrElse(0) { 0.0 }, v.getOrElse(1) { 0.0 }, v.getOrElse(2) { 0.0 })
        val dot = u3[0] * v3[0] + u3[1] * v3[1] + u3[2] * v3[2]
        val cross = listOf(
            u3[1] * v3[2] - u3[2] * v3[1],
            u3[2] * v3[0] - u3[0] * v3[2],
            u3[0] * v3[1] - u3[1] * v3[0]
        )
        val normU = sqrt(u3.sumOf { it * it })
        val normV = sqrt(v3.sumOf { it * it })
        val cosTheta = if (normU > 0 && normV > 0) (dot / (normU * normV)).coerceIn(-1.0, 1.0) else 1.0
        val angleRad = acos(cosTheta)
        val unitU = if (normU > 0) u3.map { it / normU } else listOf(0.0, 0.0, 0.0)
        val projFactor = if (normV > 0) dot / (normV * normV) else 0.0
        val proj = v3.map { it * projFactor }
        return VectorResult(
            dotProduct = dot,
            crossProduct = cross,
            normU = normU,
            normV = normV,
            angleDegrees = Math.toDegrees(angleRad),
            angleRadians = angleRad,
            unitU = unitU,
            projectionUOnV = proj
        )
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): EquationSolution {
        if (abs(a) < 1e-12) {
            val root = if (abs(b) > 1e-12) -c / b else Double.NaN
            return EquationSolution(
                title = "Linear Degenerate ($b·x + $c = 0)",
                roots = listOf("x = ${MathEngine.formatReal(root)}"),
                discriminantInfo = "a = 0 (Linear equation)",
                steps = listOf("${b}x = ${-c}", "x = ${MathEngine.formatReal(root)}")
            )
        }
        val disc = b * b - 4.0 * a * c
        val vertexX = -b / (2.0 * a)
        val vertexY = a * vertexX * vertexX + b * vertexX + c
        return if (disc >= 0) {
            val x1 = (-b + sqrt(disc)) / (2.0 * a)
            val x2 = (-b - sqrt(disc)) / (2.0 * a)
            EquationSolution(
                title = "${MathEngine.formatReal(a)}x² + ${MathEngine.formatReal(b)}x + ${MathEngine.formatReal(c)} = 0",
                roots = listOf(
                    "x₁ = ${MathEngine.formatReal(x1)}  (${MathEngine.toRationalString(x1)})",
                    "x₂ = ${MathEngine.formatReal(x2)}  (${MathEngine.toRationalString(x2)})"
                ),
                discriminantInfo = "Δ = b² − 4ac = ${MathEngine.formatReal(disc)} ≥ 0 (Real Roots)",
                steps = listOf(
                    "Discriminant Δ = (${MathEngine.formatReal(b)})² − 4(${MathEngine.formatReal(a)})(${MathEngine.formatReal(c)}) = ${MathEngine.formatReal(disc)}",
                    "Parabola Vertex: (${MathEngine.formatReal(vertexX)}, ${MathEngine.formatReal(vertexY)})",
                    "Quadratic Formula: x = (−b ± √Δ) / 2a"
                )
            )
        } else {
            val re = -b / (2.0 * a)
            val im = sqrt(-disc) / (2.0 * abs(a))
            EquationSolution(
                title = "${MathEngine.formatReal(a)}x² + ${MathEngine.formatReal(b)}x + ${MathEngine.formatReal(c)} = 0",
                roots = listOf(
                    "x₁ = ${MathEngine.formatReal(re)} + ${MathEngine.formatReal(im)}i",
                    "x₂ = ${MathEngine.formatReal(re)} − ${MathEngine.formatReal(im)}i"
                ),
                discriminantInfo = "Δ = ${MathEngine.formatReal(disc)} < 0 (Complex Conjugate Roots)",
                steps = listOf(
                    "Discriminant Δ = ${MathEngine.formatReal(disc)}",
                    "Parabola Vertex: (${MathEngine.formatReal(vertexX)}, ${MathEngine.formatReal(vertexY)})",
                    "Real part = −b / 2a = ${MathEngine.formatReal(re)}, Imaginary part = √|Δ| / 2a = ${MathEngine.formatReal(im)}"
                )
            )
        }
    }

    /**
     * Exact Cubic Equation Solver (Cardano's / Trigonometric method) for ax³ + bx² + cx + d = 0.
     */
    fun solveCubic(a: Double, b: Double, c: Double, d: Double): EquationSolution {
        if (abs(a) < 1e-12) return solveQuadratic(b, c, d)
        val B = b / a
        val C = c / a
        val D = d / a
        val p = C - (B * B) / 3.0
        val q = (2.0 * B * B * B) / 27.0 - (B * C) / 3.0 + D
        val delta = (q * q) / 4.0 + (p * p * p) / 27.0
        val offset = -B / 3.0

        val roots = if (delta > 1e-12) {
            val u = cbrt(-q / 2.0 + sqrt(delta))
            val v = cbrt(-q / 2.0 - sqrt(delta))
            val x1 = u + v + offset
            val re = -(u + v) / 2.0 + offset
            val im = (sqrt(3.0) / 2.0) * abs(u - v)
            listOf(
                "x₁ = ${MathEngine.formatReal(x1)}",
                "x₂ = ${MathEngine.formatReal(re)} + ${MathEngine.formatReal(im)}i",
                "x₃ = ${MathEngine.formatReal(re)} − ${MathEngine.formatReal(im)}i"
            )
        } else if (abs(delta) <= 1e-12) {
            val u = cbrt(-q / 2.0)
            val x1 = 2.0 * u + offset
            val x2 = -u + offset
            listOf(
                "x₁ = ${MathEngine.formatReal(x1)}",
                "x₂ = x₃ = ${MathEngine.formatReal(x2)}"
            )
        } else {
            val r = sqrt(-(p * p * p) / 27.0)
            val phi = acos((-q / (2.0 * r)).coerceIn(-1.0, 1.0))
            val m = 2.0 * cbrt(r)
            val x1 = m * cos(phi / 3.0) + offset
            val x2 = m * cos((phi + 2.0 * PI) / 3.0) + offset
            val x3 = m * cos((phi + 4.0 * PI) / 3.0) + offset
            listOf(
                "x₁ = ${MathEngine.formatReal(x1)}",
                "x₂ = ${MathEngine.formatReal(x2)}",
                "x₃ = ${MathEngine.formatReal(x3)}"
            )
        }
        return EquationSolution(
            title = "Cubic Polynomial Solver",
            roots = roots,
            discriminantInfo = "Depressed Cubic Δ = ${MathEngine.formatReal(delta)} (p = ${MathEngine.formatReal(p)}, q = ${MathEngine.formatReal(q)})",
            steps = listOf(
                "Normalized depressed cubic: t³ + ${MathEngine.formatReal(p)}t + ${MathEngine.formatReal(q)} = 0",
                "Shift substitution: x = t − b/(3a) = t + ${MathEngine.formatReal(offset)}"
            )
        )
    }

    /**
     * 3×3 or 2×2 Linear System Solver (Ax = b) via Cramer's Rule.
     */
    fun solveLinearSystem(coeffs: List<List<Double>>, rhs: List<Double>): EquationSolution {
        val n = coeffs.size
        val detA = determinant(coeffs)
        if (abs(detA) < 1e-11) {
            return EquationSolution(
                title = "${n}×${n} Linear System (Ax = b)",
                roots = listOf("No Unique Solution (det(A) = 0)"),
                discriminantInfo = "System is dependent or inconsistent",
                steps = listOf("Determinant det(A) = 0")
            )
        }
        val varNames = listOf("x", "y", "z", "w")
        val roots = mutableListOf<String>()
        val steps = mutableListOf("det(A) = ${MathEngine.formatReal(detA)}")
        for (col in 0 until n) {
            val modified = List(n) { r ->
                List(n) { c -> if (c == col) rhs[r] else coeffs[r][c] }
            }
            val detCol = determinant(modified)
            val sol = detCol / detA
            roots.add("${varNames[col]} = ${MathEngine.formatReal(sol)}  (${MathEngine.toRationalString(sol)})")
            steps.add("det(A_${varNames[col]}) = ${MathEngine.formatReal(detCol)}  ⇒  ${varNames[col]} = ${MathEngine.formatReal(detCol)} / ${MathEngine.formatReal(detA)}")
        }
        return EquationSolution(
            title = "${n}×${n} Linear System Solved",
            roots = roots,
            discriminantInfo = "Unique Solution via Cramer's Rule (det(A) = ${MathEngine.formatReal(detA)})",
            steps = steps
        )
    }
}
