# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Attributes needed for reflection-based libraries and generics
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*, AnnotationDefault

# Kotlin Serialization
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

-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializable <methods>;
}

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.* *;
    @javax.inject.* *;
}
-keep class com.gago.weatherapp.di.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# Retrofit
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Moshi
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Keep network DTOs
-keepnames @kotlin.Metadata class com.gago.weatherapp.data.remote.dto.**
-keep class com.gago.weatherapp.data.remote.dto.** { *; }
-keepclassmembers class com.gago.weatherapp.data.remote.dto.** { *; }

# Keep ViewModels
-keep class com.gago.weatherapp.ui.**ViewModel { *; }
-keep class com.gago.weatherapp.ui.**ViewModel$* { *; }

# Keep Composables
-keepclassmembers class com.gago.weatherapp.ui.** {
    @androidx.compose.runtime.Composable <methods>;
}

# DataStore serializers
-keepclassmembers class * implements androidx.datastore.core.Serializer {
    <init>(...);
    java.lang.Object readFrom(...);
    void writeTo(...);
}

# Keep DataStore-persisted models
-keep class com.gago.weatherapp.data.datastore.** { *; }
-keepclassmembers class com.gago.weatherapp.data.datastore.** { *; }

# Google Places SDK
-dontwarn io.grpc.internal.DnsNameResolverProvider
-dontwarn io.grpc.internal.PickFirstLoadBalancerProvider

# Keep no-arg constructors for ServiceLoader
-keepclassmembers class * {
    <init>();
}

# Avoid ServiceConfigurationError from Places
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**
