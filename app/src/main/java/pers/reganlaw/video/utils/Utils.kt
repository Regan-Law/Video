package pers.reganlaw.video.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import com.hw.videoprocessor.VideoProcessor
import io.microshow.rxffmpeg.RxFFmpegInvoke
import io.microshow.rxffmpeg.RxFFmpegSubscriber
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pers.reganlaw.video.CameraActivity
import pers.reganlaw.video.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


object Utils {
	private const val TAG = "VideoTest"

	private val utilsCoroutineScope = CoroutineScope(Dispatchers.Main)

	// 将videoCompression函数提取到Utils中
	fun videoCompression(context: Context, uri: Uri, binding: ActivityMainBinding) {
		val uriList = listOf(uri)
		binding.progress.bringToFront()
		binding.progress.visibility = View.VISIBLE

		val videoName = SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.CHINA
		).format(System.currentTimeMillis()) + "compressed"

		val config = Configuration(
			videoNames = listOf(videoName),
			quality = VideoQuality.MEDIUM,
			isMinBitrateCheckEnabled = true,
			videoWidth = 360.0,
			videoHeight = 720.0
		)

		// 使用一个协程作用域来执行异步操作
		CoroutineScope(Dispatchers.IO).launch {
			Log.d(CameraActivity.TAG, "$uri {uri}")
			VideoCompressor.start(
				context = context.applicationContext,
				uris = uriList,
				sharedStorageConfiguration = SharedStorageConfiguration(
					saveAt = SaveLocation.movies,
					subFolderName = "video"
				),
				configureWith = config,
				listener = object : CompressionListener {
					override fun onProgress(index: Int, percent: Float) {
						if (percent <= 100) {
							updateUIFromBackgroundThread(context, binding, percent)
						}
					}

					override fun onStart(index: Int) {}

					override fun onSuccess(index: Int, size: Long, path: String?) {
						updateUIFromBackgroundThread(context, binding, null)
					}

					override fun onCancelled(index: Int) {
						updateUIFromBackgroundThread(context, binding, null, true)
					}

					override fun onFailure(index: Int, failureMessage: String) {
						updateUIFromBackgroundThread(
							context,
							binding,
							null,
							false,
							failureMessage
						)
					}
				}
			)
		}
	}

	private fun updateUIFromBackgroundThread(
		context: Context,
		binding: ActivityMainBinding,
		percent: Float? = null,
		cancelled: Boolean = false,
		failureMessage: String? = null
	) {
		// 检查context是否是Activity的实例，如果是，则使用runOnUiThread
		if (context is Activity) {
			context.runOnUiThread {
				when {
					percent != null -> {
						Log.d(CameraActivity.TAG, percent.toString())
						if (percent < 1) {
							binding.progress.visibility = VISIBLE
							binding.progress.progress = ((percent - 0.5) * 200).toInt()
						} else {
							binding.progress.visibility = GONE
						}
					}

					cancelled -> {
						Toast.makeText(context, "Compression cancelled", Toast.LENGTH_SHORT).show()
						binding.progress.visibility = GONE
					}

					failureMessage != null -> {
						Toast.makeText(
							context,
							"Video compress failed: $failureMessage",
							Toast.LENGTH_SHORT
						).show()
						binding.progress.visibility = GONE
					}
				}
			}
		} else {
			// 如果context不是一个Activity，你可以使用Handler来更新UI
			Handler(Looper.getMainLooper()).post {
				// 这里写UI更新的代码
			}
		}
	}


	fun videoCompressionRx(
		lifecycleScope: LifecycleCoroutineScope,
		context: Context,
		uri: Uri,
		binding: ActivityMainBinding,
		onCompletion: () -> Unit
	) {
		lifecycleScope.launch(Dispatchers.IO) {
			try {
				var inputPath = getFilePathFromContentUri(uri, context.contentResolver)
				Log.d(TAG, "inputPath${inputPath}")
//				inputPath = "/storage/emulated/0/Movies/video/5.mp4"
				Log.d(TAG, "inputPaths${inputPath}")
				val outputPath = getOutputPath(context)

				// 构建FFmpeg命令
				val command = arrayOf(
					"-y", // 覆盖输出文件
					"-i", inputPath, // 输入文件
					"-vcodec", "libx264", // 使用H.264编码器
					"-crf", "20", // 设置CRF值，数值越小质量越高
					"-preset", "medium", // 设置预设速度与质量的平衡
					outputPath // 输出文件
				)

				// 执行FFmpeg命令
				RxFFmpegInvoke.getInstance()
					.runCommandRxJava(command)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(object : RxFFmpegSubscriber() {
						override fun onFinish() {
							Log.d(TAG, "Video compression finished.")
							binding.progress.visibility = GONE
							context.toast("Video compressed successfully.")
							onCompletion.invoke()

						}

						override fun onProgress(progress: Int, time: Long) {
							Log.d(TAG, "loading $progress")
							binding.progress.visibility = View.VISIBLE
							binding.progress.progress = progress
						}

						override fun onCancel() {
							Log.d(TAG, "Video compression cancelled.")

							context.toast("Compression cancelled.")

						}

						override fun onError(message: String) {
							Log.e(TAG, "Error during video compression: $message")
							context.toast("Video compression failed: $message")
						}
					})
			} catch (e: Exception) {
				Log.e(TAG, "Error getting video file path: ${e.message}")
			}
		}
	}

	fun videoCompressionVP(
		lifecycleScope: LifecycleCoroutineScope,
		context: Context,
		uri: Uri,
		binding: ActivityMainBinding
	) {
		lifecycleScope.launch(Dispatchers.IO) {
			try {
				var inputPath = getFilePathFromContentUri(uri, context.contentResolver)
				inputPath = "/storage/emulated/0/Movies/video/2024-07-21 09_37_57.mp4"
				val outputPath = getOutputPath(context)
				val retriever = MediaMetadataRetriever()
				retriever.setDataSource(context, uri)
				val originWidth =
					retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
						.toInt()
				val originHeight =
					retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
						.toInt()
				val bitrate =
					retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)!!
						.toInt()
				VideoProcessor.processor(context)
					.input(inputPath)
					.output(outputPath)
					.frameRate(18)
//					.bitrate(bitrate / 2)
					.progressListener { progress ->
						Log.d(TAG, "loading $progress")
						updateUIFromBackgroundThread(context, binding, progress)
					}
					.process()
			} catch (e: Exception) {
				Log.e(TAG, "Error getting video file path: ${e.message}")
			}
		}

	}

	private fun getOutputPath(context: Context): String {
		val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
		val fileName = SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss",
			Locale.getDefault()
		).format(System.currentTimeMillis()) + ".mp4"
		return File(outputDirectory, fileName).absolutePath
	}

	//Uri转文件路径
	private fun getFilePathFromContentUri(
		uri: Uri?,
		contentResolver: ContentResolver
	): String {
		val filePath: String
		val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
		val cursor = contentResolver.query(uri!!, filePathColumn, null, null, null)
		cursor!!.moveToFirst()
		val columnIndex = cursor.getColumnIndex(filePathColumn[0])
		filePath = cursor.getString(columnIndex)
		cursor.close()
		return filePath
	}

	fun getVideoPreviewBitmap(context: Context, videoUri: Uri): Bitmap? {
		try {
			// 初始化MediaMetadataRetriever对象
			val retriever = MediaMetadataRetriever()
			// 直接使用Uri设置数据源，避免使用FileDescriptor
			retriever.setDataSource(context, videoUri)
			// 提取视频的某一帧作为封面，这里提取视频的中间帧作为示例
			// 根据需要调整timeUs参数来获取不同时间点的帧
			val timeUs =
				retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
					?.div(2) ?: 0
			val bitmap =
				retriever.getFrameAtTime(timeUs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
			// 关闭MediaMetadataRetriever并释放资源
			retriever.release()
			return bitmap
		} catch (e: Exception) {
			Log.e("VideoUtils", "Error getting video preview bitmap: ${e.message}")
			return null
		}
	}

	// Utility function to show Toast messages
	fun Context.toast(message: String) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show()
	}
}
