package com.four.yy.vpn.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.four.yy.vpn.base.BaseActivity
import com.four.yy.vpn.databinding.ActivityPrivacyPolicyBinding
import com.four.yy.vpn.utils.Constant

class PrivacyPolicyWebView : BaseActivity<ActivityPrivacyPolicyBinding>() {


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.idWebView.settings.javaScriptEnabled = true
        binding.idWebView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        binding.idWebView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                binding.title.text = title
            }
        }
        binding.idWebView.loadUrl(Constant.PrivacyPolicy)
        binding.idBack.setOnClickListener {
            finish()
        }
    }
}