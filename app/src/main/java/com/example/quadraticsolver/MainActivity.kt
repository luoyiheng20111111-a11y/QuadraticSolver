package com.example.quadraticsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QuadraticSolverScreen()
            }
        }
    }
}

@Composable
fun QuadraticSolverScreen() {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<QuadraticResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "一元二次方程求解器",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ax² + bx + c = 0",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = a,
            onValueChange = { a = it },
            label = { Text("系数 a") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = b,
            onValueChange = { b = it },
            label = { Text("系数 b") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = c,
            onValueChange = { c = it },
            label = { Text("系数 c") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val aVal = a.toDoubleOrNull()
                val bVal = b.toDoubleOrNull()
                val cVal = c.toDoubleOrNull()

                if (aVal == null || bVal == null || cVal == null) {
                    result = QuadraticResult.Error("请输入有效的数字")
                } else if (aVal == 0.0) {
                    result = QuadraticResult.Error("a 不能为 0（不是一元二次方程）")
                } else {
                    result = solveQuadratic(aVal, bVal, cVal)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "求解", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        result?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (res) {
                        is QuadraticResult.TwoRealRoots -> {
                            Text(
                                text = "两个不相等的实根",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ResultRow("x₁ =", res.x1)
                            Spacer(modifier = Modifier.height(8.dp))
                            ResultRow("x₂ =", res.x2)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "判别式 Δ = ${res.discriminant}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is QuadraticResult.OneRealRoot -> {
                            Text(
                                text = "两个相等的实根",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ResultRow("x₁ = x₂ =", res.x)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "判别式 Δ = 0",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is QuadraticResult.ComplexRoots -> {
                            Text(
                                text = "一对共轭复根",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "x₁ = ${res.real} + ${res.imaginary}i",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "x₂ = ${res.real} - ${res.imaginary}i",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "判别式 Δ = ${res.discriminant}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is QuadraticResult.Error -> {
                            Text(
                                text = res.message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatNumber(value),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun formatNumber(num: Double): String {
    return if (num == num.toLong().toDouble()) {
        num.toLong().toString()
    } else {
        String.format("%.4f", num)
    }
}

fun solveQuadratic(a: Double, b: Double, c: Double): QuadraticResult {
    val discriminant = b * b - 4 * a * c

    return when {
        discriminant > 0 -> {
            val sqrtD = sqrt(discriminant)
            val x1 = (-b + sqrtD) / (2 * a)
            val x2 = (-b - sqrtD) / (2 * a)
            QuadraticResult.TwoRealRoots(x1, x2, discriminant)
        }
        discriminant == 0.0 -> {
            val x = -b / (2 * a)
            QuadraticResult.OneRealRoot(x)
        }
        else -> {
            val real = -b / (2 * a)
            val imaginary = sqrt(-discriminant) / (2 * a)
            QuadraticResult.ComplexRoots(real, imaginary, discriminant)
        }
    }
}

sealed class QuadraticResult {
    data class TwoRealRoots(val x1: Double, val x2: Double, val discriminant: Double) : QuadraticResult()
    data class OneRealRoot(val x: Double) : QuadraticResult()
    data class ComplexRoots(val real: Double, val imaginary: Double, val discriminant: Double) : QuadraticResult()
    data class Error(val message: String) : QuadraticResult()
}
