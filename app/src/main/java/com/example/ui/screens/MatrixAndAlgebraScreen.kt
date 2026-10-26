package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EquationSolution
import com.example.domain.MathEngine
import com.example.domain.MatrixAndSolverEngine
import com.example.domain.MatrixResult
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalcKeyShapeStyle
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.components.MorphingCalcButton
import com.example.ui.theme.LocalExpressivePalette

@Composable
fun MatrixAndAlgebraScreen(
    viewModel: CalculatorViewModel,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalExpressivePalette.current
    var subTab by remember { mutableStateOf("Matrices") }
    var dim by remember { mutableIntStateOf(3) }

    // Matrix A & Matrix B state (4x4 backing arrays)
    val matrixA = remember {
        mutableStateListOf(
            mutableStateListOf("2", "1", "0", "0"),
            mutableStateListOf("1", "3", "1", "0"),
            mutableStateListOf("0", "1", "2", "0"),
            mutableStateListOf("0", "0", "0", "1")
        )
    }
    val matrixB = remember {
        mutableStateListOf(
            mutableStateListOf("1", "2", "0", "0"),
            mutableStateListOf("0", "1", "1", "0"),
            mutableStateListOf("2", "0", "1", "0"),
            mutableStateListOf("0", "0", "0", "1")
        )
    }
    var editingMatrix by remember { mutableStateOf("A") }

    fun readMatrix(src: List<List<String>>, n: Int): List<List<Double>> {
        return List(n) { r ->
            List(n) { c -> src[r][c].toDoubleOrNull() ?: 0.0 }
        }
    }

    var matrixResult by remember {
        mutableStateOf(
            MatrixAndSolverEngine.inverse(
                listOf(
                    listOf(2.0, 1.0, 0.0),
                    listOf(1.0, 3.0, 1.0),
                    listOf(0.0, 1.0, 2.0)
                )
            )
        )
    }

    // Vector state
    var uX by remember { mutableStateOf("3") }
    var uY by remember { mutableStateOf("4") }
    var uZ by remember { mutableStateOf("0") }
    var vX by remember { mutableStateOf("1") }
    var vY by remember { mutableStateOf("2") }
    var vZ by remember { mutableStateOf("2") }

    // Polynomial & Linear System state
    var polyDegree by remember { mutableIntStateOf(2) }
    var coeffA by remember { mutableStateOf("1") }
    var coeffB by remember { mutableStateOf("-5") }
    var coeffC by remember { mutableStateOf("6") }
    var coeffD by remember { mutableStateOf("0") }
    var rhs1 by remember { mutableStateOf("5") }
    var rhs2 by remember { mutableStateOf("8") }
    var rhs3 by remember { mutableStateOf("4") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-Tab Pill Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Matrices", "3D Vectors", "Eq Solvers").forEach { tab ->
                ExpressiveStatusChip(
                    label = tab,
                    active = subTab == tab,
                    onClick = { subTab = tab },
                    modifier = Modifier.weight(1f),
                    tag = "algebra_subtab_$tab"
                )
            }
        }

        when (subTab) {
            "Matrices" -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matrix Workspace (${dim}×${dim})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(2, 3, 4).forEach { d ->
                                    ExpressiveStatusChip(
                                        label = "${d}×${d}",
                                        active = dim == d,
                                        onClick = { dim = d }
                                    )
                                }
                                ExpressiveStatusChip(
                                    label = "Edit $editingMatrix",
                                    active = true,
                                    onClick = { editingMatrix = if (editingMatrix == "A") "B" else "A" }
                                )
                            }
                        }

                        val activeGrid = if (editingMatrix == "A") matrixA else matrixB
                        for (r in 0 until dim) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (c in 0 until dim) {
                                    OutlinedTextField(
                                        value = activeGrid[r][c],
                                        onValueChange = { activeGrid[r][c] = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("matrix_cell_${r}_$c"),
                                        singleLine = true,
                                        label = { Text("${editingMatrix}${r + 1}${c + 1}") }
                                    )
                                }
                            }
                        }

                        // Matrix Operations Grid (10 distinct operations)
                        val opsRow1 = listOf("A + B", "A − B", "A × B", "det(A)", "A⁻¹")
                        val opsRow2 = listOf("Aᵀ", "RREF", "Eigen λ", "A²", "A³")

                        listOf(opsRow1, opsRow2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { op ->
                                    MorphingCalcButton(
                                        label = op,
                                        onClick = {
                                            val a = readMatrix(matrixA, dim)
                                            val b = readMatrix(matrixB, dim)
                                            matrixResult = when (op) {
                                                "A + B" -> MatrixAndSolverEngine.add(a, b)
                                                "A − B" -> MatrixAndSolverEngine.subtract(a, b)
                                                "A × B" -> MatrixAndSolverEngine.multiply(a, b)
                                                "det(A)" -> {
                                                    val dVal = MatrixAndSolverEngine.determinant(a)
                                                    val trVal = MatrixAndSolverEngine.trace(a)
                                                    MatrixResult(
                                                        title = "Determinant & Trace of A",
                                                        scalarText = "det(A) = ${MathEngine.formatReal(dVal)}  •  tr(A) = ${MathEngine.formatReal(trVal)}"
                                                    )
                                                }
                                                "A⁻¹" -> MatrixAndSolverEngine.inverse(a)
                                                "Aᵀ" -> MatrixAndSolverEngine.transpose(a)
                                                "RREF" -> {
                                                    val (rrefMat, rank) = MatrixAndSolverEngine.rrefAndRank(a)
                                                    MatrixResult(
                                                        title = "Row Reduced Echelon Form (RREF)",
                                                        matrix = rrefMat,
                                                        scalarText = "rank(A) = $rank  •  nullity = ${dim - rank}"
                                                    )
                                                }
                                                "Eigen λ" -> MatrixResult(
                                                    title = "Eigenvalues & Invariants",
                                                    scalarText = MatrixAndSolverEngine.eigenvaluesSummary(a)
                                                )
                                                "A²" -> MatrixAndSolverEngine.power(a, 2)
                                                else -> MatrixAndSolverEngine.power(a, 3)
                                            }
                                            viewModel.recordExternalCalculation(
                                                expression = "Matrix $op (${dim}×${dim})",
                                                result = matrixResult.scalarText ?: matrixResult.title,
                                                suiteCategory = "Matrix"
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        containerColor = palette.fnKeyContainer,
                                        contentColor = palette.fnKeyContent,
                                        shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                                        fontSize = 13.sp,
                                        hapticsEnabled = hapticsEnabled
                                    )
                                }
                            }
                        }
                    }
                }

                // Result Card
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("matrix_result_card"),
                    shape = RoundedCornerShape(24.dp),
                    color = palette.cardAccentSoft
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = matrixResult.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        matrixResult.scalarText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        matrixResult.matrix?.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { v ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                        modifier = Modifier.weight(1f).padding(3.dp)
                                    ) {
                                        Text(
                                            text = MathEngine.formatReal(v),
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "3D Vectors" -> {
                val uVec = listOf(uX.toDoubleOrNull() ?: 0.0, uY.toDoubleOrNull() ?: 0.0, uZ.toDoubleOrNull() ?: 0.0)
                val vVec = listOf(vX.toDoubleOrNull() ?: 0.0, vY.toDoubleOrNull() ?: 0.0, vZ.toDoubleOrNull() ?: 0.0)
                val vecRes = MatrixAndSolverEngine.computeVectors(uVec, vVec)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Vector u = (uₓ, uᵧ, u_z)", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(uX, { uX = it }, Modifier.weight(1f), singleLine = true, label = { Text("uₓ") })
                            OutlinedTextField(uY, { uY = it }, Modifier.weight(1f), singleLine = true, label = { Text("uᵧ") })
                            OutlinedTextField(uZ, { uZ = it }, Modifier.weight(1f), singleLine = true, label = { Text("u_z") })
                        }
                        Text("Vector v = (vₓ, vᵧ, v_z)", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(vX, { vX = it }, Modifier.weight(1f), singleLine = true, label = { Text("vₓ") })
                            OutlinedTextField(vY, { vY = it }, Modifier.weight(1f), singleLine = true, label = { Text("vᵧ") })
                            OutlinedTextField(vZ, { vZ = it }, Modifier.weight(1f), singleLine = true, label = { Text("v_z") })
                        }
                        ExpressiveWavyDivider()
                        Text("Dot Product u · v = ${MathEngine.formatReal(vecRes.dotProduct)}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Cross Product u × v = (${vecRes.crossProduct.joinToString(", ") { MathEngine.formatReal(it) }})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Magnitudes: ‖u‖ = ${MathEngine.formatReal(vecRes.normU)}  •  ‖v‖ = ${MathEngine.formatReal(vecRes.normV)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Angle θ = ${MathEngine.formatReal(vecRes.angleDegrees)}° (${MathEngine.formatReal(vecRes.angleRadians)} rad)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Unit Vector û = (${vecRes.unitU.joinToString(", ") { MathEngine.formatReal(it) }})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Projection proj_v(u) = (${vecRes.projectionUOnV.joinToString(", ") { MathEngine.formatReal(it) }})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            else -> {
                // Equation Solvers: Quadratic, Cubic, and Linear System Ax = b
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ExpressiveStatusChip("Quadratic (Deg 2)", polyDegree == 2, { polyDegree = 2 })
                            ExpressiveStatusChip("Cubic (Deg 3)", polyDegree == 3, { polyDegree = 3 })
                            ExpressiveStatusChip("3×3 System Ax=b", polyDegree == 99, { polyDegree = 99 })
                        }

                        val solution: EquationSolution = when (polyDegree) {
                            2 -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(coeffA, { coeffA = it }, Modifier.weight(1f), singleLine = true, label = { Text("a (x²)") })
                                    OutlinedTextField(coeffB, { coeffB = it }, Modifier.weight(1f), singleLine = true, label = { Text("b (x)") })
                                    OutlinedTextField(coeffC, { coeffC = it }, Modifier.weight(1f), singleLine = true, label = { Text("c") })
                                }
                                MatrixAndSolverEngine.solveQuadratic(
                                    coeffA.toDoubleOrNull() ?: 1.0,
                                    coeffB.toDoubleOrNull() ?: 0.0,
                                    coeffC.toDoubleOrNull() ?: 0.0
                                )
                            }
                            3 -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(coeffA, { coeffA = it }, Modifier.weight(1f), singleLine = true, label = { Text("a (x³)") })
                                    OutlinedTextField(coeffB, { coeffB = it }, Modifier.weight(1f), singleLine = true, label = { Text("b (x²)") })
                                    OutlinedTextField(coeffC, { coeffC = it }, Modifier.weight(1f), singleLine = true, label = { Text("c (x)") })
                                    OutlinedTextField(coeffD, { coeffD = it }, Modifier.weight(1f), singleLine = true, label = { Text("d") })
                                }
                                MatrixAndSolverEngine.solveCubic(
                                    coeffA.toDoubleOrNull() ?: 1.0,
                                    coeffB.toDoubleOrNull() ?: 0.0,
                                    coeffC.toDoubleOrNull() ?: 0.0,
                                    coeffD.toDoubleOrNull() ?: 0.0
                                )
                            }
                            else -> {
                                Text(
                                    text = "Uses Matrix A (3×3) as coefficient matrix and b = [b₁, b₂, b₃]ᵀ:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(rhs1, { rhs1 = it }, Modifier.weight(1f), singleLine = true, label = { Text("b₁") })
                                    OutlinedTextField(rhs2, { rhs2 = it }, Modifier.weight(1f), singleLine = true, label = { Text("b₂") })
                                    OutlinedTextField(rhs3, { rhs3 = it }, Modifier.weight(1f), singleLine = true, label = { Text("b₃") })
                                }
                                MatrixAndSolverEngine.solveLinearSystem(
                                    readMatrix(matrixA, 3),
                                    listOf(rhs1.toDoubleOrNull() ?: 0.0, rhs2.toDoubleOrNull() ?: 0.0, rhs3.toDoubleOrNull() ?: 0.0)
                                )
                            }
                        }

                        ExpressiveWavyDivider()
                        Text(solution.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(solution.discriminantInfo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        solution.roots.forEach { root ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = palette.resultPillContainer.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = root,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.resultPillContent,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        solution.steps.forEach { step ->
                            Text("• $step", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
