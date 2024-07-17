package pers.reganlaw.video

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pers.reganlaw.video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private var videoUri: Uri? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.btAdd.setOnClickListener {
			val intent = Intent(this, CameraActivity::class.java)
			startActivity(intent)
		}
		val videoUriString = intent.getStringExtra("uri")
		videoUri = Uri.parse(videoUriString ?: return)
		
	}

	companion object {
		const val TAG = "Video"
	}

}