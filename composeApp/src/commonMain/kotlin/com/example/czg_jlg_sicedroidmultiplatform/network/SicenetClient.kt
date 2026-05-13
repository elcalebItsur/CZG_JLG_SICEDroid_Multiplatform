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

class SicenetClient(
    private val sessionManager: SessionManager
) {
    private val baseUrl = "https://sicenet.itsur.edu.mx"

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
            level = LogLevel.BODY
        }
        defaultRequest {
            url(baseUrl)
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x86) AppleWebKit/537.36")
            header("Accept", "*/*")
        }
    }

    private suspend fun soapRequest(
        action: String,
        body: String
    ): String {
        val cookies = sessionManager.getCookies()
        
        val response = client.post("/ws/wsalumnos.asmx") {
            header("Content-Type", "text/xml; charset=utf-8")
            header("SOAPAction", "\"http://tempuri.org/$action\"")
            if (cookies.isNotEmpty()) {
                header("Cookie", cookies.joinToString("; "))
            }
            setBody(body)
        }

        // Capturar cookies del Set-Cookie si existen
        val setCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie)
        if (setCookieHeaders != null && setCookieHeaders.isNotEmpty()) {
            val currentCookies = sessionManager.getCookies().toMutableSet()
            setCookieHeaders.forEach { header ->
                val cookie = header.split(";").firstOrNull()
                if (cookie != null) currentCookies.add(cookie)
            }
            sessionManager.saveCookies(currentCookies)
        }

        return response.bodyAsText()
    }

    suspend fun acceso(matricula: String, contrasenia: String): String {
        val body = bodyacceso.format(escapeXml(matricula.uppercase()), escapeXml(contrasenia))
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
        private val bodyacceso = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <accesoLogin xmlns="http://tempuri.org/">
                  <strMatricula>%s</strMatricula>
                  <strContrasenia>%s</strContrasenia>   
                  <tipoUsuario>ALUMNO</tipoUsuario>
                </accesoLogin>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        private val bodyperfil = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        private val bodyCarga = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        private val bodyKardex = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
                  <aluLineamiento>1</aluLineamiento>
                </getAllKardexConPromedioByAlumno>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        private val bodyUnidades = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        private val bodyFinal = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
                  <bytModEducativo>1</bytModEducativo>
                </getAllCalifFinalByAlumnos>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
