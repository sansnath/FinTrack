package com.example.fintrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.fintrack.data.Transaction
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

    // RAW amount ONLY digits (from database)
    var amountRaw by remember {
        mutableStateOf(
            transaction.amount.toString().replace(".", "").replace(",", "")
        )
    }

    // TEXTFIELD controller with formatted initial value
    var amountText by remember {
        mutableStateOf(
            TextFieldValue(
                text = formatRupiah(transaction.amount),
                selection = TextRange(formatRupiah(transaction.amount).length)
            )
        )
    }

    // CATEGORY dropdown
    val categoryList = listOf("Makan", "Transport", "Belanja", "Tagihan", "Gaji", "Kesehatan", "Hiburan", "Lainnya")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }

    var type by remember { mutableStateOf(transaction.type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // TITLE
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // AMOUNT WITH AUTO RUPIAH FORMAT
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->

                    // keep digits only
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
                modifier = Modifier.fillMaxWidth()
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

            // TYPE RADIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = type == "income",
                                    onClick = { type = "income" }
                                )
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = type == "income",
                                onClick = { type = "income" }
                            )
                            Text("Income")
                        }

                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = type == "expense",
                                    onClick = { type = "expense" }
                                )
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = type == "expense",
                                onClick = { type = "expense" }
                            )
                            Text("Expense")
                        }
                    }
                }
            }

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }
            }
        }
    }
}
