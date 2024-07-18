package pers.reganlaw.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pers.reganlaw.video.databinding.ActivityVideoPreviewBinding

class VideoPreviewActivity : AppCompatActivity() {
	private lateinit var binding: ActivityVideoPreviewBinding
	private var videoUri: Uri? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityVideoPreviewBinding.inflate(layoutInflater)
		setContentView(binding.root)
		videoUri = Uri.parse(intent.getStringExtra("preview") ?: return)
		videoPreview()
		binding.btConfirm.setOnClickListener {
			if (videoUri != null) {
				val intentCompress = Intent(this, MainActivity::class.java)
				setResult(Activity.RESULT_OK, intentCompress)
				intentCompress.putExtra("isCompress", true)
				intentCompress.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
				intentCompress.putExtra("preparedToCompress", videoUri.toString())
				startActivity(intentCompress)
				binding.preview.stopPlayback()
//				val contentResolver = contentResolver
//				val movieUri = getMovieFileUri(contentResolver)
//				val test5 = File("/storage/emulated/0/Movies/video/5.mp4").toUri()
//				Log.d(TAG, test5.toString())
//				val test10 = Uri.parse("content://media/external/video/media/10")
//				videoCompression(videoUri!!)
//				Log.d(TAG, movieUri.toString())
//				videoCompression(videoUri!!)
			} else {
				toast("Uri is null")
			}

		}
	}


	private fun videoPreview() {
		if (videoUri != null) {
			binding.preview.setVideoURI(videoUri)
			binding.preview.requestFocus()
			binding.preview.setOnPreparedListener {
				binding.preview.start()
			}
		} else {
			toast("Nothing to play")
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		binding.preview.stopPlayback()
	}

	private fun toast(m: String) {
		Toast.makeText(this, m, Toast.LENGTH_LONG).show()
	}
}