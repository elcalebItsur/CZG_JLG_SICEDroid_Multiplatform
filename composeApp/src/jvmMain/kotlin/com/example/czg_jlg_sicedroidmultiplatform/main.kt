package com.example.czg_jlg_sicedroidmultiplatform

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SICEDroid - ITSUR",
        state = rememberWindowState(width = 400.dp, height = 800.dp)
    ) {
        App()
    }
}