package com.four.yy.vpn.base

import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.four.yy.vpn.utils.ViewBindingUtil
import com.gyf.immersionbar.ImmersionBar


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var binding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        ImmersionBar.with(this).init()
        layoutInflater
        binding = ViewBindingUtil.create(javaClass, layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


    }


    protected open fun jumpActivityFinish(clazz: Class<*>?) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) {
            startActivity(Intent(this, clazz))
            finish()
        }
    }

    protected open fun jumpActivity(clazz: Class<*>?) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) {
            startActivity(Intent(this, clazz))
        }
    }

    protected open fun showDialogByActivity(
        content: String,
        sure: String,
        cancel: Boolean = true,
        listener: OnClickListener?
    ) {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage(content)
            .setCancelable(cancel)
            .setPositiveButton(sure, listener)
            .create()
        alertDialog.show()
    }
}