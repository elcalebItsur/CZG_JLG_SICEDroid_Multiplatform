package com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.czg_jlg_sicedroidmultiplatform.data.SNRepository

sealed class StartupResult {
    data class Authenticated(val matricula: String) : StartupResult()
    data object NotAuthenticated : StartupResult()
}

class StartupViewModel(
    private val repository: SNRepository
) : ViewModel() {

    suspend fun checkSession(): StartupResult {
        return if (repository.isLoggedIn()) {
            val matricula = repository.getSavedMatricula()
            if (matricula.isNotEmpty()) {
                StartupResult.Authenticated(matricula)
            } else {
                StartupResult.NotAuthenticated
            }
        } else {
            StartupResult.NotAuthenticated
        }
    }
}
