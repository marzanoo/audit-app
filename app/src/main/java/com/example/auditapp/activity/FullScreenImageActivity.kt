package com.example.auditapp.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.auditapp.R

class FullScreenImageActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        val imageView = findViewById<ImageView>(R.id.fullScreenImageView)
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(imageView)
        }

        // Close activity when clicking the image
        imageView.setOnClickListener {
            finish()
        }
    }
}