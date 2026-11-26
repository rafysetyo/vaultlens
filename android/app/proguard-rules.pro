# VaultLens ProGuard / R8 keep rules for Firebase AI Logic, Firestore, and ML Kit
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepclassmembers class com.vaultlens.app.data.model.** { *; }
-dontwarn com.google.errorprone.annotations.**
