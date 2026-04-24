package com.sayeedjoy.linkarena.ads

import android.content.Context
import com.sayeedjoy.linkarena.R

object AdUnitIds {
    fun banner(context: Context): String = context.getString(R.string.admob_banner_ad_unit_id)
    fun interstitial(context: Context): String = context.getString(R.string.admob_interstitial_ad_unit_id)
    fun rewarded(context: Context): String = context.getString(R.string.admob_rewarded_ad_unit_id)
    fun appOpen(context: Context): String = context.getString(R.string.admob_app_open_ad_unit_id)
    fun native(context: Context): String = context.getString(R.string.admob_native_ad_unit_id)
}
