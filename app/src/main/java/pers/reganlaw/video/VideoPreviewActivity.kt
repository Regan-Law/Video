package pers.reganlaw.video

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
				intentCompress.putExtra("uri", videoUri.toString())
				startActivity(intentCompress)
				finish()
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