package pers.reganlaw.video

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pers.reganlaw.video.databinding.ActivityMainBinding
import pers.reganlaw.video.utils.Utils.getVideoPreviewBitmap
import pers.reganlaw.video.utils.Utils.toast
import pers.reganlaw.video.utils.Utils.videoCompressionVP
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private var videoUri: Uri? = null
	private var compressed = false
//	private val activityResultLauncher: ActivityResultLauncher<Intent> =
//		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
//			if (activityResult.resultCode == Activity.RESULT_OK) {
//				val uri = Uri.parse(intent.getStringExtra("preparedToCompress"))
//				videoCompression(uri)
//			}
//		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.btAdd.setOnClickListener {
			val intent = Intent(this, CameraActivity::class.java)
			startActivity(intent)
			onStop()
			Log.d(TAG, "stop")
		}
//		val videoUriString = intent.getStringExtra("COMPRESSED_VIDEO_URI")
//		videoUri = Uri.parse(videoUriString ?: return)
//		if (videoUri != null) {
//			val thumbnailBitmap = generateVideoThumbnail(videoUri!!)
//			thumbnailBitmap.let {
//				binding.compressed.setImageBitmap(it)
//			}
//			binding.deleteButton.setOnClickListener {
//				deleteCompressedVideo(videoUri!!)
//			}
//		}

		isCompress(intent)
		isCompressed()
//		compress = intent.getBooleanExtra("isCompress", false)
//		if (compress) {
//			val uri = Uri.parse(intent.getStringExtra("preparedToCompress"))
//			videoCompression(uri)
//		}
	}

	private fun isCompressed() {
		if (compressed) {
			lifecycleScope.launch(Dispatchers.Main) {
				val thumbnailBitmap = getVideoPreviewBitmap(this@MainActivity, videoUri!!)
				thumbnailBitmap.let {
					binding.compressed.setImageBitmap(it)
				}
			}

		}
	}

	private fun isCompress(intent: Intent) {
		if (intent.getBooleanExtra("isCompress", false)) {
			val uri = Uri.parse(intent.getStringExtra("preparedToCompress"))
//			videoCompression(uri)
			videoCompressionVP(this.lifecycleScope, this, uri, binding)
//			videoCompressionRx(this.lifecycleScope, this, uri, binding) {
//				if (compressed) {
//					lifecycleScope.launch(Dispatchers.Main) {
//						val thumbnailBitmap = getVideoPreviewBitmap(this@MainActivity, videoUri!!)
//						thumbnailBitmap.let {
//							binding.compressed.setImageBitmap(it)
//						}
//					}
//				}
//			}
		}
	}

	private fun videoCompression(uri: Uri) {
		val uriList = listOf(uri)
		binding.progress.bringToFront()
		binding.progress.visibility = VISIBLE
		val videoName = SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.CHINA
		).format(System.currentTimeMillis()) + "compressed"
//		val outputDirectory = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//		val videoPath = getVideoPathFromUri(uri)
//		var media = MediaMetadataRetriever()
//		media.setDataSource(videoPath)
//		val extractMetadata =
//			media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
//		val bitRate = ((extractMetadata?.toInt() ?: 0) * 0.4).toInt()
//		media.release()
//		val fileName = "$videoName.mp4"
//		val outputFile = File(outputDirectory, fileName).absolutePath
		val config = Configuration(
			videoNames = listOf(videoName),
			quality = VideoQuality.MEDIUM,

			isMinBitrateCheckEnabled = true,
			videoWidth = 360.00,
			videoHeight = 720.00
		)
		lifecycleScope.launch {
			Log.d(CameraActivity.TAG, "$uri {uri}")
			VideoCompressor.start(
				context = applicationContext,
				uris = uriList,
				sharedStorageConfiguration = SharedStorageConfiguration(
					saveAt = SaveLocation.movies,
					subFolderName = "video"
				),
				configureWith = config,
				listener = object : CompressionListener {
					override fun onProgress(index: Int, percent: Float) {
						if (percent <= 100) {
							runOnUiThread {
								Log.d(CameraActivity.TAG, percent.toString())
								binding.progress.progress = (percent * 100).toInt()
							}
						}
					}

					override fun onStart(index: Int) {

					}

					override fun onSuccess(index: Int, size: Long, path: String?) {
						runOnUiThread {
							toast("Video compressed success")
							binding.progress.visibility = GONE
							videoUri = path?.toUri()
							compressed = true
						}
					}

					override fun onCancelled(index: Int) {
						toast("Compression cancelled")
						binding.progress.visibility = GONE
					}

					override fun onFailure(index: Int, failureMessage: String) {
						toast("Video compress failed")
						binding.progress.visibility = GONE
					}
				}
			)
		}
	}

//	private fun generateVideoThumbnail(videoUri: Uri): Bitmap {
//		val thumbnailSize = Point()
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//			val windowMetrics = windowManager.currentWindowMetrics
//			val insets = windowMetrics.windowInsets
//				.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
//			val bounds = windowMetrics.bounds
//			thumbnailSize.set(
//				bounds.width() - insets.left - insets.right,
//				bounds.height() - insets.top - insets.bottom
//			)
//		} else {
//			// For devices running an SDK before R
//			val display = windowManager.defaultDisplay
//			display?.getSize(thumbnailSize)
//		}
//		val thumbWidth = thumbnailSize.x / 4
//		val thumbHeight = thumbnailSize.y / 4
//		return contentResolver.loadThumbnail(videoUri, Size(thumbWidth, thumbHeight), null)
//	}


	companion object {
		const val TAG = "Video"
	}

}