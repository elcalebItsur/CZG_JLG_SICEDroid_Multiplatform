package com.example.czg_jlg_sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.czg_jlg_sicedroidmultiplatform.model.*
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.AcademicUiState
import com.example.czg_jlg_sicedroidmultiplatform.ui.viewmodel.AcademicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDataScreen(
    title: String,
    matricula: String,
    type: String,
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit
) {
    // Observar el estado de carga del ViewModel según el tipo
    val currentUiState = when (type) {
        "CARGA" -> viewModel.cargaUiState
        "KARDEX" -> viewModel.kardexUiState
        "UNIDADES" -> viewModel.unidadesUiState
        "FINAL" -> viewModel.finalesUiState
        else -> AcademicUiState.Idle
    }

    // Cargar datos automáticamente al entrar
    LaunchedEffect(matricula, type) {
        if (currentUiState is AcademicUiState.Idle) {
            when (type) {
                "CARGA" -> viewModel.loadCarga(matricula)
                "KARDEX" -> viewModel.loadKardex(matricula)
                "UNIDADES" -> viewModel.loadUnidades(matricula)
                "FINAL" -> viewModel.loadFinales(matricula)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B396A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (type) {
                        "CARGA" -> viewModel.loadCarga(matricula)
                        "KARDEX" -> viewModel.loadKardex(matricula)
                        "UNIDADES" -> viewModel.loadUnidades(matricula)
                        "FINAL" -> viewModel.loadFinales(matricula)
                    }
                },
                containerColor = Color(0xFF1B396A),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF5F5F5))) {
            // Contenido principal
            when (currentUiState) {
                is AcademicUiState.Success<*> -> {
                    when (type) {
                        "CARGA" -> CargaTableContent(currentUiState.data as List<MateriaCarga>)
                        "KARDEX" -> KardexContent(currentUiState.data as KardexModel)
                        "UNIDADES" -> UnidadesContent(currentUiState.data as List<CalificacionUnidad>)
                        "FINAL" -> FinalesContent(currentUiState.data as List<CalificacionFinal>)
                    }
                }
                is AcademicUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1B396A))
                    }
                }
                is AcademicUiState.Error -> {
                    EmptyState(currentUiState.message)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CargaTableContent(items: List<MateriaCarga>) {
    if (items.isEmpty()) {
        EmptyState("No hay carga académica disponible")
    } else {
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { materia ->
                CargaTableRow(materia)
            }
        }
    }
}

@Composable
fun CargaTableRow(materia: MateriaCarga) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = materia.Materia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B396A)
            )
            Text(text = materia.Docente, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Text(text = " Grupo: ${materia.Grupo}", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Text(text = " Créditos: ${materia.CreditosMateria}", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            ScheduleGrid(materia)
        }
    }
}

@Composable
fun ScheduleGrid(materia: MateriaCarga) {
    val dias = listOf("L", "M", "Mi", "J", "V")
    val horarios = listOf(materia.Lunes, materia.Martes, materia.Miercoles, materia.Jueves, materia.Viernes)
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dias.forEachIndexed { index, dia ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dia, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(
                    text = horarios[index]?.takeIf { it.isNotBlank() } ?: "-",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun KardexContent(kardexModel: KardexModel) {
    if (kardexModel.lstKardex.isEmpty()) {
        EmptyState("No hay información en el kardex")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                KardexSummaryCard(kardexModel.Promedio)
            }
            items(kardexModel.lstKardex) { item ->
                KardexItemRow(item)
            }
        }
    }
}

@Composable
fun KardexSummaryCard(summary: SummaryKardex) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B396A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "Promedio General", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                Text(text = summary.PromedioGral.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Avance: ${summary.AvanceCdts}%", fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${summary.CdtsAcum} / ${summary.CdtsPlan} Créditos", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun KardexItemRow(item: KardexItem) {
    val isAproved = item.Calif >= 70
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).background(if (isAproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(text = item.Calif.toString(), fontWeight = FontWeight.Bold, color = if (isAproved) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.Materia, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${item.P1} ${item.A1} • ${item.Acred}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(text = "${item.Cdts} cdt", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun UnidadesContent(items: List<CalificacionUnidad>) {
    if (items.isEmpty()) {
        EmptyState("No hay calificaciones registradas")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item ->
                UnidadesCard(item)
            }
        }
    }
}

@Composable
fun UnidadesCard(item: CalificacionUnidad) {
    val units = listOf(item.C1, item.C2, item.C3, item.C4, item.C5, item.C6, item.C7, item.C8, item.C9, item.C10, item.C11, item.C12, item.C13)
        .filterNotNull()
        .filter { it != "null" && it.isNotEmpty() }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.Materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B396A))
            Text(text = "Grupo: ${item.Grupo}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                units.forEachIndexed { index, calif ->
                    val score = calif.toIntOrNull() ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "U${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Box(modifier = Modifier.padding(top = 2.dp).fillMaxWidth().height(30.dp).background(if (score >= 70) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Text(text = calif, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinalesContent(items: List<CalificacionFinal>) {
    if (items.isEmpty()) {
        EmptyState("No hay calificaciones finales")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                FinalGradeRow(item)
            }
        }
    }
}

@Composable
fun FinalGradeRow(item: CalificacionFinal) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.materia, fontWeight = FontWeight.Bold, color = Color(0xFF1B396A))
                Text(text = "${item.grupo} • ${item.acred}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = item.calif.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (item.calif >= 70) Color(0xFF2E7D32) else Color(0xFFC62828))
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
