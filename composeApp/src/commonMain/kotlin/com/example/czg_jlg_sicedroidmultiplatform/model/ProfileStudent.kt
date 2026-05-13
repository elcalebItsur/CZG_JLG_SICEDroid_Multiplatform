package com.example.czg_jlg_sicedroidmultiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val carrera: String = "",
    val semestre: String = "",
    val promedio: String = "",
    val estado: String = "",
    val statusMatricula: String = "",
    val especialidad: String = "",
    val cdtsReunidos: String = "",
    val cdtsActuales: String = "",
    val semActual: String = "",
    val inscrito: String = "",
    val estatusAcademico: String = "",
    val estatusAlumno: String = "",
    val reinscripcionFecha: String = "",
    val sinAdeudos: String = ""
)

@Serializable
data class Usuario(
    val matricula: String = ""
)

@Serializable
data class MateriaParcial(
    val materia: String = "",
    val parciales: List<String> = emptyList()
)
