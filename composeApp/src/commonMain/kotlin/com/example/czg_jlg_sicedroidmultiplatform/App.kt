package com.example.czg_jlg_sicedroidmultiplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.czg_jlg_sicedroidmultiplatform.di.commonAppContainer
import com.example.czg_jlg_sicedroidmultiplatform.ui.screens.*
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "startup"

        // Decidir si mostrar el drawer (solo en pantallas académicas)
        val showDrawer = currentRoute != "login" && currentRoute != "startup"
        
        // Obtener matrícula actual del repositorio para las rutas
        val sessionMatricula = remember(currentRoute) { commonAppContainer.snRepository.getSavedMatricula() }

        if (showDrawer) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1B396A))
                                .padding(24.dp)
                        ) {
                            Text(
                                "SICENET Menu",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        NavigationDrawerItem(
                            label = { Text("Mi Perfil") },
                            selected = currentRoute.startsWith("profile"),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("profile/$sessionMatricula") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Carga Académica") },
                            selected = currentRoute.startsWith("carga"),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("carga/$sessionMatricula") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Kardex") },
                            selected = currentRoute.startsWith("kardex"),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("kardex/$sessionMatricula") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Calificaciones Unidad") },
                            selected = currentRoute.startsWith("unidades"),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("unidades/$sessionMatricula") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.Info, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Calificación Final") },
                            selected = currentRoute.startsWith("final"),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("final/$sessionMatricula") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        NavigationDrawerItem(
                            label = { Text("Cerrar Sesión") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                commonAppContainer.snRepository.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(navController, drawerState)
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavHost(navController, drawerState)
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
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
        composable(
            route = "profile/{matricula}",
            arguments = listOf(navArgument("matricula") { type = NavType.StringType })
        ) { backStackEntry ->
            val matricula = backStackEntry.savedStateHandle.get<String>("matricula") ?: ""
            ProfileFlow(matricula, navController) { scope.launch { drawerState.open() } }
        }
        composable(
            route = "carga/{matricula}",
            arguments = listOf(navArgument("matricula") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.savedStateHandle.get<String>("matricula") ?: ""
            val academicViewModel: AcademicViewModel = viewModel { commonAppContainer.createAcademicViewModel() }
            AcademicDataScreen("Carga Académica", m, "CARGA", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }
        composable(
            route = "kardex/{matricula}",
            arguments = listOf(navArgument("matricula") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.savedStateHandle.get<String>("matricula") ?: ""
            val academicViewModel: AcademicViewModel = viewModel { commonAppContainer.createAcademicViewModel() }
            AcademicDataScreen("Kardex", m, "KARDEX", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }
        composable(
            route = "unidades/{matricula}",
            arguments = listOf(navArgument("matricula") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.savedStateHandle.get<String>("matricula") ?: ""
            val academicViewModel: AcademicViewModel = viewModel { commonAppContainer.createAcademicViewModel() }
            AcademicDataScreen("Calificaciones Unidad", m, "UNIDADES", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }
        composable(
            route = "final/{matricula}",
            arguments = listOf(navArgument("matricula") { type = NavType.StringType })
        ) { backStackEntry ->
            val m = backStackEntry.savedStateHandle.get<String>("matricula") ?: ""
            val academicViewModel: AcademicViewModel = viewModel { commonAppContainer.createAcademicViewModel() }
            AcademicDataScreen("Calificación Final", m, "FINAL", academicViewModel) {
                scope.launch { drawerState.open() }
            }
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
        CircularProgressIndicator(color = Color(0xFF1B396A))
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
fun ProfileFlow(matricula: String, navController: NavHostController, onMenuClick: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel { commonAppContainer.createProfileViewModel() }

    ProfileScreen(
        profileUiState = viewModel.profileUiState,
        onMenuClick = onMenuClick,
        onLoadProfile = { viewModel.loadProfile(it) },
        matricula = matricula
    )
}