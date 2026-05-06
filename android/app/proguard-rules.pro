# Drive in Car ProGuard rules

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keep class kotlin.Metadata { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firestore (uses reflection on data classes)
-keep class com.driveincar.data.** { *; }
-keep class com.driveincar.domain.model.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
