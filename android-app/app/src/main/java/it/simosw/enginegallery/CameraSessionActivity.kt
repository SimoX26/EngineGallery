package it.simosw.enginegallery

import android.content.ClipData
import android.content.Intent
import android.content.res.ColorStateList
import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class CameraSessionActivity : AppCompatActivity() {

    private lateinit var counterText: TextView
    private lateinit var thumbnailStrip: LinearLayout
    private lateinit var thumbnailScroll: HorizontalScrollView
    private lateinit var captureButton: Button
    private lateinit var doneButton: Button

    private val capturedUris = mutableListOf<Uri>()
    private var pendingCaptureUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                pendingCaptureUri = null
                return@registerForActivityResult
            }

            val capturedUri = pendingCaptureUri
            pendingCaptureUri = null
            if (capturedUri == null) {
                return@registerForActivityResult
            }

            capturedUris.add(capturedUri)
            renderCapturedPreview()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_session)
        supportActionBar?.title = getString(R.string.camera_session_title)

        counterText = findViewById(R.id.cameraSessionCounter)
        thumbnailStrip = findViewById(R.id.cameraSessionThumbnailStrip)
        thumbnailScroll = findViewById(R.id.cameraSessionThumbnailScroll)
        captureButton = findViewById(R.id.cameraSessionCaptureButton)
        doneButton = findViewById(R.id.cameraSessionDoneButton)

        captureButton.setOnClickListener {
            launchCameraCapture()
        }
        doneButton.setOnClickListener {
            finishWithResult()
        }

        renderCapturedPreview()
    }

    private fun launchCameraCapture() {
        val imageFile = createTempImageFile() ?: return
        val imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (cameraIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.file_error, Toast.LENGTH_SHORT).show()
            return
        }

        pendingCaptureUri = imageUri
        cameraLauncher.launch(cameraIntent)
    }

    private fun finishWithResult() {
        if (capturedUris.isEmpty()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val resultIntent = Intent()
        val first = capturedUris.first()
        resultIntent.data = first

        val clipData = ClipData.newUri(contentResolver, "captured-image-0", first)
        capturedUris.drop(1).forEachIndexed { index, uri ->
            clipData.addItem(ClipData.Item(uri))
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        resultIntent.clipData = clipData
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun renderCapturedPreview() {
        val count = capturedUris.size
        counterText.text = getString(R.string.camera_session_counter, count)

        thumbnailStrip.removeAllViews()
        if (count == 0) {
            thumbnailScroll.alpha = 0.6f
            return
        }

        thumbnailScroll.alpha = 1f
        val density = resources.displayMetrics.density
        val size = (72 * density).toInt()
        val margin = (8 * density).toInt()

        capturedUris.forEach { uri ->
            val thumb = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = margin
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                contentDescription = getString(R.string.camera_session_thumbnail_content_desc)
                setOnClickListener { showCapturedPreview(uri) }
            }
            thumbnailStrip.addView(thumb)
        }
    }

    private fun showCapturedPreview(uri: Uri) {
        val density = resources.displayMetrics.density
        val padding = (16 * density).toInt()
        val dialog = Dialog(this)
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.WHITE)
        }
        val title = TextView(this).apply {
            text = getString(R.string.camera_session_preview_title)
            textSize = 20f
            setTextColor(Color.BLACK)
        }
        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (resources.displayMetrics.heightPixels * 0.65f).toInt()
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setImageURI(uri)
        }
        val deleteButton = Button(this).apply {
            text = getString(R.string.camera_session_delete)
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(220, 38, 38))
            setOnClickListener {
                capturedUris.remove(uri)
                try { contentResolver.delete(uri, null, null) } catch (_: Exception) { }
                renderCapturedPreview()
                dialog.dismiss()
            }
        }
        panel.addView(title)
        panel.addView(image)
        panel.addView(deleteButton)
        dialog.setContentView(panel)
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.94f).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
}
