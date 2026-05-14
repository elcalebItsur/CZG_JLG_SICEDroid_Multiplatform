package com.example.czg_jlg_sicedroidmultiplatform.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.example.czg_jlg_sicedroidmultiplatform.data.SessionManager
import com.example.czg_jlg_sicedroidmultiplatform.getPlatform

class SicenetClient(
    private val sessionManager: SessionManager
) {
    // Detectamos si la plataforma es Web (Wasm o JS) para aplicar el proxy
    private val isWeb = getPlatform().name.let { 
        it.contains("Web", true) || it.contains("Wasm", true) || it.contains("JS", true) 
    }

    private val baseUrl = "https://sicenet.itsur.edu.mx"
    // URL de nuestro propio backend proxy
    private val webProxyUrl = "http://localhost:3000/soap"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    private suspend fun soapRequest(
        action: String,
        body: String
    ): String {
        val cookies = sessionManager.getCookies()
        
        val finalUrl = if (isWeb) webProxyUrl else "$baseUrl/ws/wsalumnos.asmx"
        
        val response = client.post(finalUrl) {
            // ELIMINAR ESPACIOS CRÍTICOS: trim() asegura que no haya nada antes de <?xml
            setBody(body.trim())
            
            header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
            header("SOAPAction", "\"http://tempuri.org/$action\"")
            
            if (isWeb) {
                // En Web usamos un header personalizado para que el proxy lo convierta en Cookie
                if (cookies.isNotEmpty()) {
                    header("X-Proxy-Cookie", cookies.joinToString("; "))
                }
            } else {
                // En Mobile/Desktop enviamos el header Cookie estándar
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x86) AppleWebKit/537.36")
                if (cookies.isNotEmpty()) {
                    header(HttpHeaders.Cookie, cookies.joinToString("; "))
                }
            }
        }

        val responseText = response.bodyAsText()
        val currentCookies = sessionManager.getCookies().toMutableSet()
        
        // 1. Intentar capturar del header estándar Set-Cookie
        response.headers.getAll(HttpHeaders.SetCookie)?.forEach { header ->
            header.split(";").firstOrNull()?.let { currentCookies.add(it) }
        }
        
        // 2. Si es Web, el proxy nos envía las cookies en X-Proxy-Set-Cookie
        if (isWeb) {
            response.headers["X-Proxy-Set-Cookie"]?.let { proxyCookiesJson ->
                try {
                    val cookiesList = Json.decodeFromString<List<String>>(proxyCookiesJson)
                    cookiesList.forEach { header ->
                        header.split(";").firstOrNull()?.let { currentCookies.add(it) }
                    }
                } catch (e: Exception) {
                    println("Error parsing proxy cookies: ${e.message}")
                }
            }
        }
        
        if (currentCookies.size > sessionManager.getCookies().size) {
            sessionManager.saveCookies(currentCookies)
        }

        return responseText
    }

    suspend fun acceso(matricula: String, contrasenia: String): String {
        // XML en una sola línea para evitar errores de parseo en el servidor ASP.NET
        val body = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><accesoLogin xmlns="http://tempuri.org/"><strMatricula>${escapeXml(matricula.uppercase())}</strMatricula><strContrasenia>${escapeXml(contrasenia)}</strContrasenia><tipoUsuario>ALUMNO</tipoUsuario></accesoLogin></soap:Body></soap:Envelope>"""
        return soapRequest("accesoLogin", body)
    }

    suspend fun getProfile(): String {
        return soapRequest("getAlumnoAcademicoWithLineamiento", bodyperfil)
    }

    suspend fun getCarga(): String {
        return soapRequest("getCargaAcademicaByAlumno", bodyCarga)
    }

    suspend fun getKardex(): String {
        return soapRequest("getAllKardexConPromedioByAlumno", bodyKardex)
    }

    suspend fun getUnidades(): String {
        return soapRequest("getCalifUnidadesByAlumno", bodyUnidades)
    }

    suspend fun getFinales(): String {
        return soapRequest("getAllCalifFinalByAlumnos", bodyFinal)
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    companion object {
        private const val SOAP_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
        private const val SOAP_END = "</soap:Body></soap:Envelope>"

        private val bodyperfil = "${SOAP_START}<getAlumnoAcademicoWithLineamiento xmlns=\"http://tempuri.org/\" />${SOAP_END}"
        private val bodyCarga = "${SOAP_START}<getCargaAcademicaByAlumno xmlns=\"http://tempuri.org/\" />${SOAP_END}"
        private val bodyKardex = "${SOAP_START}<getAllKardexConPromedioByAlumno xmlns=\"http://tempuri.org/\"><aluLineamiento>1</aluLineamiento></getAllKardexConPromedioByAlumno>${SOAP_END}"
        private val bodyUnidades = "${SOAP_START}<getCalifUnidadesByAlumno xmlns=\"http://tempuri.org/\" />${SOAP_END}"
        private val bodyFinal = "${SOAP_START}<getAllCalifFinalByAlumnos xmlns=\"http://tempuri.org/\"><bytModEducativo>1</bytModEducativo></getAllCalifFinalByAlumnos>${SOAP_END}"
    }
}
