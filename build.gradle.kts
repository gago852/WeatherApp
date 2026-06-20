// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Force patched versions for transitive dependencies with known CVEs.
// Dependabot cannot auto-update these because they are not declared directly.
allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.bitbucket.b_c:jose4j:0.9.6")               // DoS via compressed JWE
            force("org.jdom:jdom2:2.0.6.1")                        // XXE Injection
            force("org.bouncycastle:bcprov-jdk18on:1.84")          // LDAP Injection
            force("org.bouncycastle:bcpkix-jdk18on:1.84")          // Broken crypto algorithm
            force("org.apache.commons:commons-lang3:3.18.0")       // Uncontrolled recursion DoS
            force("org.apache.httpcomponents:httpclient:4.5.13")   // XSS
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
//    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.secrets) apply false
    alias(libs.plugins.stability.analyzer) apply false
}