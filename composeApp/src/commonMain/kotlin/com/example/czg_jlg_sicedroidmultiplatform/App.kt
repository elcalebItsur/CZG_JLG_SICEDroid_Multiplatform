package com.example.czg_jlg_sicedroidmultiplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.czg_jlg_sicedroidmultiplatform.di.commonAppContainer
import com.example.czg_jlg_sicedroidmultiplatform.ui.screens.*
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(navController)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "startup"
    ) {
        composable("startup") {
            StartupScreen(navController)
        }
        composable("login") {
            LoginFlow(navController)
        }
        composable("profile/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            ProfileFlow(matricula, navController)
        }
    }
}

@Composable
fun StartupScreen(navController: NavHostController) {
    val viewModel: StartupViewModel = viewModel { commonAppContainer.createStartupViewModel() }
    
    LaunchedEffect(Unit) {
        when (val result = viewModel.checkSession()) {
            is StartupResult.Authenticated -> {
                navController.navigate("profile/${result.matricula}") {
                    popUpTo("startup") { inclusive = true }
                }
            }
            StartupResult.NotAuthenticated -> {
                navController.navigate("login") {
                    popUpTo("startup") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoginFlow(navController: NavHostController) {
    val viewModel: LoginViewModel = viewModel { commonAppContainer.createLoginViewModel() }

    LoginScreen(
        loginUiState = viewModel.loginUiState,
        matricula = viewModel.matricula,
        contrasenia = viewModel.contrasenia,
        onMatriculaChange = { viewModel.updateMatricula(it) },
        onContraseniaChange = { viewModel.updateContrasenia(it) },
        onLoginClick = { viewModel.login() },
        onLoginSuccess = { matricula ->
            navController.navigate("profile/$matricula") {
                popUpTo("login") { inclusive = true }
            }
        },
        onResetForm = {
            viewModel.resetState()
            viewModel.updateMatricula("")
            viewModel.updateContrasenia("")
        }
    )
}

@Composable
fun ProfileFlow(matricula: String, navController: NavHostController) {
    val viewModel: ProfileViewModel = viewModel { commonAppContainer.createProfileViewModel() }
    val scope = rememberCoroutineScope()

    ProfileScreen(
        profileUiState = viewModel.profileUiState,
        onLogoutClick = {
            viewModel.logout()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        },
        onLoadProfile = { viewModel.loadProfile(it) },
        matricula = matricula
    )
}