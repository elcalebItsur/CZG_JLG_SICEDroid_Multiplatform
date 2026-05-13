package com.example.czg_jlg_sicedroidmultiplatform.data

import com.example.czg_jlg_sicedroidmultiplatform.model.*
import com.example.czg_jlg_sicedroidmultiplatform.network.SicenetClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

interface SNRepository {
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    suspend fun getProfile(matricula: String): ProfileStudent
    suspend fun getCarga(matricula: String): List<MateriaCarga>
    suspend fun getKardex(matricula: String): KardexModel
    suspend fun getUnidades(matricula: String): List<CalificacionUnidad>
    suspend fun getFinales(matricula: String): List<CalificacionFinal>
    fun logout()
    fun isLoggedIn(): Boolean
    fun getSavedMatricula(): String
}

class NetworkSNRepository(
    private val client: SicenetClient,
    private val sessionManager: SessionManager
) : SNRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun cleanJson(input: String): String {
        if (input.isEmpty()) return ""
        var cleaned = input.trim()
        
        // Decodificar entidades comunes
        if (cleaned.contains("&lt;") || cleaned.contains("&quot;") || cleaned.contains("&amp;")) {
            cleaned = cleaned.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
        }

        // Extraer contenido JSON
        val firstBracket = cleaned.indexOf('[')
        val firstBrace = cleaned.indexOf('{')
        val start = if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) firstBracket else firstBrace

        if (start != -1) {
            val lastBracket = cleaned.lastIndexOf(']')
            val lastBrace = cleaned.lastIndexOf('}')
            val end = if (lastBracket != -1 && lastBracket > lastBrace) lastBracket else lastBrace

            if (end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1)
            }
        }

        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length - 1)
        }

        cleaned = cleaned.replace("\\\"", "\"")
            .replace("\\\\", "\\")

        return cleaned.trim()
    }

    private fun extractFromXml(xml: String, tagName: String): String {
        val startTag = "<$tagName>"
        val endTag = "</$tagName>"
        val start = xml.indexOf(startTag)
        val end = xml.indexOf(endTag)
        return if (start != -1 && end != -1) {
            xml.substring(start + startTag.length, end)
        } else ""
    }

    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        return try {
            val response = client.acceso(matricula, contrasenia)
            val result = extractFromXml(response, "accesoLoginResult")
            
            var isSuccess = result.equals("true", ignoreCase = true) || 
                            result == "1" || 
                            result.contains("\"acceso\":true", ignoreCase = true)

            // Si falla el primer intento, reintentamos una vez automáticamente.
            // Algunos servidores de Sicenet requieren que se establezca una sesión (cookies)
            // que a veces solo se activa correctamente tras el primer contacto.
            if (!isSuccess) {
                val retryResponse = client.acceso(matricula, contrasenia)
                val retryResult = extractFromXml(retryResponse, "accesoLoginResult")
                isSuccess = retryResult.equals("true", ignoreCase = true) || 
                            retryResult == "1" || 
                            retryResult.contains("\"acceso\":true", ignoreCase = true)
            }

            if (isSuccess) {
                sessionManager.saveMatricula(matricula)
                sessionManager.setLoggedIn(true)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProfile(matricula: String): ProfileStudent {
        return try {
            val response = client.getProfile()
            var result = extractFromXml(response, "getAlumnoAcademicoWithLineamientoResult")
            if (result.isEmpty()) result = extractFromXml(response, "getAlumnoAcademicoResult")
            
            if (result.isEmpty()) return ProfileStudent(matricula = matricula)

            val processed = cleanJson(result)
            if (processed.startsWith("{")) {
                val jsonEl = json.parseToJsonElement(processed).jsonObject
                ProfileStudent(
                    matricula = jsonEl["matricula"]?.jsonPrimitive?.content ?: matricula,
                    nombre = jsonEl["nombre"]?.jsonPrimitive?.content ?: "",
                    carrera = jsonEl["carrera"]?.jsonPrimitive?.content ?: "",
                    semestre = jsonEl["semActual"]?.jsonPrimitive?.content ?: jsonEl["semestre"]?.jsonPrimitive?.content ?: "0",
                    promedio = jsonEl["promedio"]?.jsonPrimitive?.content ?: "0",
                    estado = jsonEl["estatus"]?.jsonPrimitive?.content ?: "",
                    especialidad = jsonEl["especialidad"]?.jsonPrimitive?.content ?: "",
                    cdtsReunidos = jsonEl["cdtosAcumulados"]?.jsonPrimitive?.content ?: "",
                    cdtsActuales = jsonEl["cdtosActuales"]?.jsonPrimitive?.content ?: "",
                    semActual = jsonEl["semActual"]?.jsonPrimitive?.content ?: "",
                    inscrito = jsonEl["inscrito"]?.jsonPrimitive?.content ?: "",
                    estatusAcademico = jsonEl["estatus"]?.jsonPrimitive?.content ?: "",
                    reinscripcionFecha = jsonEl["fechaReins"]?.jsonPrimitive?.content ?: "",
                    sinAdeudos = jsonEl["adeudo"]?.jsonPrimitive?.content ?: ""
                )
            } else ProfileStudent(matricula = matricula)
        } catch (e: Exception) {
            ProfileStudent(matricula = matricula)
        }
    }

    override suspend fun getCarga(matricula: String): List<MateriaCarga> {
        return try {
            val response = client.getCarga()
            val result = extractFromXml(response, "getCargaAcademicaByAlumnoResult")
            if (result.isNotEmpty()) {
                json.decodeFromString<List<MateriaCarga>>(cleanJson(result))
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getKardex(matricula: String): KardexModel {
        return try {
            val response = client.getKardex()
            val result = extractFromXml(response, "getAllKardexConPromedioByAlumnoResult")
            if (result.isNotEmpty()) {
                json.decodeFromString<KardexModel>(cleanJson(result))
            } else KardexModel()
        } catch (e: Exception) {
            KardexModel()
        }
    }

    override suspend fun getUnidades(matricula: String): List<CalificacionUnidad> {
        return try {
            val response = client.getUnidades()
            val result = extractFromXml(response, "getCalifUnidadesByAlumnoResult")
            if (result.isNotEmpty()) {
                json.decodeFromString<List<CalificacionUnidad>>(cleanJson(result))
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFinales(matricula: String): List<CalificacionFinal> {
        return try {
            val response = client.getFinales()
            val result = extractFromXml(response, "getAllCalifFinalByAlumnosResult")
            if (result.isNotEmpty()) {
                json.decodeFromString<List<CalificacionFinal>>(cleanJson(result))
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun logout() {
        sessionManager.clear()
    }

    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    override fun getSavedMatricula(): String = sessionManager.getMatricula()
}
