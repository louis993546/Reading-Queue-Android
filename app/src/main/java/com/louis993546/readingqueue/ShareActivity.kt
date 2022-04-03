package com.louis993546.readingqueue

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.louis993546.readingqueue.ui.theme.ReadingQueueTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag("test").d(intent.action ?: "no action")
        val receivedText = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        setContent {
            ReadingQueueTheme {
                Text("URL received: $receivedText")
                LaunchedEffect(key1 = "what") {
                    delay(3000)
                    this@ShareActivity.finish()
                }
            }
        }
    }
}