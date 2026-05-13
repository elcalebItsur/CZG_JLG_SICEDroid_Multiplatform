package com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.czg_jlg_sicedroidmultiplatform.data.SNRepository
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val matricula: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val snRepository: SNRepository) : ViewModel() {
    
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set
    
    var matricula: String by mutableStateOf("")
        private set
    
    var contrasenia: String by mutableStateOf("")
        private set
    
    fun updateMatricula(newValue: String) {
        matricula = newValue
    }
    
    fun updateContrasenia(newValue: String) {
        contrasenia = newValue
    }

    fun resetState() {
        loginUiState = LoginUiState.Idle
    }

    fun login() {
        if (matricula.isBlank() || contrasenia.isBlank()) {
            loginUiState = LoginUiState.Error("Por favor ingresa matrícula y contraseña")
            return
        }

        viewModelScope.launch {
            loginUiState = LoginUiState.Loading
            try {
                val success = snRepository.acceso(matricula, contrasenia)
                if (success) {
                    loginUiState = LoginUiState.Success(matricula)
                } else {
                    loginUiState = LoginUiState.Error("Matrícula o contraseña incorrecta")
                }
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
