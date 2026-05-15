package it.simosw.enginegallery

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraUri: Uri? = null
    private val cameraUris = mutableListOf<Uri>()
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            if (callback == null) {
                return@registerForActivityResult
            }

            if (result.resultCode != RESULT_OK) {
                deliverSelectedUris(callback)
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
            if (singleData != null) {
                uris.add(singleData)
            }

            if (uris.isNotEmpty()) {
                callback.onReceiveValue(uris.toTypedArray())
                resetFileSelectionState()
                return@registerForActivityResult
            }

            if (cameraUri != null) {
                cameraUris.add(cameraUri!!)
                showContinueCameraDialog(callback)
                return@registerForActivityResult
            }

            deliverSelectedUris(callback)
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
            webView.loadUrl(BuildConfig.ENGINE_GALLERY_BASE_URL)
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
                installShareBridgeScript()
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
                resetCameraSelection()

                val chooserIntent = buildFileChooserIntent()
                fileChooserLauncher.launch(chooserIntent)
                return true
            }
        }
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

        val cameraIntent = buildCameraCaptureIntent()

        return Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, contentIntent)
            putExtra(Intent.EXTRA_TITLE, getString(R.string.file_picker_title))
            if (cameraIntent != null) {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
            }
        }
    }

    private fun buildCameraCaptureIntent(): Intent? {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = createTempImageFile()
        val cameraAvailable = cameraIntent.resolveActivity(packageManager) != null
        if (!cameraAvailable || imageFile == null) {
            cameraUri = null
            return null
        }

        cameraUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return cameraIntent
    }

    private fun showContinueCameraDialog(callback: ValueCallback<Array<Uri>>) {
        AlertDialog.Builder(this)
            .setTitle(R.string.camera_continue_title)
            .setMessage(R.string.camera_continue_message)
            .setPositiveButton(R.string.camera_continue_positive) { _, _ ->
                val nextCameraIntent = buildCameraCaptureIntent()
                if (nextCameraIntent == null) {
                    deliverSelectedUris(callback)
                } else {
                    fileChooserLauncher.launch(nextCameraIntent)
                }
            }
            .setNegativeButton(R.string.camera_continue_negative) { _, _ ->
                deliverSelectedUris(callback)
            }
            .setOnCancelListener {
                deliverSelectedUris(callback)
            }
            .show()
    }

    private fun deliverSelectedUris(callback: ValueCallback<Array<Uri>>) {
        callback.onReceiveValue(if (cameraUris.isEmpty()) null else cameraUris.toTypedArray())
        resetFileSelectionState()
    }

    private fun resetCameraSelection() {
        cameraUri = null
        cameraUris.clear()
    }

    private fun resetFileSelectionState() {
        filePathCallback = null
        resetCameraSelection()
    }

    private fun createTempImageFile(): File? {
        return try {
            val dir = File(cacheDir, "camera").apply { mkdirs() }
            File.createTempFile("engine_gallery_", ".jpg", dir)
        } catch (_: Exception) {
            Toast.makeText(this, R.string.file_error, Toast.LENGTH_SHORT).show()
            null
        }
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
                    window.AndroidShareBridge.shareImage(imageUrl, payload.title || '', payload.text || '');
                    return Promise.resolve();
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
        fun shareImage(imageUrl: String?, title: String?, text: String?) {
            if (imageUrl.isNullOrBlank()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Immagine non disponibile", Toast.LENGTH_SHORT).show()
                }
                return
            }

            ioExecutor.execute {
                val sharedFile = downloadImageForShare(imageUrl)
                if (sharedFile == null) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Errore durante la preparazione dell'immagine", Toast.LENGTH_SHORT).show()
                    }
                    return@execute
                }
                runOnUiThread {
                    openImageShareChooser(sharedFile, title, text)
                }
            }
        }
    }

    private fun downloadImageForShare(imageUrl: String): File? {
        return try {
            val shareDir = File(cacheDir, "shared").apply { mkdirs() }
            val extension = imageUrl.substringAfterLast('.', "jpg")
                .substringBefore('?')
                .substringBefore('#')
                .lowercase()
                .ifBlank { "jpg" }
            val safeExtension = if (extension.length in 2..5) extension else "jpg"
            val imageFile = File.createTempFile("engine_gallery_share_", ".$safeExtension", shareDir)

            URL(imageUrl).openStream().use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            imageFile
        } catch (_: Exception) {
            null
        }
    }

    private fun openImageShareChooser(imageFile: File, title: String?, text: String?) {
        val imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            if (!title.isNullOrBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            if (!text.isNullOrBlank()) {
                putExtra(Intent.EXTRA_TEXT, text)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, "engine-image", imageUri)
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Condividi immagine"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Nessuna app disponibile per la condivisione", Toast.LENGTH_SHORT).show()
        }
    }
}
