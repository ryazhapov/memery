package com.example.fragmentslab

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.example.fragmentslab.api.RetrofitInstance.api
import com.example.fragmentslab.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import retrofit2.awaitResponse

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            GlobalScope.launch(Dispatchers.IO) {

                val response = try {
                    api.getPost().awaitResponse()
                } catch (e: Exception) {
                    Looper.prepare()
                    val toast = Toast.makeText(this@MainActivity, "NO INTERNET CONNECTION\nCannot connect to API", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER,0,0)
                    toast.show()
                    Looper.loop()
                    Log.e(TAG, "Connection error : No internet connection")
                    return@launch
                }
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    withContext(Dispatchers.Main) {
                        val fragment = MainFragment.newInstance(
                            gifUrl = data.gifURL.replace("http","https"),
                            desc = data.description,
                            currentIndex = 0
                        )
                        supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                                R.anim.slide_out_right, R.anim.slide_in_right)
                            .replace(R.id.fragmentContainerView, fragment)
                            .commit()
                    }
                }
            }
        }
    }

    fun getCurrentIndex(): Int {
        return supportFragmentManager.backStackEntryCount + 1
    }
}