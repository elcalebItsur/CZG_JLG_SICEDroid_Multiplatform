package com.example.czg_jlg_sicedroidmultiplatform.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SessionManager(private val settings: Settings = Settings()) {

    companion object {
        private const val KEY_COOKIES = "PREF_COOKIES"
        private const val KEY_MATRICULA = "pref_matricula"
        private const val KEY_IS_LOGGED = "is_logged"
    }

    fun saveCookies(cookies: Set<String>) {
        val cookiesString = cookies.joinToString(";")
        settings[KEY_COOKIES] = cookiesString
    }

    fun getCookies(): Set<String> {
        val cookiesString = settings.getString(KEY_COOKIES, "")
        return if (cookiesString.isEmpty()) emptySet() else cookiesString.split(";").toSet()
    }

    fun saveMatricula(matricula: String) {
        settings[KEY_MATRICULA] = matricula
    }

    fun getMatricula(): String {
        return settings.getString(KEY_MATRICULA, "")
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        settings[KEY_IS_LOGGED] = isLoggedIn
    }

    fun isLoggedIn(): Boolean {
        return settings.getBoolean(KEY_IS_LOGGED, false)
    }

    fun clear() {
        settings.remove(KEY_COOKIES)
        settings.remove(KEY_MATRICULA)
        settings.remove(KEY_IS_LOGGED)
    }
}
