package com.example.quadraticsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.math.pow

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

        Spacer(modifier = Modifier.height(24.dp))

        // 实时显示方程式
        EquationDisplay(a, b, c)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = a,
            onValueChange = { 
                a = it
                result = null
            },
            label = { Text("系数 a") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = b,
            onValueChange = { 
                b = it
                result = null
            },
            label = { Text("系数 b") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = c,
            onValueChange = { 
                c = it
                result = null
            },
            label = { Text("系数 c") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "求解", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
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
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 显示根号形式的解
                            RootDisplay(res, a.toDoubleOrNull() ?: 1.0, b.toDoubleOrNull() ?: 0.0, c.toDoubleOrNull() ?: 0.0)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "判别式 Δ = ${formatNumber(res.discriminant)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is QuadraticResult.OneRealRoot -> {
                            Text(
                                text = "两个相等的实根",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ComplexRootRow("x₁ =", res.real, res.imaginary, true)
                            Spacer(modifier = Modifier.height(8.dp))
                            ComplexRootRow("x₂ =", res.real, res.imaginary, false)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "判别式 Δ = ${formatNumber(res.discriminant)}",
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
fun EquationDisplay(a: String, b: String, c: String) {
    val aVal = a.toDoubleOrNull()
    val bVal = b.toDoubleOrNull()
    val cVal = c.toDoubleOrNull()

    // 构建方程式文本，使用 Unicode 下标字符
    val equationText = buildAnnotatedString {
        // 第一个项 ax²
        val aStr = when {
            a.isEmpty() -> "a"
            aVal == null -> a
            aVal == 1.0 -> "1"
            aVal == -1.0 -> "-1"
            else -> formatNumber(aVal)
        }
        append(aStr)
        append("x²")

        // 第二个项 bx
        val bPrefix = when {
            b.isEmpty() -> ""
            bVal == null -> ""
            bVal >= 0 -> " + "
            else -> " - "
        }
        val bStr = when {
            b.isEmpty() -> "b"
            bVal == null -> kotlin.math.abs(b.toDoubleOrNull() ?: 0.0).toString()
            bVal == 1.0 || bVal == -1.0 -> "1"
            else -> formatNumber(kotlin.math.abs(bVal))
        }
        append(bPrefix)
        append(bStr)
        append("x")

        // 第三个项 c
        val cPrefix = when {
            c.isEmpty() -> ""
            cVal == null -> ""
            cVal >= 0 -> " + "
            else -> " - "
        }
        val cStr = when {
            c.isEmpty() -> "c"
            cVal == null -> kotlin.math.abs(c.toDoubleOrNull() ?: 0.0).toString()
            else -> formatNumber(kotlin.math.abs(cVal))
        }
        append(cPrefix)
        append(cStr)

        append(" = 0")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = equationText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RootDisplay(result: QuadraticResult.TwoRealRoots, a: Double, b: Double, c: Double) {
    val discriminant = result.discriminant
    val sqrtD = sqrt(discriminant)
    
    // 检查是否开得尽
    val isPerfectSquare = abs(sqrtD - sqrtD.toLong().toDouble()) < 0.0001
    
    if (isPerfectSquare) {
        // 开得尽，直接显示数值
        ResultRow("x₁ =", result.x1)
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow("x₂ =", result.x2)
    } else {
        // 开不尽，显示根号形式
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 显示根号形式的解
            val simplified = simplifySqrt(discriminant)
            
            Text(
                text = "x₁ = (${formatSignedNumber(-b)} + √${simplified.first}) / ${formatNumber(2 * a)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "x₂ = (${formatSignedNumber(-b)} - √${simplified.first}) / ${formatNumber(2 * a)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 同时显示近似值
            Text(
                text = "≈ ${formatNumber(result.x1)}  ,  ≈ ${formatNumber(result.x2)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ComplexRootRow(label: String, real: Double, imaginary: Double, isPlus: Boolean) {
    val sign = if (isPlus) "+" else "-"
    val imagStr = formatNumber(imaginary)
    
    Text(
        text = "$label ${formatNumber(real)} $sign ${imagStr}i",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
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

fun formatSignedNumber(num: Double): String {
    return if (num >= 0) "+ ${formatNumber(num)}" else "- ${formatNumber(kotlin.math.abs(num))}"
}

fun formatNumber(num: Double): String {
    return if (abs(num) < 0.0001 && num != 0.0) {
        String.format("%.6f", num).trimEnd('0').trimEnd('.')
    } else if (num == num.toLong().toDouble()) {
        num.toLong().toString()
    } else {
        String.format("%.4f", num).trimEnd('0').trimEnd('.')
    }
}

// 简化根号，如 √12 = 2√3
fun simplifySqrt(n: Double): Pair<String, Int> {
    if (n <= 0) return Pair(n.toString(), 1)
    
    val intN = n.toLong()
    var m = intN
    var coeff: Long = 1
    var divisor: Long = 1
    
    // 提取平方因子
    val primes = listOf(4L, 9L, 16L, 25L, 36L, 49L, 64L, 81L, 100L, 121L, 144L, 169L, 196L, 225L)
    
    for (prime in primes) {
        while (m % prime == 0L) {
            m /= prime
            coeff *= (prime / 4)
        }
    }
    
    // 检查是否能被其他平方数整除
    var d: Long = 2
    while (d * d <= m) {
        val square = d * d
        while (m % square == 0L) {
            m /= square
            coeff *= d
        }
        d++
    }
    
    val remaining = m
    return if (remaining == 1L) {
        Pair(formatNumber(coeff.toDouble()), 1)
    } else if (coeff == 1L) {
        Pair("${formatNumber(n.toDouble())}", 1)
    } else {
        Pair("${formatNumber(coeff.toDouble())}√${formatNumber(remaining.toDouble())}", 1)
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
