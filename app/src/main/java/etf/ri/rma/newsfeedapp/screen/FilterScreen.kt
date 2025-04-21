package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.ArrayList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(navController: NavHostController) {
    // Kategorije
    var selectedCategory by remember { mutableStateOf("Sve") }

    // DateRangePicker
    var showDatePicker by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf<Pair<String, String>?>(null) }
    val datePickerState = rememberDateRangePickerState()
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    // Nepoželjne riječi
    var unwantedInput by remember { mutableStateOf("") }
    val unwantedWords = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- CHIPOVI ZA KATEGORIJE ---
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            val categories = listOf("Sve", "Politika", "Sport", "Nauka/tehnologija", "Zabava")
            categories.forEach { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    modifier = Modifier.testTag(
                        "filter_chip_" + when (category) {
                            "Sve"                -> "all"
                            "Politika"           -> "pol"
                            "Sport"              -> "spo"
                            "Nauka/tehnologija"  -> "sci"
                            "Zabava"             -> "none"
                            else                 -> category.lowercase().replace(" ", "_")
                        }
                    )
                )
            }
        }

        // --- DATE RANGE PICKER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateRange?.let { "${it.first};${it.second}" } ?: "Nije odabrano",
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_daterange_display")
            )
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("filter_daterange_button")
            ) {
                Text("Odaberi datume")
            }
        }
        if (showDatePicker) {
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedStartDateMillis
                            ?.let { s ->
                                datePickerState.selectedEndDateMillis?.let { e ->
                                    val start = Instant.ofEpochMilli(s)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    val end = Instant.ofEpochMilli(e)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    dateRange = formatter.format(start) to formatter.format(end)
                                }
                            }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                },
                text = { DateRangePicker(state = datePickerState) }
            )
        }

        // --- NEPOŽELJNE RIJEČI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            TextField(
                value = unwantedInput,
                onValueChange = { unwantedInput = it },
                label = { Text("Nepoželjna riječ") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_unwanted_input")
            )
            Button(
                onClick = {
                    val w = unwantedInput.trim()
                    if (w.isNotEmpty() && unwantedWords.none { it.equals(w, ignoreCase = true) }) {
                        unwantedWords.add(w)
                    }
                    unwantedInput = ""
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("filter_unwanted_add_button")
            ) {
                Text("Dodaj")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("filter_unwanted_list")
        ) {
            unwantedWords.forEach { word ->
                Text(text = word)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DUGME “PRIMIJENI FILTERE” ---
        Button(
            onClick = {
                // Spremao odabrane vrijednosti u SavedStateHandle prethodnog ekrana
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("filterCategory", selectedCategory)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("filterStartDate", dateRange?.first)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("filterEndDate", dateRange?.second)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("filterUnwantedWords", ArrayList(unwantedWords))
                // Vraća se natrag
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("filter_apply_button")
        ) {
            Text("Primijeni filtere")
        }
    }
}
