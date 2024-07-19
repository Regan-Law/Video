package pers.reganlaw.video

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import pers.reganlaw.video.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity(), LifecycleOwner {
	private lateinit var binding: ActivityCameraBinding
	private var isRecording = false
	private lateinit var cameraProvider: ProcessCameraProvider
	private lateinit var preview: Preview
	private lateinit var recorder: Recorder
	private var recording: Recording? = null
	private var videoCapture: VideoCapture<Recorder>? = null
	private val handler = Handler(Looper.getMainLooper())
	private val updateTimer = object : Runnable {
		override fun run() {
			val currentTime = SystemClock.elapsedRealtime() - binding.time.base
			val timeString = currentTime.toFormattedTime()
			binding.time.text = timeString
			handler.postDelayed(this, 1000)
		}
	}

	private fun Long.toFormattedTime(): String {
		val second = ((this / 1000) % 60).toInt()
		val min = (this / (1000 * 60) % 60).toInt()
		val hour = ((this / (1000 * 60 * 60)) % 24).toInt()
		return if (hour > 0) String.format(
			Locale.CHINA,
			"%02d:%02d:%02d",
			hour,
			min,
			second
		) else String.format(Locale.CHINA, "%02d:%02d", min, second)
	}

	private val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
		)
	} else {
		arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCameraBinding.inflate(layoutInflater)
		enableEdgeToEdge()
		setContentView(binding.root)
		requestPermissions()
		binding.btRecord.setOnClickListener {
			if (!isRecording) startRecording() else stopRecording()
		}
	}

	private fun requestPermissions() {
		XXPermissions.with(this)
			.permission(permissionArray)
			.request(object : OnPermissionCallback {
				override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
					if (!allGranted) {
						toast("获取部分权限成功，但部分权限未正常授予")
						return
					}
					startCamera()
				}

				override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
					if (doNotAskAgain) {
						toast("请手动授予权限")
						XXPermissions.startPermissionActivity(this@CameraActivity, permissions)
					} else {
						toast("获取权限失败")
					}
				}
			})
	}

	private fun stopRecording() {
		isRecording = false
		binding.time.isGone = true
		binding.time.stop()
		handler.removeCallbacks(updateTimer)
	}

	private fun startRecording() {
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		val videoCapture = this.videoCapture ?: return
		binding.btRecord.isEnabled = false
		val outputFile = File(this.getExternalFilesDir(null), "${System.currentTimeMillis()}.mp4")
		Log.d(TAG, "FilePath: ${outputFile.absolutePath}")
		val outputOptions = FileOutputOptions.Builder(outputFile).build()
		val currRecording = recording
		if (currRecording != null) {
			currRecording.stop()
			stopRecording()
			recording = null
			return
		}
		startTimer()
		val fileName = SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.getDefault()
		).format(System.currentTimeMillis()) + ".mp4"
		val contentValues = ContentValues().apply {
			put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
			put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
			put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/video")
		}
		val mediaOutputOptions =
			MediaStoreOutputOptions
				.Builder(
					contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
				).setContentValues(contentValues)
				.build()
		recording = videoCapture.output
			.prepareRecording(this, mediaOutputOptions)
			.apply {
				if (ActivityCompat.checkSelfPermission(
						this@CameraActivity,
						Manifest.permission.RECORD_AUDIO
					) != PackageManager.PERMISSION_GRANTED
				) {
					withAudioEnabled()
				}
			}
			.start(ContextCompat.getMainExecutor(this)) { recorderEvent ->
				when (recorderEvent) {
					is VideoRecordEvent.Start -> {
						binding.btRecord.setImageResource(R.drawable.stop)
						binding.btRecord.isEnabled = true
					}

					is VideoRecordEvent.Finalize -> {
						if (!recorderEvent.hasError()) {
							val videoUri = recorderEvent.outputResults.outputUri
							val intent = Intent(this, VideoPreviewActivity::class.java)
							intent.putExtra("preview", videoUri.toString())
							startActivity(intent)
							finish()
							Log.d(TAG, "VideoUri${videoUri}")
						} else {
							recording?.close()
							recording = null
							Log.e(TAG, "Video capture ends with error ${recorderEvent.error}")
						}
						binding.btRecord.setImageResource(R.drawable.start)
						binding.btRecord.isEnabled = true
					}
				}
			}
	}

//	override fun onPause() {
//		super.onPause()
//		recording?.stop()
//		startRecording()
//	}

	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
		cameraProviderFuture.addListener(
			{
				cameraProvider = cameraProviderFuture.get()
				bindCameraUseCases()
			}, ContextCompat.getMainExecutor(this)
		)
	}

	private fun bindCameraUseCases() {
		val cameraSelector = CameraSelector.Builder()
			.requireLensFacing(CameraSelector.LENS_FACING_BACK)
			.build()
		preview = Preview.Builder().build().also {
			it.setSurfaceProvider(binding.preview.surfaceProvider)
		}
		recorder = Recorder.Builder()
			.setQualitySelector(QualitySelector.from(Quality.HD))
			.build()
		
		videoCapture = VideoCapture.withOutput(recorder)

		try {
			cameraProvider.unbindAll()
			cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
		} catch (e: Exception) {
			Log.e(TAG, "Use case bind failed", e)
		}
	}

	private fun startTimer() {
		binding.time.isVisible = true
		binding.time.base = SystemClock.elapsedRealtime()
		binding.time.start()
		handler.post(updateTimer)
	}

	private fun toast(m: String) {
		Toast.makeText(this, m, Toast.LENGTH_LONG).show()
	}

	companion object {
		const val TAG = "VideoTest"
	}
}