package com.example.czg_jlg_sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.czg_jlg_sicedroidmultiplatform.model.ProfileStudent
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.ProfileUiState

// Colores personalizados
private val DarkBlue = Color(0xFF1B396A)
private val CoolGray = Color(0xFF807E82)
private val DarkText = Color(0xFF000000)
private val LightBackground = Color(0xFFF5F5F5)

@Composable
fun ProfileScreen(
    profileUiState: ProfileUiState,
    onLogoutClick: () -> Unit,
    onLoadProfile: (String) -> Unit,
    matricula: String,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(matricula) {
        onLoadProfile(matricula)
    }

    when (profileUiState) {
        is ProfileUiState.Loading, is ProfileUiState.Idle -> {
            LoadingScreen(modifier = modifier)
        }
        is ProfileUiState.Success -> {
            ProfileDetailScreen(
                profile = profileUiState.profile,
                onLogoutClick = onLogoutClick,
                modifier = modifier
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(
                error = profileUiState.message,
                onBackClick = { onLoadProfile(matricula) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = DarkBlue)
        Text("Cargando perfil...", modifier = Modifier.padding(top = 16.dp), color = CoolGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDetailScreen(
    profile: ProfileStudent,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información Personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información Personal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(DarkBlue, shape = RoundedCornerShape(4.dp))
                    ) {}
                    ProfileInfoRow(label = "Matrícula", value = profile.matricula)
                    ProfileInfoRow(label = "Nombre", value = profile.nombre)
                }
            }

            // Información Académica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información Académica",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(DarkBlue, shape = RoundedCornerShape(4.dp))
                    ) {}
                    ProfileInfoRow(label = "Carrera", value = profile.carrera)
                    ProfileInfoRow(label = "Semestre Actual", value = profile.semActual)
                    ProfileInfoRowExpandible(label = "Especialidad", value = profile.especialidad)
                    ProfileInfoRow(label = "Créditos Acumulados", value = profile.cdtsReunidos)
                    ProfileInfoRow(label = "Créditos Actuales", value = profile.cdtsActuales)
                    ProfileInfoRow(label = "Inscrito", value = if (profile.inscrito == "true") "Si" else "No")
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = CoolGray,
            modifier = Modifier.weight(0.4f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = DarkText,
            modifier = Modifier.weight(0.6f),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ProfileInfoRowExpandible(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = CoolGray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            color = DarkText,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Error",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Text(
            error,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = CoolGray
        )
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
        ) {
            Text("Atrás", color = Color.White)
        }
    }
}
