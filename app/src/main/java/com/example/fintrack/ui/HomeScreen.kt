package com.example.fintrack.ui

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fintrack.data.Transaction
import com.example.fintrack.utils.formatRupiah
import com.example.fintrack.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Transaction) -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    // ====== STATE FILTER ======
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterFrom by remember { mutableStateOf<String?>(null) }
    var filterTo by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf("All") }
    var filterCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "Makan", "Transport", "Belanja", "Tagihan",
        "Gaji", "Kesehatan", "Hiburan", "Lainnya"
    )

    // ====== LOGIC FILTER ======
    val filteredTransactions = viewModel.transactions.filter { t ->
        val date = t.date

        val datePass =
            (filterFrom.isNullOrBlank() || date >= filterFrom!!) &&
                    (filterTo.isNullOrBlank() || date <= filterTo!!)

        val typePass = filterType == "All" || t.type == filterType
        val catPass = filterCategory == "All" || t.category == filterCategory

        datePass && typePass && catPass
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${viewModel.currentUser?.name ?: "User"}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ====== SUMMARY ======
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        title = "Income",
                        amount = viewModel.totalIncome,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expense",
                        amount = viewModel.totalExpense,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SummaryCard(
                    title = "Balance",
                    amount = viewModel.balance,
                    color = if (viewModel.balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ====== TITLE + FILTER BUTTON ======
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = { showFilterDialog = true }) {
                        Text("Filter")
                    }
                }
            }

            // ====== LIST TRANSAKSI ======
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
                                text = "No transactions yet.\nTap + to add your first transaction!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { onNavigateToEditTransaction(transaction) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    // ====== DIALOG FILTER ======
    if (showFilterDialog) {
        FilterDialog(
            from = filterFrom,
            to = filterTo,
            type = filterType,
            category = filterCategory,
            categories = categories,
            onApply = { from, to, type, cat ->
                filterFrom = from
                filterTo = to
                filterType = type
                filterCategory = cat
                showFilterDialog = false
            },
            onCancel = { showFilterDialog = false }
        )
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

                // ====== DATE FROM ======
                Text("Date From (yyyy-MM-dd)")
                OutlinedButton(onClick = {
                    showDatePicker(
                        context = context,
                        onDateSelected = { tempFrom = it }
                    )
                }) {
                    Text(text = tempFrom ?: "Select Date")
                }

                // ====== DATE TO ======
                Text("Date To (yyyy-MM-dd)")
                OutlinedButton(onClick = {
                    showDatePicker(
                        context = context,
                        onDateSelected = { tempTo = it }
                    )
                }) {
                    Text(text = tempTo ?: "Select Date")
                }

                // ====== TYPE (All / Income / Expenses) ======
                Text("Jenis")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem("All", tempType == "All") { tempType = "All" }
                    FilterChipItem("Income", tempType == "income") { tempType = "income" }
                    FilterChipItem("Expenses", tempType == "expense") { tempType = "expense" }
                }

                // ====== CATEGORY DROPDOWN ======
                Text("Kategori")
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
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
            val realMonth = m + 1
            val dateStr = String.format("%04d-%02d-%02d", y, realMonth, d)
            onDateSelected(dateStr)
        },
        year,
        month,
        day
    ).show()
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

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start     // *** RATA KIRI ***
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Text(
                text = formatRupiah(amount),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ==========================
            // Kiri: Title + Category + Date
            // ==========================
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

            // ==========================
            // Kanan: Amount + Delete (mepet kanan)
            // ==========================
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Nominal
                Text(
                    text = (if (isIncome) "+ " else "- ") + formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)   // jarak sebelum icon bin
                )

                // Tombol Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)             // lebih kecil & rapi
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

