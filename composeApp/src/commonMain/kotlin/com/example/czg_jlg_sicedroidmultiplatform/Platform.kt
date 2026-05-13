package com.example.czg_jlg_sicedroidmultiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform