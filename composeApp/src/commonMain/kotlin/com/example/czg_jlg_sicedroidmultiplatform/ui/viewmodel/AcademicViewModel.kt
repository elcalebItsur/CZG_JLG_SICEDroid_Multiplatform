package com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.czg_jlg_sicedroidmultiplatform.data.SNRepository
import com.example.czg_jlg_sicedroidmultiplatform.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AcademicUiState<out T> {
    data object Idle : AcademicUiState<Nothing>
    data object Loading : AcademicUiState<Nothing>
    data class Success<T>(val data: T) : AcademicUiState<T>
    data class Error(val message: String) : AcademicUiState<Nothing>
}

class AcademicViewModel(private val snRepository: SNRepository) : ViewModel() {

    var cargaUiState: AcademicUiState<List<MateriaCarga>> by mutableStateOf(AcademicUiState.Idle)
        private set

    var kardexUiState: AcademicUiState<KardexModel> by mutableStateOf(AcademicUiState.Idle)
        private set

    var unidadesUiState: AcademicUiState<List<CalificacionUnidad>> by mutableStateOf(AcademicUiState.Idle)
        private set

    var finalesUiState: AcademicUiState<List<CalificacionFinal>> by mutableStateOf(AcademicUiState.Idle)
        private set

    private fun normalize(m: String) = m.trim().uppercase()
    private fun isInvalid(m: String) = m.isEmpty() || m.contains("{") || m.contains("}")

    fun loadCarga(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            cargaUiState = AcademicUiState.Loading
            try {
                val data = snRepository.getCarga(m)
                cargaUiState = AcademicUiState.Success(data)
            } catch (e: Exception) {
                cargaUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadKardex(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            kardexUiState = AcademicUiState.Loading
            try {
                val data = snRepository.getKardex(m)
                kardexUiState = AcademicUiState.Success(data)
            } catch (e: Exception) {
                kardexUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadUnidades(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            unidadesUiState = AcademicUiState.Loading
            try {
                val data = snRepository.getUnidades(m)
                unidadesUiState = AcademicUiState.Success(data)
            } catch (e: Exception) {
                unidadesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadFinales(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            finalesUiState = AcademicUiState.Loading
            try {
                val data = snRepository.getFinales(m)
                finalesUiState = AcademicUiState.Success(data)
            } catch (e: Exception) {
                finalesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
