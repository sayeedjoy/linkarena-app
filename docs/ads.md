# Google Ads / AdMob Integration

This app did not have Google Ads implemented before this change. I added a test-mode AdMob integration across the Android app.

## What Was Added

Implemented Google Mobile Ads SDK support:

- Added `com.google.android.gms:play-services-ads` to the Gradle version catalog and app dependencies.
- Added the AdMob application ID metadata to `AndroidManifest.xml`.
- Initialized `MobileAds` once at app startup in `LinkArenaApplication`.
- Added reusable ad utilities under `app/src/main/java/com/sayeedjoy/linkarena/ads`.

## Ad Formats Wired

- Banner ads: adaptive banner component via `BannerAd`.
- Native ads: lightweight native ad card via `NativeAdCard`.
- Interstitial ads: loaded and shown through `InterstitialAdManager`.
- Rewarded ads: loaded and shown through `RewardedAdManager`.
- App open ads: managed through `AppOpenAdManager` on activity resume.

## Current Placements

- Main logged-in bottom area: adaptive banner above the bottom navigation.
- Home bookmark list: native ad item at the top of the list.
- Group detail bookmark list: native ad item at the top of the list.
- Bookmark detail navigation: interstitial shown before opening bookmark detail from Home.
- Group detail navigation: interstitial shown before opening a group from Collections.
- Settings screen: rewarded ad button labeled `Watch Rewarded Ad`.
- App foreground/open: app-open ad manager is registered at app launch.

## Files Changed

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/com/sayeedjoy/linkarena/LinkArenaApplication.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ads/AdUnitIds.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ads/AdComposables.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ads/InterstitialAdManager.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ads/RewardedAdManager.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ads/AppOpenAdManager.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ui/navigation/MainNavGraph.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ui/home/HomeScreen.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ui/groups/GroupDetailScreen.kt`
- `app/src/main/java/com/sayeedjoy/linkarena/ui/settings/SettingsScreen.kt`

## Test IDs In Use

The app currently uses Google's official AdMob test IDs in `app/src/main/res/values/strings.xml`:

- App ID: `ca-app-pub-3940256099942544~3347511713`
- Adaptive banner: `ca-app-pub-3940256099942544/9214589741`
- Interstitial: `ca-app-pub-3940256099942544/1033173712`
- Rewarded: `ca-app-pub-3940256099942544/5224354917`
- App open: `ca-app-pub-3940256099942544/9257395921`
- Native: `ca-app-pub-3940256099942544/2247696110`

These are safe for development. Do not ship production builds with these IDs.

## Production Steps

Before publishing:

- Create the Android app in AdMob.
- Replace `admob_app_id` with the real AdMob app ID.
- Replace each `admob_*_ad_unit_id` string with real ad unit IDs.
- Keep test ads enabled on development devices.
- Review AdMob policy for interstitial frequency and rewarded ad disclosure.

## Verification Status

I attempted to run `./gradlew.bat assembleDebug`, but the local shell did not have `JAVA_HOME` configured. After finding Android Studio's bundled JDK, Gradle then needed to download `gradle-9.3.1-bin.zip`, which was blocked by the sandbox/network approval flow before completion.

Build verification is still pending.
