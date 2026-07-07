# Project-specific R8 rules. Library rules (Hilt, Retrofit, OkHttp, Moshi,
# kotlinx.serialization, DataStore) ship as consumer rules inside each artifact,
# so only rules that no dependency can provide for itself belong here.

# Keep source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# gRPC (pulled in by the Places SDK) discovers these providers through
# ServiceLoader, which instantiates them reflectively via the no-arg constructor.
-keepclassmembers class * extends io.grpc.NameResolverProvider {
    <init>();
}
-keepclassmembers class * extends io.grpc.LoadBalancerProvider {
    <init>();
}
-dontwarn io.grpc.internal.DnsNameResolverProvider
-dontwarn io.grpc.internal.PickFirstLoadBalancerProvider
-dontwarn com.google.android.libraries.places.**
