package it.simosw.enginegallery

import android.webkit.CookieManager
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

object AndroidPushRegistration {
    private const val TAG = "AndroidPushRegistration"
    private val executor = Executors.newSingleThreadExecutor()

    fun getAdminCsrf(callback: (String?) -> Unit) {
        executor.execute {
            val csrf = try {
                val endpoint = endpoint()
                val cookies = CookieManager.getInstance().getCookie(endpoint)
                if (cookies.isNullOrBlank()) null else request("GET", endpoint, cookies, null)
            } catch (ex: Exception) {
                Log.w(TAG, "Controllo notifiche Android fallito", ex)
                null
            }
            callback(csrf)
        }
    }

    fun register(token: String, csrf: String? = null, onResult: ((Boolean) -> Unit)? = null) {
        executor.execute {
            val registered = try {
                val endpoint = endpoint()
                val cookies = CookieManager.getInstance().getCookie(endpoint)
                if (cookies.isNullOrBlank()) {
                    false
                } else {
                    val tokenCsrf = csrf ?: request("GET", endpoint, cookies, null)
                    if (tokenCsrf == null) {
                        false
                    } else {
                        val body = "csrfToken=${encode(tokenCsrf)}&token=${encode(token)}"
                        request("POST", endpoint, cookies, body) == "registered"
                    }
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Registrazione notifiche Android fallita", ex)
                false
            }
            onResult?.invoke(registered)
        }
    }

    fun unregister(token: String, csrf: String, onResult: (Boolean) -> Unit) {
        executor.execute {
            val removed = try {
                val endpoint = endpoint()
                val cookies = CookieManager.getInstance().getCookie(endpoint)
                if (cookies.isNullOrBlank()) false else {
                    val body = "csrfToken=${encode(csrf)}&token=${encode(token)}&enabled=false"
                    request("POST", endpoint, cookies, body) == "registered"
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Disattivazione notifiche Android fallita", ex)
                false
            }
            onResult(removed)
        }
    }

    private fun endpoint(): String = BuildConfig.ENGINE_GALLERY_BASE_URL.trimEnd('/') + "/android-push/register"

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun request(method: String, endpoint: String, cookies: String, body: String?): String? {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            instanceFollowRedirects = false
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Cookie", cookies)
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }
        }
        return try {
            if (body != null) connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val responseCode = connection.responseCode
            if (method == "GET" && responseCode == 200
                && connection.contentType?.startsWith("text/plain") == true) {
                connection.inputStream.bufferedReader().use { it.readText().takeIf { token -> token.length in 20..100 } }
            } else {
                if (method == "POST" && responseCode != 204) {
                    Log.w(TAG, "Registrazione notifiche Android: HTTP $responseCode")
                }
                if (method == "POST" && responseCode == 204) "registered" else null
            }
        } finally {
            connection.disconnect()
        }
    }
}
