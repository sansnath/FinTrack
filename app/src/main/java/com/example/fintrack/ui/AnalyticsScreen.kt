package com.example.fintrack.ui

import android.graphics.Color as AColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fintrack.data.Transaction
import com.example.fintrack.utils.formatRupiah
import com.example.fintrack.viewmodel.MainViewModel
import com.example.fintrack.utils.generateAiRecommendation
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

fun splitAiOutput(aiText: String): Pair<String, String> {
    val lines = aiText.trim().lines()

    val bullets = lines
        .takeWhile { it.trim().startsWith("-") }
        .joinToString("\n")

    val paragraph = lines
        .dropWhile { it.trim().startsWith("-") }
        .joinToString("\n")
        .trim()

    return bullets to paragraph
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: MainViewModel, onBack: () -> Unit) {

    val income = viewModel.totalIncome
    val expense = viewModel.totalExpense
    val transactions = viewModel.transactions
    val context = LocalContext.current

    var aiText by remember { mutableStateOf("Menghasilkan rekomendasi...") }

    LaunchedEffect(Unit) {
        try {
            aiText = generateAiRecommendation(
                context,
                """
                Buat analisis keuangan singkat berdasarkan data berikut:
                income = $income
                expense = $expense
                transaksi = $transactions

                Formatkan seperti ini (tanpa bold dan tanpa markdown):
                - Pengeluaran terbesar: ...
                - Total tabungan: ...
                - Rasio: ...
                - Status finansial: ...
                Berikan 1 paragraf rekomendasi singkat.
                """.trimIndent()
            )
        } catch (e: Exception) {
            aiText = "Gagal memuat AI: ${e.message}"
        }
    }

    val (bulletText, paragraphText) = splitAiOutput(aiText)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Insights", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Text("Income vs Expense", style = MaterialTheme.typography.titleMedium, color = Color.White)

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = {
                    PieChart(context).apply {

                        val entries = listOf(
                            PieEntry(income.toFloat(), "Income"),
                            PieEntry(expense.toFloat(), "Expense")
                        )

                        val colors = listOf(
                            AColor.parseColor("#4CAF50"),
                            AColor.parseColor("#F44336")
                        )

                        val dataSet = PieDataSet(entries, "").apply {
                            setColors(colors)
                            valueTextColor = AColor.TRANSPARENT
                            valueTextSize = 0f
                        }

                        data = PieData(dataSet)
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawEntryLabels(false)
                        centerText = ""
                        animateY(900)
                        invalidate()
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(14.dp).background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoSmallText("Income: ${formatRupiah(income)}", Color.White)
                }

                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(14.dp).background(Color(0xFFF44336))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoSmallText("Expense: ${formatRupiah(expense)}", Color.White)
                }
            }

            Divider(color = Color.Gray)

            Text("Financial Insights", style = MaterialTheme.typography.titleMedium, color = Color.White)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 300.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3A3A45))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    // Bullet — left aligned
                    Text(
                        text = bulletText,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Paragraph — justified
                    Text(
                        text = paragraphText,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}

@Composable
fun AutoSmallText(text: String, color: Color) {
    var fontSize by remember { mutableStateOf(14.sp) }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { if (it.didOverflowWidth) fontSize *= 0.85f }
    )
}
