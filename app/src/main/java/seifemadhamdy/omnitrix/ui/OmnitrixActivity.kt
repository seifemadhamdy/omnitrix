package seifemadhamdy.omnitrix.ui

import android.app.Activity
import android.os.Bundle
import seifemadhamdy.omnitrix.databinding.ActivityOmnitrixBinding

class OmnitrixActivity : Activity() {

    private lateinit var binding: ActivityOmnitrixBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOmnitrixBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.omnitrixView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.omnitrixView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.omnitrixView.destroy()
    }
}