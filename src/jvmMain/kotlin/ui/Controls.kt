package ui

import ColorScripts
import Rules
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Controls(
    rules: Rules.Colors,
    submitNewRules: (Rules.Colors) -> Unit,
    playPause: () -> Unit,
    isPlaying: Boolean,
    inspectedTileString: String
) {
    Box {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val ruleString = rules.toAnnotatedString()
            var pendingRules by remember { mutableStateOf(rules.toString()) }
            var selectedRule by remember {
                mutableStateOf(ColorScripts.byRules(rules)?.name ?: "Custom")
            }

            Column(
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                var expandedDropdown by remember { mutableStateOf(false) }
                val dropdownIcon = if (expandedDropdown) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                }
                OutlinedTextField(
                    value = selectedRule,
                    onValueChange = { selectedRule = it },
                    label = { Text("Select pre-made script") },
                    trailingIcon = {
                        Icon(
                            imageVector = dropdownIcon,
                            contentDescription = dropdownIcon.name,
                            modifier = Modifier.clickable { expandedDropdown = !expandedDropdown }
                        )
                    },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ColorScripts.values().forEach { script ->
                        DropdownMenuItem(onClick = {
                            pendingRules = script.copy().toString()
                            selectedRule = script.name
                            expandedDropdown = false
                        }) {
                            Text(script.name)
                        }
                    }
                }
                TextField(
                    value = pendingRules,
                    onValueChange = { pendingRules = it },
                    enabled = true
                )
            }
            Column(
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val newRules = Rules.Colors.fromString(pendingRules)
                        selectedRule = ColorScripts.byRules(newRules)?.name ?: "Custom"
                        submitNewRules(newRules)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = Icons.Default.Refresh.name,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Button(
                    onClick = {
                        playPause()
                    }
                ) {
                    val (vector, description) = when (isPlaying) {
                        true -> Icons.Default.Pause
                        false -> Icons.Default.PlayArrow
                    }.let { it to it.name }

                    Icon(
                        imageVector = vector,
                        contentDescription = description,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Text(text = ruleString, modifier = Modifier.background(Color.LightGray))
            Text(text = inspectedTileString, modifier = Modifier.background(Color.LightGray))
        }
    }
}