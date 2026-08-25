package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.FinancialViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: FinancialViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val smartParseResult by viewModel.smartParseResult.collectAsStateWithLifecycle()
    val isParsing by viewModel.isParsingExpense.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Manual, 1 = AI Smart Parse

    // Form fields
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD_DINING) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }
    var note by remember { mutableStateOf("") }

    // AI Smart input
    var naturalLanguageInput by remember { mutableStateOf("") }

    LaunchedEffect(smartParseResult) {
        smartParseResult?.let { result ->
            title = result.title
            amountText = if (result.amount > 0) "%.2f".format(result.amount) else ""
            selectedCategory = result.category
            selectedPaymentMethod = result.paymentMethod
            if (result.note.isNotEmpty()) note = result.note
            selectedTabIndex = 0 // Switch to manual review
            viewModel.clearSmartParseResult()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Record Financial Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manual Entry")
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Smart Parse")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 1) {
                // AI Smart Parse Mode
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paste a receipt text, SMS alert, or natural description:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = naturalLanguageInput,
                        onValueChange = { naturalLanguageInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_natural_language_input"),
                        placeholder = { Text("e.g. $42.50 at Sweetgreen for lunch with Apple Pay") },
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset suggestions for instant testing
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "$18.40 at Chipotle Mexican Grill",
                            "$65.00 Shell Gas Station",
                            "$12.99 Netflix Subscription",
                            "$110.00 Whole Foods groceries"
                        )
                        presets.forEach { preset ->
                            Surface(
                                onClick = { naturalLanguageInput = preset },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.smartParseExpense(naturalLanguageInput) },
                        enabled = naturalLanguageInput.isNotBlank() && !isParsing,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parse_with_gemini_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        if (isParsing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini is parsing...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parse & Populate Fields with Gemini", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Manual Entry Mode
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Type Selector (Expense vs Income)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = transactionType == TransactionType.EXPENSE,
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text("Expense (-)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.error
                            )
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.INCOME,
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text("Income (+)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.18f),
                                selectedLabelColor = Color(0xFF10B981)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_input"),
                        label = { Text("Amount ($)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_title_input"),
                        label = { Text("Merchant / Description") },
                        placeholder = { Text("e.g. Trader Joe's, Coffee, Salary") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TransactionCategory.values().forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(cat.displayName, fontSize = 11.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                    selectedLabelColor = cat.color
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Method Selector
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Note Field
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_note_input"),
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Add any details or tags") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && title.isNotBlank()) {
                                viewModel.addTransaction(
                                    title = title.trim(),
                                    amount = amount,
                                    type = transactionType,
                                    category = selectedCategory,
                                    paymentMethod = selectedPaymentMethod,
                                    note = note.trim()
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_transaction_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Log Habit (+50 XP)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
