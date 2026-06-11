package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdBanner(
    modifier: Modifier = Modifier
) {
    val adHtml = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            body {
              margin: 0;
              padding: 0;
              display: flex;
              justify-content: center;
              align-items: center;
              height: 100vh;
              background-color: transparent;
              overflow: hidden;
            }
            #ad-container {
              width: 320px;
              height: 50px;
              display: flex;
              justify-content: center;
              align-items: center;
            }
          </style>
        </head>
        <body>
          <div id="ad-container">
            <script type="text/javascript">
              atOptions = {
                'key' : '805a59f0dc6fffbf1db27abff0d7ff60',
                'format' : 'iframe',
                'height' : 50,
                'width' : 320,
                'params' : {}
              };
            </script>
            <script type="text/javascript" src="https://www.highperformanceformat.com/805a59f0dc6fffbf1db27abff0d7ff60/invoke.js"></script>
          </div>
        </body>
        </html>
    """.trimIndent()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle sponsored label
        Text(
            text = "SPONSORED PROMOTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Web view card container mapping the requested 320x50 dimensions
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        
                        // Set transparent background for native integrations
                        setBackgroundColor(0)
                        
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                // Let links load inside external browser rather than hijacking our app view
                                if (url != null) {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                return false
                            }
                        }
                        
                        loadDataWithBaseURL("https://www.highperformanceformat.com", adHtml, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
