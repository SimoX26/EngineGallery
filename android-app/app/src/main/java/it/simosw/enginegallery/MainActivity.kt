package it.simosw.enginegallery

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.concurrent.Executors
import android.net.http.SslError
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EngineGalleryWebView"
        private const val IMAGE_SHARE_TAG = "ImageShare"
        private const val PRIMARY_HOST = "rettificamotorilacroce.it"
        private const val WWW_HOST = "www.rettificamotorilacroce.it"
        private const val LEGACY_IP_HOST = "82.165.20.124"
    }

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            if (callback == null) {
                return@registerForActivityResult
            }

            if (result.resultCode != RESULT_OK) {
                callback.onReceiveValue(null)
                resetFileSelectionState()
                return@registerForActivityResult
            }

            val uris = mutableListOf<Uri>()

            val clipData = result.data?.clipData
            if (clipData != null) {
                for (index in 0 until clipData.itemCount) {
                    clipData.getItemAt(index).uri?.let(uris::add)
                }
            }

            val singleData = result.data?.data
            if (singleData != null && !uris.contains(singleData)) {
                uris.add(singleData)
            }

            callback.onReceiveValue(if (uris.isEmpty()) null else uris.toTypedArray())
            resetFileSelectionState()
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        applySystemInsets()
        setupWebView()
        setupNavigation()

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            val startupUrl = BuildConfig.ENGINE_GALLERY_BASE_URL
            Log.i(TAG, "Startup URL: $startupUrl")
            webView.loadUrl(startupUrl)
        }

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
        swipeRefresh.setOnChildScrollUpCallback { _, _ ->
            webView.canScrollVertically(-1)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    private fun applySystemInsets() {
        val initialLeft = swipeRefresh.paddingLeft
        val initialTop = swipeRefresh.paddingTop
        val initialRight = swipeRefresh.paddingRight
        val initialBottom = swipeRefresh.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(swipeRefresh) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialLeft + systemBars.left,
                initialTop + systemBars.top,
                initialRight + systemBars.right,
                initialBottom + systemBars.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(swipeRefresh)
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, false)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }
        webView.addJavascriptInterface(AndroidShareBridge(), "AndroidShareBridge")
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val upgradedUri = upgradeToHttpsIfNeeded(uri)
                if (upgradedUri != uri) {
                    Log.i(TAG, "Upgrading URL to HTTPS: $uri -> $upgradedUri")
                    view?.loadUrl(upgradedUri.toString())
                    return true
                }
                val scheme = uri.scheme.orEmpty()

                return when {
                    scheme == "http" || scheme == "https" -> false
                    scheme == "tel" || scheme == "mailto" -> {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                        true
                    }
                    else -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                            true
                        } catch (_: ActivityNotFoundException) {
                            false
                        }
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                Log.i(TAG, "Page finished: $url")
                installShareBridgeScript()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    Log.e(
                        TAG,
                        "Main frame error url=${request.url} code=${error?.errorCode} desc=${error?.description}"
                    )
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    Log.e(
                        TAG,
                        "Main frame HTTP error url=${request.url} status=${errorResponse?.statusCode}"
                    )
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                Log.e(
                    TAG,
                    "SSL error primary=${error?.primaryError} url=${error?.url}"
                )
                handler?.cancel()
                Toast.makeText(
                    this@MainActivity,
                    "Errore SSL durante la connessione al server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                val chooserIntent = buildFileChooserIntent()
                fileChooserLauncher.launch(chooserIntent)
                return true
            }
        }
    }

    private fun upgradeToHttpsIfNeeded(uri: Uri): Uri {
        val scheme = uri.scheme?.lowercase().orEmpty()
        if (scheme != "http") {
            return uri
        }
        val host = uri.host?.lowercase().orEmpty()
        val isEngineGalleryHost = host == PRIMARY_HOST || host == WWW_HOST || host == LEGACY_IP_HOST
        if (!isEngineGalleryHost) {
            return uri
        }
        return uri.buildUpon().scheme("https").build()
    }

    override fun onPause() {
        webView.onPause()
        webView.pauseTimers()
        CookieManager.getInstance().flush()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onDestroy() {
        ioExecutor.shutdown()
        super.onDestroy()
    }

    private fun buildFileChooserIntent(): Intent {
        val contentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val cameraSessionIntent = Intent(this, CameraSessionActivity::class.java)

        return Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, contentIntent)
            putExtra(Intent.EXTRA_TITLE, getString(R.string.file_picker_title))
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraSessionIntent))
        }
    }

    private fun resetFileSelectionState() {
        filePathCallback = null
    }

    private fun installShareBridgeScript() {
        val script = """
            (function () {
              if (window.__engineGalleryShareBridgeInstalled) return;
              window.__engineGalleryShareBridgeInstalled = true;
              const originalShare = navigator.share ? navigator.share.bind(navigator) : null;
              navigator.share = function (data) {
                try {
                  const payload = data || {};
                  const imageUrl = payload.url || '';
                  if (window.AndroidShareBridge && imageUrl) {
                    window.AndroidShareBridge.shareImage(imageUrl);
                    return Promise.resolve();
                  }
                  const files = payload.files;
                  if (window.AndroidShareBridge && files && files.length > 0) {
                    const first = files[0];
                    if (first) {
                      return new Promise((resolve, reject) => {
                        try {
                          const reader = new FileReader();
                          reader.onload = function () {
                            try {
                              window.AndroidShareBridge.shareImageData(
                                String(reader.result || ''),
                                String(first.type || ''),
                                String(first.name || '')
                              );
                              resolve();
                            } catch (e) {
                              reject(e);
                            }
                          };
                          reader.onerror = function () {
                            reject(new Error('Errore lettura file condiviso'));
                          };
                          reader.readAsDataURL(first);
                        } catch (e) {
                          reject(e);
                        }
                      });
                    }
                  }
                } catch (_e) {}
                if (originalShare) return originalShare(data);
                return Promise.reject(new Error('Share non disponibile'));
              };
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    inner class AndroidShareBridge {
        @JavascriptInterface
        fun shareImage(imageUrl: String?) {
            // Share only the image payload in Android apps.
            if (imageUrl.isNullOrBlank()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Immagine non disponibile", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val normalizedUrl = upgradeToHttpsIfNeeded(Uri.parse(imageUrl)).toString()
            runOnUiThread {
                val userAgent = webView.settings.userAgentString ?: "Android WebView"
                val referer = webView.url ?: BuildConfig.ENGINE_GALLERY_BASE_URL
                val cookies = CookieManager.getInstance().getCookie(normalizedUrl)

                ioExecutor.execute {
                    val sharedFile = downloadImageForShare(
                        normalizedUrl = normalizedUrl,
                        userAgent = userAgent,
                        referer = referer,
                        cookies = cookies
                    )
                    if (sharedFile == null) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Errore durante la preparazione dell'immagine", Toast.LENGTH_SHORT).show()
                        }
                        return@execute
                    }
                    runOnUiThread {
                        openImageShareChooser(sharedFile)
                    }
                }
            }
        }

        @JavascriptInterface
        fun shareImageData(dataUrl: String?, mimeType: String?, fileName: String?) {
            if (dataUrl.isNullOrBlank()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Immagine non disponibile", Toast.LENGTH_SHORT).show()
                }
                return
            }

            ioExecutor.execute {
                val sharedFile = decodeDataUrlForShare(dataUrl, mimeType, fileName)
                if (sharedFile == null) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Errore durante la preparazione dell'immagine", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }
                runOnUiThread {
                    openImageShareChooser(sharedFile)
                }
            }
        }

        @JavascriptInterface
        fun shareTechnicalSheet(imageUrlsJson: String?, @Suppress("UNUSED_PARAMETER") shareText: String?) {
            if (imageUrlsJson.isNullOrBlank()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Immagini non disponibili", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val normalizedUrls = try {
                val jsonArray = JSONArray(imageUrlsJson)
                val urls = mutableListOf<String>()
                for (index in 0 until jsonArray.length()) {
                    val raw = jsonArray.optString(index).orEmpty().trim()
                    if (raw.isBlank()) {
                        continue
                    }
                    val normalized = upgradeToHttpsIfNeeded(Uri.parse(raw)).toString()
                    urls.add(normalized)
                }
                urls
            } catch (ex: Exception) {
                Log.e(IMAGE_SHARE_TAG, "Payload immagini non valido", ex)
                emptyList()
            }

            if (normalizedUrls.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Immagini non disponibili", Toast.LENGTH_SHORT).show()
                }
                return
            }

            runOnUiThread {
                val userAgent = webView.settings.userAgentString ?: "Android WebView"
                val referer = webView.url ?: BuildConfig.ENGINE_GALLERY_BASE_URL
                val cookiesByUrl = normalizedUrls.associateWith { url ->
                    CookieManager.getInstance().getCookie(url)
                }

                ioExecutor.execute {
                    val sharedFiles = mutableListOf<File>()
                    for (url in normalizedUrls) {
                        val sharedFile = downloadImageForShare(
                            normalizedUrl = url,
                            userAgent = userAgent,
                            referer = referer,
                            cookies = cookiesByUrl[url]
                        )
                        if (sharedFile == null) {
                            Log.w(IMAGE_SHARE_TAG, "Immagine non condivisibile, skip: $url")
                            continue
                        }
                        sharedFiles.add(sharedFile)
                    }

                    if (sharedFiles.isEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Nessuna immagine condivisibile", Toast.LENGTH_SHORT).show()
                        }
                        return@execute
                    }

                    runOnUiThread {
                        openTechnicalSheetShareChooser(sharedFiles)
                    }
                }
            }
        }
    }

    private fun decodeDataUrlForShare(dataUrl: String, mimeType: String?, fileName: String?): File? {
        return try {
            val commaIndex = dataUrl.indexOf(',')
            if (commaIndex <= 0) {
                Log.e(IMAGE_SHARE_TAG, "Data URL non valido")
                return null
            }
            val metadata = dataUrl.substring(0, commaIndex)
            if (!metadata.contains(";base64")) {
                Log.e(IMAGE_SHARE_TAG, "Data URL senza base64")
                return null
            }
            val base64Payload = dataUrl.substring(commaIndex + 1)
            val bytes = Base64.getDecoder().decode(base64Payload)
            if (bytes.isEmpty()) {
                Log.e(IMAGE_SHARE_TAG, "Data URL vuoto")
                return null
            }

            val shareDir = File(cacheDir, "shared").apply { mkdirs() }
            val extension = detectImageExtension(mimeType, fileName)
            val imageFile = File.createTempFile("engine_gallery_share_", ".$extension", shareDir)
            imageFile.outputStream().use { output ->
                output.write(bytes)
            }
            imageFile
        } catch (ex: Exception) {
            Log.e(IMAGE_SHARE_TAG, "Errore decoding immagine condivisa", ex)
            null
        }
    }

    private fun detectImageExtension(mimeType: String?, fileName: String?): String {
        val fromMime = mimeType
            ?.substringAfter('/', "")
            ?.lowercase()
            ?.substringBefore(';')
            ?.ifBlank { "" }
            .orEmpty()
        if (fromMime in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif")) {
            return if (fromMime == "jpeg") "jpg" else fromMime
        }

        val fromName = fileName
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.ifBlank { "" }
            .orEmpty()
        if (fromName in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif")) {
            return if (fromName == "jpeg") "jpg" else fromName
        }
        return "jpg"
    }

    private fun downloadImageForShare(
        normalizedUrl: String,
        userAgent: String,
        referer: String,
        cookies: String?
    ): File? {
        var connection: HttpURLConnection? = null
        return try {
            val shareDir = File(cacheDir, "shared").apply { mkdirs() }
            val extension = normalizedUrl.substringAfterLast('.', "jpg")
                .substringBefore('?')
                .substringBefore('#')
                .lowercase()
                .ifBlank { "jpg" }
            val safeExtension = if (extension.length in 2..5) extension else "jpg"
            val imageFile = File.createTempFile("engine_gallery_share_", ".$safeExtension", shareDir)

            connection = (URL(normalizedUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15000
                readTimeout = 20000
                requestMethod = "GET"
                doInput = true
                if (!cookies.isNullOrBlank()) {
                    setRequestProperty("Cookie", cookies)
                }
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "image/*,*/*;q=0.8")
                setRequestProperty("Referer", referer)
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                val body = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (_: Exception) {
                    ""
                }
                Log.e(TAG, "Share download HTTP error code=$code url=$normalizedUrl body=${body.take(200)}")
                Log.e(IMAGE_SHARE_TAG, "Errore preparazione immagine: HTTP $code url=$normalizedUrl body=${body.take(200)}")
                return null
            }

            connection.inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            imageFile
        } catch (ex: Exception) {
            Log.e(TAG, "Share download failed for url=$normalizedUrl", ex)
            Log.e(IMAGE_SHARE_TAG, "Errore preparazione immagine", ex)
            null
        } finally {
            try {
                connection?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private fun openImageShareChooser(imageFile: File) {
        val imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, "engine-image", imageUri)
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Condividi immagine"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Nessuna app disponibile per la condivisione", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTechnicalSheetShareChooser(imageFiles: List<File>) {
        if (imageFiles.isEmpty()) {
            Toast.makeText(this, "Immagini non disponibili", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUris = imageFiles.map { file ->
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        }

        val shareIntent = if (imageUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(contentResolver, "engine-image", imageUris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(contentResolver, "engine-image", imageUris.first())
            }
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Condividi scheda tecnica"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Nessuna app disponibile per la condivisione", Toast.LENGTH_SHORT).show()
        }
    }
}
