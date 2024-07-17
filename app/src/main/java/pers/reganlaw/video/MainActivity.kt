package pers.reganlaw.video

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pers.reganlaw.video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.btAdd.setOnClickListener {
			val intent = Intent(this, CameraActivity::class.java)
			startActivity(intent)
		}
	}

	companion object {
		const val TAG = "Video"
	}

}