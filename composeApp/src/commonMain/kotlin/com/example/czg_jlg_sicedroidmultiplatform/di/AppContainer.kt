package com.example.czg_jlg_sicedroidmultiplatform.di

import com.example.czg_jlg_sicedroidmultiplatform.data.NetworkSNRepository
import com.example.czg_jlg_sicedroidmultiplatform.data.SNRepository
import com.example.czg_jlg_sicedroidmultiplatform.data.SessionManager
import com.example.czg_jlg_sicedroidmultiplatform.network.SicenetClient
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.LoginViewModel
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.ProfileViewModel
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.StartupViewModel
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.AcademicViewModel

class AppContainer {
    
    private val sessionManager: SessionManager by lazy {
        SessionManager()
    }

    private val sicenetClient: SicenetClient by lazy {
        SicenetClient(sessionManager)
    }

    val snRepository: SNRepository by lazy {
        NetworkSNRepository(sicenetClient, sessionManager)
    }

    // Factory methods for ViewModels (to be used with ViewModelProvider or directly in Compose)
    fun createLoginViewModel() = LoginViewModel(snRepository)
    fun createProfileViewModel() = ProfileViewModel(snRepository)
    fun createStartupViewModel() = StartupViewModel(snRepository)
    fun createAcademicViewModel() = AcademicViewModel(snRepository)
}

// Global instance or provide via CompositionLocal
val commonAppContainer = AppContainer()
