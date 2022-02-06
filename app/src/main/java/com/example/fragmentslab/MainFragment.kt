package com.example.fragmentslab

import android.content.ContentValues
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.fragmentslab.R.id.*
import com.example.fragmentslab.api.RetrofitInstance
import com.example.fragmentslab.databinding.FragmentMainBinding
import com.example.fragmentslab.model.Post
import kotlinx.coroutines.*
import retrofit2.awaitResponse

private lateinit var binding: FragmentMainBinding
private var currentIndex = 0
private var enabledBack = false
var list: MutableList<Post> = mutableListOf()

@DelicateCoroutinesApi
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.description.text = getDesc()

        val circularProgressDrawable = activity?.let { CircularProgressDrawable(it) }
        if (circularProgressDrawable != null) {
            circularProgressDrawable.strokeWidth = 7f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
        }

        Glide.with(this)
            .asGif()
            .load(getGifUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(circularProgressDrawable)
            .error(R.drawable.ic_baseline_error_outline_24)
            .centerCrop()
            .into(binding.gifView)

        binding.buttonNext.setOnClickListener {
            next()
            this.view?.let { enableButton(it.findViewById(buttonBack)) }}

        binding.buttonBack.setOnClickListener {
            if (view?.findViewById<View?>(buttonBack)?.isEnabled!!) {
                back()
            }
        }
        if (enabledBack) {
            binding.buttonBack.isEnabled = true
            binding.buttonBack.isClickable = true
        } else {
            binding.buttonBack.isEnabled = false
            binding.buttonBack.isClickable = false
        }
        return binding.root
    }

    @DelicateCoroutinesApi
    private fun next() {
        enabledBack = true

        GlobalScope.launch(Dispatchers.IO) {

            if (currentIndex < list.size) {
                currentIndex++
                //println("next " + currentIndex)
                val data = list[currentIndex - 1]
                val fragment = newInstance(
                    gifUrl = data.gifURL.replace("http","https"),
                    desc = data.description,
                    currentIndex = (requireActivity() as MainActivity).getCurrentIndex() + 1
                )
                parentFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                        R.anim.slide_out_right, R.anim.slide_in_right)
                    .addToBackStack(null)
                    .replace(fragmentContainerView, fragment)
                    .commit()
            } else {
                    val response = try {
                        RetrofitInstance.api.getPost().awaitResponse()
                    } catch (e: Exception) {
                        Looper.prepare()
                        val toast = Toast.makeText(activity, "NO INTERNET CONNECTION\nCannot load next post", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER,0,0)
                        toast.show()
                        Looper.loop()
                        Log.e(ContentValues.TAG, "Connection error : No internet connection")
                        return@launch
                    }
                    if (response.isSuccessful) {
                        val data = response.body()!!
                        list.add(data)
                        withContext(Dispatchers.Main) {
                            val fragment = newInstance(
                                gifUrl = data.gifURL.replace("http","https"),
                                desc = data.description,
                                currentIndex = (requireActivity() as MainActivity).getCurrentIndex() + 1
                            )
                            parentFragmentManager
                                .beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                                    R.anim.slide_out_right, R.anim.slide_in_right)
                                .addToBackStack(null)
                                .replace(fragmentContainerView, fragment)
                                .commit()
                        }
                        currentIndex++
                        //println("next " + currentIndex)
                    } else {
                        requireParentFragment().activity
                    }
            }
        }
    }

    private fun back() {
        if (currentIndex == 1) {
            enabledBack = false
        }
        if (currentIndex == 0) {
            view?.let { disableButton(it.findViewById(buttonBack)) }
        } else {
            requireActivity().onBackPressed()
            currentIndex--
            //println("back " + currentIndex)
        }
    }

    private fun disableButton (button: Button) {
        button.isClickable = false
        button.isEnabled = false
    }

    private fun enableButton (button: Button) {
        button.isClickable = true
        button.isEnabled = true
    }

    private fun getGifUrl() : String? = requireArguments().getString(ARG_GIF_URL)

    private fun getDesc() : String? = requireArguments().getString(ARG_DESC)

    companion object {
        @JvmStatic
        private val ARG_GIF_URL = "ARG_GIF_URL"

        @JvmStatic
        private val ARG_DESC = "ARG_DESC"

        @JvmStatic
        private val ARG_CURRENT_INDEX = "ARG_CURRENT_INDEX"

        @JvmStatic
        fun newInstance(gifUrl: String, desc: String, currentIndex: Int): MainFragment {
            val args = Bundle().apply {
                putString(ARG_GIF_URL, gifUrl)
                putString(ARG_DESC, desc)
                putInt(ARG_CURRENT_INDEX, currentIndex)
            }
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
