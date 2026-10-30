# Jetpack Glance Widgets & ActionCallbacks
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>(...);
}
-keep class * extends androidx.glance.appwidget.GlanceAppWidget {
    public <init>(...);
}
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver {
    public <init>(...);
}

# Keep Glance WidgetState kotlinx.serialization classes
-keep class com.ramitsuri.podcasts.widget.data.** { *; }
-keepclassmembers class com.ramitsuri.podcasts.widget.data.** { *; }

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**
