package com.gago.weatherapp.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.SettingViewModel
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MeasureUnit

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingViewModel = hiltViewModel()
) {

    val settingValue = settingsViewModel.settings.collectAsState(initial = Settings()).value
//    Log.d("SettingsOnSettingScreen", settingValue.toString())
    ScaffoldSetting(settings = settingValue, navController = navController) {
        settingsViewModel.saveChangeSettings(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldSetting(
    navController: NavController,
    settings: Settings,
    onSettingsChanged: (Settings) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            var isExpanded by remember { mutableStateOf(false) }

            var isDialogAboutOpen by remember { mutableStateOf(false) }

            val selectedTextFromResources =
                stringResource(id = settings.unitOfMeasurement.stringRes)

            var selectedText by remember { mutableStateOf("") }
            selectedText = selectedTextFromResources

            ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isChanged ->
                isExpanded = isChanged
            }) {
                TextField(
                    value = selectedText, onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                        .menuAnchor(),
                    readOnly = true,
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.unit_of_measurement)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }) {

                    MeasureUnit.entries.forEach { measureUnit ->
                        val menuText = stringResource(id = measureUnit.stringRes)
                        DropdownMenuItem(
                            text = { Text(text = menuText) },
                            onClick = {
                                selectedText = menuText
                                isExpanded = false
                                onSettingsChanged(
                                    settings.copy(
                                        unitOfMeasurement = measureUnit
                                    )
                                )
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(
                onClick = { isDialogAboutOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "About",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            if (isDialogAboutOpen) {
                AboutDialog {
                    isDialogAboutOpen = false
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDialogChange: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDialogChange() },
        confirmButton = {
            TextButton(onClick = { onDialogChange() }) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        },
        title = { Text(text = stringResource(id = R.string.dialog_title_about)) },
        text = { Text(text = stringResource(id = R.string.info_about_app)) })
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ScaffoldSetting(settings = Settings(), navController = rememberNavController()) {

            }
        }
    }
}