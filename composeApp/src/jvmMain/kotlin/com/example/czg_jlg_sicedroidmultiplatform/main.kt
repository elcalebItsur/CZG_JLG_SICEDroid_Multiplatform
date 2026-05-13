package com.example.czg_jlg_sicedroidmultiplatform

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CZG_JLG_SICEDroidMultiplatform",
    ) {
        App()
    }
}