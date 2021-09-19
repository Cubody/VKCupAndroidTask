package com.uralitsports.vkcup

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Button
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.auth.VKScope

class LoginActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VK.isLoggedIn()) {
            UserActivity.startFrom(this)
            finish()
            return
        }
        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.PHOTOS))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                UserActivity.startFrom(this@LoginActivity)
                finish()
            }

            override fun onLoginFailed(authException: VKAuthException) {
                if (!authException.isCanceled) {
                    val descriptionResource =
                        if (authException.webViewError == WebViewClient.ERROR_HOST_LOOKUP) R.string.message_connection_error
                        else R.string.message_unknown_error
                    AlertDialog.Builder(this@LoginActivity)
                        .setMessage(descriptionResource)
                        .setPositiveButton(R.string.vk_retry) { _, _ ->
                            VK.login(
                                this@LoginActivity,
                                arrayListOf(VKScope.WALL, VKScope.PHOTOS)
                            )
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        if (!VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}