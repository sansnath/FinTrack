package com.example.fintrack.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.data.Transaction
import com.example.fintrack.utils.formatRupiah
import com.example.fintrack.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.Assessment
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Transaction) -> Unit,
    onLogout: () -> Unit
) {

    val currentUser = viewModel.currentUser
    val transactions = viewModel.transactions

    val income = viewModel.totalIncome
    val expense = viewModel.totalExpense
    val balance = viewModel.balance

    // FILTER STATES
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterFrom by remember { mutableStateOf<String?>(null) }
    var filterTo by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf("All") }
    var filterCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "Makan", "Transport", "Belanja", "Tagihan",
        "Gaji", "Kesehatan", "Hiburan", "Lainnya"
    )

    // FILTER LOGIC
    val filteredTransactions = transactions.filter { t ->
        val datePass =
            (filterFrom.isNullOrBlank() || t.date >= filterFrom!!) &&
                    (filterTo.isNullOrBlank() || t.date <= filterTo!!)

        val typePass = filterType == "All" || t.type == filterType
        val catPass = filterCategory == "All" || t.category == filterCategory

        datePass && typePass && catPass
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${currentUser?.name ?: "User"}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {

                    // 🔥 Tombol Analytics (grafik)
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(
                            Icons.Default.Assessment,   // pakai Assessment biar aman
                            contentDescription = "Analytics"
                        )
                    }

                    // 🔒 Tombol Logout
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SUMMARY CARDS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        title = "Income",
                        amount = income,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expense",
                        amount = expense,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SummaryCard(
                    title = "Balance",
                    amount = balance,
                    color = if (balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // HEADER + FILTER BUTTON
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    TextButton(onClick = { showFilterDialog = true }) {
                        Text("Filter")
                    }
                }
            }

            // TRANSACTION LIST
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found.\nTry adjusting your filter.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    filteredTransactions,
                    key = { it.firestoreId ?: it.hashCode().toString() }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { onNavigateToEditTransaction(transaction) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            from = filterFrom,
            to = filterTo,
            type = filterType,
            category = filterCategory,
            categories = categories,
            onApply = { from, to, type, category ->
                filterFrom = from
                filterTo = to
                filterType = type
                filterCategory = category
                showFilterDialog = false
            },
            onCancel = { showFilterDialog = false }
        )
    }
}
@Composable
fun AutoResizedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    maxFontSize: TextUnit = MaterialTheme.typography.headlineSmall.fontSize,
    minFontSize: TextUnit = 12.sp
) {
    var textStyle by remember(text) { // ← key important!
        mutableStateOf(TextStyle(fontSize = maxFontSize, fontWeight = fontWeight, color = color))
    }
    var ready by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (ready) drawContent()
        },
        onTextLayout = { result ->
            if (!ready) {
                if (result.didOverflowWidth && textStyle.fontSize > minFontSize) {
                    textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9f)
                } else {
                    ready = true
                }
            }
        }
    )
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AutoResizedText(
                text = formatRupiah(amount),
                color = color,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    from: String?,
    to: String?,
    type: String,
    category: String,
    categories: List<String>,
    onApply: (String?, String?, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var tempFrom by remember { mutableStateOf(from) }
    var tempTo by remember { mutableStateOf(to) }
    var tempType by remember { mutableStateOf(type) }
    var tempCategory by remember { mutableStateOf(category) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Filter Transactions") },
        confirmButton = {
            TextButton(onClick = {
                onApply(tempFrom, tempTo, tempType, tempCategory)
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Text("Date From (yyyy-MM-dd)")
                OutlinedButton(onClick = {
                    showDatePicker(context) { tempFrom = it }
                }) {
                    Text(tempFrom ?: "Select Date")
                }

                Text("Date To (yyyy-MM-dd)")
                OutlinedButton(onClick = {
                    showDatePicker(context) { tempTo = it }
                }) {
                    Text(tempTo ?: "Select Date")
                }

                Text("Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem("All", tempType == "All") { tempType = "All" }
                    FilterChipItem("income", tempType == "income") { tempType = "income" }
                    FilterChipItem("expense", tempType == "expense") { tempType = "expense" }
                }

                Text("Category")
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = tempCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    tempCategory = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "income"
    val color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(40.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // === TEXT SECTION ===
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${transaction.category} • ${transaction.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // === AMOUNT + DELETE ===
            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = (if (isIncome) "+ " else "- ") + formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 10.dp)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

private fun showDatePicker(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _, y, m, d ->
            val date = String.format("%04d-%02d-%02d", y, m + 1, d)
            onDateSelected(date)
        },
        year,
        month,
        day
    ).show()
}
