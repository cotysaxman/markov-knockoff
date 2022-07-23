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
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Color.Gray)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val ruleString = rules.toAnnotatedString()
            var pendingRules by remember { mutableStateOf(rules.toString()) }

            Column(
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                DropdownTextField(
                    defaultValue = "Custom",
                    options = ColorScripts.values().asList(),
                    isSelected = { ColorScripts.byRules(rules) == it },
                    displayName = { it.name },
                    onSelected = { pendingRules = it.copy().toString() }
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
                        val newRules = Rules.Colors.fromString(pendingRules)
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

@Composable
private fun <T> DropdownTextField(
    @Suppress("SameParameterValue")
    defaultValue: String = "",
    options: Collection<T>,
    isSelected: (T) -> Boolean,
    displayName: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dropdownIcon = if (expanded) {
        Icons.Filled.KeyboardArrowUp
    } else {
        Icons.Filled.KeyboardArrowDown
    }

    val initialSelection = options.firstOrNull { isSelected(it) }?.let { currentSelection ->
        displayName(currentSelection)
    } ?: defaultValue
    var selectionText by remember {
        mutableStateOf(initialSelection)
    }

    OutlinedTextField(
        value = selectionText,
        onValueChange = { selectionText = it },
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