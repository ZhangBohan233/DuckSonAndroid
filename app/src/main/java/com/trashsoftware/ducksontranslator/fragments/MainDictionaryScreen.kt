package com.trashsoftware.ducksontranslator.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
@Composable
fun MainDictionaryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Hello Compose!", style = MaterialTheme.typography.headlineMedium)
        TextField(
            value = "",
            onValueChange = {},
            label = { Text("Search...") }
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* TODO */ }) {
            Text("Search")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DictionaryScreenPreview() {
    MaterialTheme {
        MainDictionaryScreen()
    }
}
