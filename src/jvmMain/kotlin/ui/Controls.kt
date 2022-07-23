package ui

import ColorScripts
import RuleSet
import RuleSetInterpreter
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
    ruleSet: RuleSet<Color>,
    submitNewRules: (RuleSet<Color>) -> Unit,
    playPause: () -> Unit,
    isPlaying: Boolean,
    inspectedTileString: String
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Color.Gray)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val ruleString = RuleSetInterpreter.Colors.toAnnotatedString(ruleSet)
            var pendingRules by remember {
                mutableStateOf(RuleSetInterpreter.Colors.encodeToString(ruleSet))
            }

            Column(
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                DropdownTextField(
                    defaultValue = "Custom",
                    options = ColorScripts.values().asList(),
                    currentSelectionProvider = { ColorScripts.byString(pendingRules) },
                    displayName = { it.name },
                    onSelected = { pendingRules = it.ruleString }
                )
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
                        val newRuleSet = RuleSetInterpreter.Colors.decodeRuleSetSafely(pendingRules)
                        submitNewRules(newRuleSet)
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

@Composable
private fun <T> DropdownTextField(
    @Suppress("SameParameterValue")
    defaultValue: String = "",
    options: Collection<T>,
    currentSelectionProvider: () -> T?,
    displayName: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dropdownIcon = if (expanded) {
        Icons.Filled.KeyboardArrowUp
    } else {
        Icons.Filled.KeyboardArrowDown
    }

    var selectionText = currentSelectionProvider()?.let { selection ->
        displayName(selection)
    } ?: defaultValue

    OutlinedTextField(
        value = selectionText,
        onValueChange = {},
        label = { Text("Select pre-made script") },
        trailingIcon = {
            Icon(
                imageVector = dropdownIcon,
                contentDescription = dropdownIcon.name,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        },
        readOnly = true
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val name = displayName(option)
            DropdownMenuItem(onClick = {
                onSelected(option)
                selectionText = name
                expanded = false
            }) {
                Text(name)
            }
        }
    }
}