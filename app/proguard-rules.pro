# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep names of classes that are used via reflection
-keep class com.example.grapheneadminapp.** { *; }

# Keep names of classes that are used as DeviceAdminReceiver
-keep public class * extends android.app.admin.DeviceAdminReceiver

# Keep names of classes that are used as Service
-keep public class * extends android.app.Service

# Keep names of classes that are used as BroadcastReceiver
-keep public class * extends android.content.BroadcastReceiver

# Keep names of classes that are used as ContentProvider
-keep public class * extends android.content.ContentProvider

# Keep names of classes that are used as Application
-keep public class * extends android.app.Application

# Keep names of classes that are used as Activity
-keep public class * extends android.app.Activity

# Keep names of classes that are used as Fragment
-keep public class * extends androidx.fragment.app.Fragment

# Keep names of classes that are used as DialogFragment
-keep public class * extends androidx.fragment.app.DialogFragment

# Keep names of classes that are used as View
-keep public class * extends android.view.View
