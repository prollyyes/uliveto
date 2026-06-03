# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep GeckoView
-keep class org.mozilla.geckoview.** { *; }

# GeckoView internal bundle used via reflection in PrefsApplier
-keep class org.mozilla.gecko.util.GeckoBundle { *; }
-keepclassmembers class org.mozilla.geckoview.GeckoRuntime {
    void setDefaultPrefs(org.mozilla.gecko.util.GeckoBundle);
}
