package com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.czg_jlg_sicedroidmultiplatform.data.SNRepository
import com.example.czg_jlg_sicedroidmultiplatform.model.ProfileStudent
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Idle : ProfileUiState
    data object Loading : ProfileUiState
    data class Success(val profile: ProfileStudent) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(private val snRepository: SNRepository) : ViewModel() {

    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Idle)
        private set

    fun loadProfile(matricula: String) {
        if (matricula.isEmpty()) {
            profileUiState = ProfileUiState.Error("Matrícula inválida")
            return
        }

        viewModelScope.launch {
            profileUiState = ProfileUiState.Loading
            try {
                val profile = snRepository.getProfile(matricula)
                if (profile.nombre.isNotEmpty()) {
                    profileUiState = ProfileUiState.Success(profile)
                } else {
                    profileUiState = ProfileUiState.Error("No se pudo obtener la información del perfil")
                }
            } catch (e: Exception) {
                profileUiState = ProfileUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun logout() {
        snRepository.logout()
    }
}
