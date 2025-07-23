package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gago.weatherapp.R
import androidx.compose.ui.tooling.preview.Preview
import com.gago.weatherapp.ui.theme.WeatherAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCityOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit = {},
    searchResults: List<CityResult> = emptyList(),
    onSearchTextChanged: (String) -> Unit = {},
    searchText: String = "",
    onResultClick: (CityResult) -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    onClear: () -> Unit = {}
) {
    if (!isVisible) return
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .testTag("search_overlay")
        ) {
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .fillMaxWidth(0.95f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp)
            ) {
                var text by remember { mutableStateOf(TextFieldValue(searchText)) }
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onSearchTextChanged(it.text)
                    },
                    label = { Text("Buscar ciudad") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar ciudad"
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = text.text.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = {
                                text = TextFieldValue("")
                                onSearchTextChanged("")
                                onClear()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field")
                )
                Spacer(Modifier.height(8.dp))
                GoogleMapsAttribution(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp)
                )
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                AnimatedVisibility(visible = searchResults.isNotEmpty()) {
                    SearchResultsList(
                        results = searchResults,
                        onResultClick = onResultClick
                    )
                }
            }
        }
    }
}

data class CityResult(val name: String, val country: String)

@Composable
fun SearchResultsList(
    results: List<CityResult>,
    onResultClick: (CityResult) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .testTag("search_results_list")
    ) {
        LazyColumn {
            items(results.take(5)) { result ->
                ListItem(
                    headlineContent = { Text(result.name) },
                    supportingContent = { Text(result.country) },
                    modifier = Modifier
                        .clickable { onResultClick(result) }
                        .testTag("search_result_item")
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@Composable
fun GoogleMapsAttribution(modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .testTag("google_maps_attribution")
            .clickable {
                // TODO: Open Google Maps TOS
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Powered by", color = Color.White)
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.google_on_non_white),
                contentDescription = "Google logo",
                tint = Color.Unspecified,
                modifier = Modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchCityOverlayPreview() {
    val mockResults = listOf(
        CityResult("Buenos Aires", "Argentina"),
        CityResult("Madrid", "España"),
        CityResult("Paris", "Francia"),
        CityResult("New York", "USA"),
        CityResult("Tokyo", "Japón"),
        CityResult("London", "UK"),
        CityResult("London", "UK")
    )
    WeatherAppTheme {
        SearchCityOverlay(
            isVisible = true,
            searchResults = mockResults,
            isLoading = false
        )
    }
} 