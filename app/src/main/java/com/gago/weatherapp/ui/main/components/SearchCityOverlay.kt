package com.gago.weatherapp.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gago.weatherapp.R
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.google.android.libraries.places.api.model.AutocompletePrediction


@Composable
fun SearchCityOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit = {},
    searchResults: List<AutocompletePrediction> = emptyList(),
    onSearchTextChanged: (String) -> Unit = {},
    searchText: String = "",
    onResultClick: (AutocompletePrediction) -> Unit = {},
    isLoading: Boolean = false,
    error: Int? = null,
    onClear: () -> Unit = {},
    showAddGpsButton: Boolean = false,
    onAddGpsCity: () -> Unit = {}
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
                    .fillMaxWidth()
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
                    label = { Text(stringResource(R.string.button_search_city)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.search_icon),
                            contentDescription = stringResource(R.string.button_search_city)
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
                                    imageVector = ImageVector.vectorResource(R.drawable.clear_icon),
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
                // GPS add-city button
                AnimatedVisibility(visible = showAddGpsButton) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onAddGpsCity,
                            enabled = !isLoading,
                            modifier = Modifier.testTag("gps_search_button")
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.location_icon),
                                contentDescription = stringResource(R.string.button_use_my_location)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.button_use_my_location))
                        }
                    }
                }
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error?.let { errorResId ->
                    Text(
                        text = stringResource(errorResId),
                        color = MaterialTheme.colorScheme.error
                    )
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

@Composable
fun SearchResultsList(
    results: List<AutocompletePrediction>,
    onResultClick: (AutocompletePrediction) -> Unit
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
                    headlineContent = { Text(result.getPrimaryText(null).toString()) },
                    supportingContent = { Text(result.getSecondaryText(null).toString()) },
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
    WeatherAppTheme {
        SearchCityOverlay(
            isVisible = true,
            searchResults = emptyList(),
            isLoading = false
        )
    }
} 