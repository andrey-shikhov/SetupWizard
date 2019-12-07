package me.shikhov.setupwizardapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.shikhov.wlog.Log

private const val TAG = "wizard-activity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.get(TAG).a("onCreate").r()

        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .add(R.id.activity_root, SetupFragment())
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.get(TAG).a("onResume").r()
    }

    override fun onPause() {
        super.onPause()
        Log.get(TAG).a("onPause").r()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.get(TAG).a("onDestroy").r()
    }
}
