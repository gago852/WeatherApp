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
#-renamesourcefileattribute SourceFile
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable

# Keep source file and line numbers for better crash reports
-renamesourcefileattribute SourceFile

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep `Companion` object fields of serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.* *;
    @javax.inject.* *;
}

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.animation.** { *; }

# Keep Compose UI classes that are accessed via reflection
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }
-keep class androidx.compose.ui.platform.CompositionLocalsKt { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep your DTOs
-keepnames @kotlin.Metadata class com.gago.weatherapp.data.remote.dto.**
-keep class com.gago.weatherapp.data.remote.dto.** { *; }
-keepclassmembers class com.gago.weatherapp.data.remote.dto.** { *; }

# Keep your ViewModels
-keep class com.gago.weatherapp.ui.**ViewModel { *; }
-keep class com.gago.weatherapp.ui.**ViewModel$* { *; }

# Keep your Composables
-keepclassmembers class com.gago.weatherapp.ui.** {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep your Hilt modules and components
-keep class com.gago.weatherapp.di.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# DataStore
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keepclassmembers class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.core.** { *; }
-keepclassmembers class androidx.datastore.core.** { *; }

# Keep DataStore serializers - Modified to handle generics better
-keepclassmembers class * implements androidx.datastore.core.Serializer {
    <init>(...);
    java.lang.Object readFrom(...);
    void writeTo(...);
}

# Keep your DataStore preferences
-keep class com.gago.weatherapp.data.datastore.** { *; }
-keepclassmembers class com.gago.weatherapp.data.datastore.** { *; }

# Keep Proto DataStore if you're using it
-keep class com.google.protobuf.** { *; }
-keepclassmembers class com.google.protobuf.** { *; }

# Additional rules for generics
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep generic signatures for serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializable <methods>;
}

# Keep generic type information for Moshi
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Keep generic type information for Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep generic type information for Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.* *;
    @javax.inject.* *;
}

# Keep generic type information for Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep your ViewModels with generics
-keep class com.gago.weatherapp.ui.**ViewModel { *; }
-keep class com.gago.weatherapp.ui.**ViewModel$* { *; }

# Keep your Composables with generics
-keepclassmembers class com.gago.weatherapp.ui.** {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep your Hilt modules with generics
-keep class com.gago.weatherapp.di.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# Keep generic type information for your DTOs
-keepnames @kotlin.Metadata class com.gago.weatherapp.data.remote.dto.**
-keep class com.gago.weatherapp.data.remote.dto.** { *; }
-keepclassmembers class com.gago.weatherapp.data.remote.dto.** { *; }