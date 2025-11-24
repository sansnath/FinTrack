package com.example.fintrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.fintrack.data.Transaction
import com.example.fintrack.ui.theme.Expense
import com.example.fintrack.ui.theme.Income
import com.example.fintrack.utils.formatRupiah
import com.example.fintrack.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    viewModel: MainViewModel,
    transaction: Transaction,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }

    var amountRaw by remember {
        mutableStateOf(
            transaction.amount.toString().replace(".", "").replace(",", "")
        )
    }

    // TEXT dengan format Rupiah
    var amountText by remember {
        mutableStateOf(
            TextFieldValue(
                text = formatRupiah(transaction.amount),
                selection = TextRange(formatRupiah(transaction.amount).length)
            )
        )
    }

    val categoryList = listOf("Makan", "Transport", "Belanja", "Tagihan", "Gaji", "Kesehatan", "Hiburan", "Lainnya")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }

    var type by remember { mutableStateOf(transaction.type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // TITLE
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // AMOUNT (format Rupiah)
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    val digits = newValue.text.filter { it.isDigit() }
                    amountRaw = digits

                    val formatted = if (digits.isNotEmpty()) {
                        formatRupiah(digits.toDouble())
                    } else ""

                    amountText = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // CATEGORY DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoryList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                selectedCategory = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            // TYPE PICKER
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TypeSelectionCard(
                    label = "Income",
                    selected = type == "income",
                    onClick = { type = "income" },
                    color = Income,
                    modifier = Modifier.weight(1f)
                )

                TypeSelectionCard(
                    label = "Expense",
                    selected = type == "expense",
                    onClick = { type = "expense" },
                    color = Expense,
                    modifier = Modifier.weight(1f)
                )
            }

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // UPDATE BUTTON
            Button(
                onClick = {
                    val finalAmount = amountRaw.toDoubleOrNull() ?: 0.0

                    viewModel.updateTransaction(
                        transaction = transaction,
                        title = title,
                        amount = finalAmount.toString(),
                        type = type,
                        category = selectedCategory,
                        date = transaction.date,
                        onSuccess = onNavigateBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Update Transaction", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
