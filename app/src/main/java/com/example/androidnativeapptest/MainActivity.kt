package com.example.androidnativeapptest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var urlInput: EditText
    private lateinit var loadButton: Button
    private lateinit var setHomeButton: Button
    private lateinit var backButton: Button
    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences
    
    private companion object {
        const val PREFS_NAME = "WebViewPrefs"
        const val HOME_URL_KEY = "home_url"
        const val DEFAULT_URL = "https://www.google.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize views
        urlInput = findViewById(R.id.url_input)
        loadButton = findViewById(R.id.load_button)
        setHomeButton = findViewById(R.id.set_home_button)
        backButton = findViewById(R.id.back_button)
        webView = findViewById(R.id.web_view)

        // Configure WebView
        setupWebView()

        // Set button click listeners
        loadButton.setOnClickListener {
            loadUrl()
        }
        
        setHomeButton.setOnClickListener {
            setHomeUrl()
        }

        // Set back button click listener
        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        // Set Enter key listener for URL input
        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl()
                true
            } else {
                false
            }
        }

        // Load home URL or default URL
        loadHomeUrl()
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = CustomWebViewClient()
    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            
            // Handle custom URL schemes (non-http/https)
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return handleCustomUrlScheme(url)
            }
            
            // Let WebView handle http/https URLs normally
            return false
        }
        
        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url == null) return false
            
            // Handle custom URL schemes (non-http/https)
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return handleCustomUrlScheme(url)
            }
            
            // Let WebView handle http/https URLs normally
            return false
        }
        
        private fun handleCustomUrlScheme(url: String): Boolean {
            return try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                // Check if there's an app that can handle this intent
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // 何もしない（エラーを無視）
                }
                true
            } catch (e: Exception) {
                // Error creating or launching intent
                Toast.makeText(this@MainActivity, 
                    "Error handling link: $url", 
                    Toast.LENGTH_LONG).show()
                true
            }
        }
    }

    private fun loadUrl() {
        val url = urlInput.text.toString().trim()
        if (url.isNotEmpty()) {
            val finalUrl = if (isUrl(url)) {
                // It looks like a URL, handle as before
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else {
                    url
                }
            } else {
                // It doesn't look like a URL, perform Google search
                val encodedQuery = URLEncoder.encode(url, "UTF-8")
                "https://www.google.com/search?q=$encodedQuery"
            }
            webView.loadUrl(finalUrl)
        }
    }
    
    private fun isUrl(input: String): Boolean {
        // If it contains spaces, it's likely a search query
        if (input.contains(" ")) {
            return false
        }
        
        // If it already starts with http:// or https://, it's a URL
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return true
        }
        
        // If it contains a dot and doesn't look like a search query, treat as URL
        // Simple heuristic: if it has a dot and no spaces, likely a domain
        if (input.contains(".")) {
            // Additional check: if it's just numbers and dots, might be an IP
            // If it contains common TLD patterns or looks domain-like, treat as URL
            return true
        }
        
        // Special cases like localhost
        if (input.equals("localhost", ignoreCase = true) || input.startsWith("localhost:")) {
            return true
        }
        
        // If it's just a single word without dots, treat as search query
        return false
    }
    
    private fun setHomeUrl() {
        val currentUrl = webView.url
        if (currentUrl != null) {
            sharedPreferences.edit()
                .putString(HOME_URL_KEY, currentUrl)
                .apply()
        }
    }
    
    private fun loadHomeUrl() {
        val homeUrl = sharedPreferences.getString(HOME_URL_KEY, null)
        if (homeUrl != null) {
            urlInput.setText(homeUrl)
            webView.loadUrl(homeUrl)
        } else {
            urlInput.setText(DEFAULT_URL)
            webView.loadUrl(DEFAULT_URL)
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