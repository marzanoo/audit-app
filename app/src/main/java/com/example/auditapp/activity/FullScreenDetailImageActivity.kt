package com.example.auditapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.auditapp.databinding.ActivityFullScreenDetailImageBinding

class FullScreenDetailImageActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

    private lateinit var binding: ActivityFullScreenDetailImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenDetailImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.fullScreenDetailImageView)
        }

        // Close activity when clicking the image
        binding.fullScreenDetailImageView.setOnClickListener {
            finish()
        }
    }
}