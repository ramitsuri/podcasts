# Jetpack Glance Widgets & Receivers
-keep class * extends androidx.glance.appwidget.GlanceAppWidget {
    public <init>(...);
}
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver {
    public <init>(...);
}

# Jetpack Glance ActionCallbacks
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>(...);
}

# Jetpack Glance State Definitions
-keep class * implements androidx.glance.state.GlanceStateDefinition {
    public *;
}

# Glance Widget DataStore State & kotlinx.serialization
-keep class com.ramitsuri.podcasts.widget.data.** { *; }
-keepclassmembers class com.ramitsuri.podcasts.widget.data.** { *; }

-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
