package com.example.androidnativeapptest

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var urlInput: EditText
    private lateinit var loadButton: Button
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        urlInput = findViewById(R.id.url_input)
        loadButton = findViewById(R.id.load_button)
        webView = findViewById(R.id.web_view)

        // Configure WebView
        setupWebView()

        // Set button click listener
        loadButton.setOnClickListener {
            loadUrl()
        }

        // Load default URL
        loadUrl()
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
    }

    private fun loadUrl() {
        val url = urlInput.text.toString().trim()
        if (url.isNotEmpty()) {
            val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            webView.loadUrl(finalUrl)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}