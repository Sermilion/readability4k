# Readability4K ProGuard Rules

# Keep Article data class for serialization/reflection
-keep class com.sermilion.readability4k.Article { *; }

# Keep exception classes for crash reporting and proper stack traces
-keep class com.sermilion.readability4k.ReadabilityException { *; }
-keep class com.sermilion.readability4k.MaxElementsExceededException { *; }

# Keep model classes used for configuration and output
-keep class com.sermilion.readability4k.model.** { *; }

# Keep Readability4K public API
-keep class com.sermilion.readability4k.Readability4K {
    public <init>(...);
    public ** parse();
    public ** parseAsync();
}

# Ksoup HTML parser - keep reflection-accessed members
-keepclassmembers class com.fleeksoft.ksoup.** {
    <init>(...);
}

# Keep Logger interface for extension
-keep interface com.sermilion.readability4k.util.Logger {
    *;
}

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Preserve generic signatures for proper type inference
-keepattributes Signature

# Preserve annotations for runtime reflection
-keepattributes *Annotation*

# If using kotlinx.serialization (optional)
-if class com.sermilion.readability4k.Article
-keepclassmembers class com.sermilion.readability4k.Article {
    kotlinx.serialization.KSerializer serializer(...);
}

# R8 full mode compatibility
-allowaccessmodification
-repackageclasses
