package com.sayeedjoy.linkarena.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun BannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp
    val adSize = remember(adWidth) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        factory = {
            AdView(it).apply {
                setAdSize(adSize)
                adUnitId = AdUnitIds.banner(it)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            if (adView.adSize != adSize) {
                adView.setAdSize(adSize)
                adView.loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(context) {
        AdLoader.Builder(context, AdUnitIds.native(context))
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { createNativeAdView(it) },
            update = { adView -> bindNativeAd(adView, ad) }
        )
    }
}

@Composable
fun rememberActivity(): Activity? {
    var context = LocalContext.current
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

private fun createNativeAdView(context: Context): NativeAdView {
    val root = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(24, 20, 24, 20)
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val label = TextView(context).apply {
        text = "Sponsored"
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
    }
    val headline = TextView(context).apply {
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
    }
    val body = TextView(context).apply {
        textSize = 14f
        maxLines = 2
    }
    val callToAction = Button(context)

    container.addView(label)
    container.addView(headline)
    container.addView(body)
    container.addView(callToAction)
    root.addView(container)

    root.headlineView = headline
    root.bodyView = body
    root.callToActionView = callToAction

    return root
}

private fun bindNativeAd(adView: NativeAdView, nativeAd: NativeAd) {
    (adView.headlineView as TextView).text = nativeAd.headline
    (adView.bodyView as TextView).apply {
        text = nativeAd.body.orEmpty()
        visibility = if (nativeAd.body == null) android.view.View.GONE else android.view.View.VISIBLE
    }
    (adView.callToActionView as Button).apply {
        text = nativeAd.callToAction.orEmpty()
        visibility = if (nativeAd.callToAction == null) android.view.View.GONE else android.view.View.VISIBLE
    }
    adView.setNativeAd(nativeAd)
}
