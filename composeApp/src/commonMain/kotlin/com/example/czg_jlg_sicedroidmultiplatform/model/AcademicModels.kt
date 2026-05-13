package com.example.czg_jlg_sicedroidmultiplatform.model

import kotlinx.serialization.Serializable

@Serializable
data class MateriaCarga(
    val Materia: String = "",
    val Docente: String = "",
    val Grupo: String = "",
    val clvOficial: String = "",
    val Lunes: String? = "",
    val Martes: String? = "",
    val Miercoles: String? = "",
    val Jueves: String? = "",
    val Viernes: String? = "",
    val Sabado: String? = "",
    val CreditosMateria: Int = 0,
    val EstadoMateria: String = "",
    val Observaciones: String = "",
    val Semipresencial: String = ""
)

@Serializable
data class KardexItem(
    val ClvMat: String? = "",
    val ClvOfiMat: String? = "",
    val Materia: String = "",
    val Cdts: Int = 0,
    val Calif: Int = 0,
    val Acred: String = "",
    val P1: String? = "",
    val A1: String? = ""
)

@Serializable
data class SummaryKardex(
    val PromedioGral: Double = 0.0,
    val CdtsAcum: Int = 0,
    val CdtsPlan: Int = 0,
    val MatCursadas: Int = 0,
    val MatAprobadas: Int = 0,
    val AvanceCdts: Double = 0.0
)

@Serializable
data class KardexModel(
    val lstKardex: List<KardexItem> = emptyList(),
    val Promedio: SummaryKardex = SummaryKardex()
)

@Serializable
data class CalificacionUnidad(
    val Materia: String = "",
    val Grupo: String = "",
    val C1: String? = null,
    val C2: String? = null,
    val C3: String? = null,
    val C4: String? = null,
    val C5: String? = null,
    val C6: String? = null,
    val C7: String? = null,
    val C8: String? = null,
    val C9: String? = null,
    val C10: String? = null,
    val C11: String? = null,
    val C12: String? = null,
    val C13: String? = null,
    val UnidadesActivas: String? = "",
    val Observaciones: String? = ""
)

@Serializable
data class CalificacionFinal(
    val materia: String = "",
    val grupo: String = "",
    val calif: Int = 0,
    val acred: String = "",
    val Observaciones: String = ""
)
