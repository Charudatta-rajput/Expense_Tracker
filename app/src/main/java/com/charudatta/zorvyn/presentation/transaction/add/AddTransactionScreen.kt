package com.charudatta.zorvyn.presentation.transaction.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    preFilledAmount: String = "",
    preFilledType: String = "expense",
    preFilledCategory: String = "",
    preFilledNote: String = "",
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    var amount by remember(preFilledAmount) { mutableStateOf(preFilledAmount) }
    var selectedCategory by remember(preFilledCategory) { mutableStateOf(preFilledCategory) }
    var customCategory by remember { mutableStateOf("") }
    var note by remember(preFilledNote) { mutableStateOf(preFilledNote) }
    var type by remember(preFilledType) { mutableStateOf(preFilledType) }
    var showCustomCategory by remember { mutableStateOf(false) }

    val predefinedCategories = if (type == "expense") {
        listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Education")
    } else {
        listOf("Salary", "Freelance", "Gift", "Investment")
    }

    LaunchedEffect(preFilledCategory) {
        if (preFilledCategory.isNotEmpty() && !predefinedCategories.contains(preFilledCategory)) {
            showCustomCategory = true
            customCategory = preFilledCategory
            selectedCategory = ""
        } else if (preFilledCategory.isNotEmpty()) {
            selectedCategory = preFilledCategory
        }
    }



    var amountError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }


    val expenseCategories = listOf(
        CategoryItem("Food", "🍔", FeatherIcons.Coffee, Color(0xFFFF6B6B)),
        CategoryItem("Transport", "🚗", FeatherIcons.Truck, Color(0xFF4ECDC4)),
        CategoryItem("Shopping", "🛍️", FeatherIcons.ShoppingBag, Color(0xFFFFB347)),
        CategoryItem("Entertainment", "🎬", FeatherIcons.Music, Color(0xFFA855F7)),
        CategoryItem("Bills", "📄", FeatherIcons.File, Color(0xFF60A5FA)),
        CategoryItem("Health", "💊", FeatherIcons.Heart, Color(0xFFF87171)),
        CategoryItem("Education", "📚", FeatherIcons.Book, Color(0xFF34D399)),
    )

    val incomeCategories = listOf(
        CategoryItem("Salary", "💰", FeatherIcons.DollarSign, Color(0xFF00C853)),
        CategoryItem("Freelance", "💻", FeatherIcons.DollarSign, Color(0xFF00C853)),
        CategoryItem("Gift", "🎁", FeatherIcons.Heart, Color(0xFF00C853)),
        CategoryItem("Investment", "📈", FeatherIcons.DollarSign, Color(0xFF00C853)),
    )

    val categories = if (type == "expense") expenseCategories else incomeCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Transaction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = { type = "expense" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "expense") Color(0xFFFF5252) else MaterialTheme.colorScheme.surface,
                            contentColor = if (type == "expense") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expense", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }


                    Button(
                        onClick = { type = "income" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "income") Color(0xFF00C853) else MaterialTheme.colorScheme.surface,
                            contentColor = if (type == "income") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Income", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }


            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.filter { char -> char.isDigit() || char == '.' }
                    amountError = false
                },
                label = { Text("Amount", fontSize = 14.sp) },
                leadingIcon = {
                    Text(
                        "₹",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252)
                    )
                },
                isError = amountError,
                supportingText = {
                    if (amountError) {
                        Text("Enter valid amount", fontSize = 11.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252),
                    focusedLabelColor = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252)
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )


                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { categoryItem ->
                        CategoryChip(
                            item = categoryItem,
                            isSelected = selectedCategory == categoryItem.name,
                            onClick = {
                                selectedCategory = categoryItem.name
                                customCategory = ""
                                showCustomCategory = false
                                categoryError = false
                            }
                        )
                    }
                    item {
                        CategoryChip(
                            item = CategoryItem("Custom", "✏️", FeatherIcons.Edit, MaterialTheme.colorScheme.primary),
                            isSelected = showCustomCategory,
                            onClick = {
                                showCustomCategory = true
                                selectedCategory = ""
                                categoryError = false
                            }
                        )
                    }
                }


                if (showCustomCategory) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = {
                            customCategory = it
                            categoryError = false
                        },
                        placeholder = { Text("Enter custom category") },
                        leadingIcon = {
                            Icon(FeatherIcons.Tag, null, modifier = Modifier.size(18.dp))
                        },
                        isError = categoryError,
                        supportingText = {
                            if (categoryError) {
                                Text("Please enter a category", fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }


            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(FeatherIcons.FileText, null, modifier = Modifier.size(18.dp))
                },
                placeholder = { Text("Add a note...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252),
                    focusedLabelColor = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252)
                )
            )

            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = {
                    var isValid = true
                    val finalCategory = if (showCustomCategory) customCategory else selectedCategory

                    if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull() == 0.0) {
                        amountError = true
                        isValid = false
                    }

                    if (finalCategory.isBlank()) {
                        categoryError = true
                        isValid = false
                    }

                    if (isValid) {
                        viewModel.addTransaction(
                            amount = amount.toDouble(),
                            type = type,
                            category = finalCategory,
                            note = note
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "income") Color(0xFF00C853) else Color(0xFFFF5252)
                )
            ) {
                Icon(
                    if (type == "income") Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (type == "income") "Add Income" else "Add Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CategoryChip(
    item: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) item.color else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isSelected) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.emoji, fontSize = 14.sp)
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class CategoryItem(
    val name: String,
    val emoji: String,
    val icon: ImageVector,
    val color: Color
)