# Publishing Readability4K

This guide explains how to publish Readability4K locally and use it in your projects.

## Publishing to Maven Local

Maven Local is a local Maven repository on your computer (~/.m2/repository) where you can publish libraries for testing and development.

### Step 1: Publish the Library

From the Readability4K project directory:

```bash
./gradlew publishToMavenLocal
```

This will:
- Build all variants (Android, JVM, iOS)
- Create Maven artifacts
- Publish to `~/.m2/repository/com/sermilion/readability4k-android/1.0.0/`

### Step 2: Verify Publication

Check that the library was published:

```bash
ls ~/.m2/repository/com/sermilion/readability4k-android/1.0.0/
```

You should see files like:
- `readability4k-android-1.0.0.aar`
- `readability4k-android-1.0.0.pom`
- `readability4k-android-1.0.0-sources.jar`

---

## Using in Your Android Project

### Step 1: Add Maven Local Repository

**For Kotlin DSL (settings.gradle.kts):**

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // Add this line
    }
}
```

**For Groovy (settings.gradle or build.gradle):**

```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // Add this line
    }
}
```

### Step 2: Add Dependency

**app/build.gradle.kts** (Kotlin DSL):

```kotlin
dependencies {
    // Simple! Works for Android, JVM, iOS - Gradle auto-resolves the correct variant
    implementation("com.sermilion:readability4k:1.0.0")
}
```

**app/build.gradle** (Groovy):

```groovy
dependencies {
    implementation 'com.sermilion:readability4k:1.0.0'
}
```

> **Note:** No need to specify `-android`, `-jvm`, etc. Gradle's KMP plugin automatically resolves the correct platform variant based on your project configuration!

### Step 3: Sync Project

In Android Studio:
- Click **"Sync Now"** when prompted
- Or: **File → Sync Project with Gradle Files**

---

## Basic Usage in Android

### In a ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sermilion.readability4k.Readability4K
import com.sermilion.readability4k.model.ReadabilityOptions
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {

    fun extractArticle(url: String, html: String) {
        viewModelScope.launch {
            try {
                val options = ReadabilityOptions(
                    charThreshold = 500,
                    keepClasses = false,
                    disableJSONLD = false
                )

                val readability = Readability4K(url, html, options)
                val article = readability.parseAsync()  // Use async for Android!

                // Use the extracted article
                println("Title: ${article.title}")
                println("Author: ${article.byline}")
                println("Content: ${article.content}")
                println("Text: ${article.textContent}")
                println("Length: ${article.length} characters")
            } catch (e: Exception) {
                println("Failed to extract article: ${e.message}")
            }
        }
    }
}
```

### In a Repository

```kotlin
import com.sermilion.readability4k.Readability4K
import com.sermilion.readability4k.Article
import com.sermilion.readability4k.model.ReadabilityOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleRepository {

    suspend fun parseArticle(
        url: String,
        html: String,
        charThreshold: Int = 500
    ): Result<Article> = withContext(Dispatchers.IO) {
        try {
            val options = ReadabilityOptions(charThreshold = charThreshold)
            val readability = Readability4K(url, html, options)
            val article = readability.parseAsync()
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### With Compose UI

```kotlin
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ArticleScreen(
    url: String,
    html: String,
    viewModel: ArticleViewModel = viewModel()
) {
    var article by remember { mutableStateOf<Article?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(url) {
        loading = true
        try {
            val readability = Readability4K(url, html)
            article = readability.parseAsync()
        } finally {
            loading = false
        }
    }

    when {
        loading -> CircularProgressIndicator()
        article != null -> {
            Column {
                Text(article!!.title ?: "No title", style = MaterialTheme.typography.h4)
                Text(article!!.byline ?: "Unknown author", style = MaterialTheme.typography.subtitle1)
                Text(article!!.textContent ?: "No content")
            }
        }
        else -> Text("Failed to extract article")
    }
}
```

---

## Updating the Library

After making changes to Readability4K:

### 1. Republish to Maven Local

```bash
cd /path/to/Readability4K
./gradlew clean publishToMavenLocal
```

### 2. Update Version (Optional)

Edit `library/build.gradle.kts`:

```kotlin
group = "com.sermilion"
version = "1.0.1"  // Increment version
```

### 3. Refresh in Android Studio

- **File → Invalidate Caches → Restart**
- Or: Delete build folders and re-sync

---

## Available Artifacts

After publishing, Gradle creates these platform-specific artifacts automatically:

| Artifact ID | Target Platform | Notes |
|------------|-----------------|-------|
| `readability4k` | Common (use this!) | Gradle auto-resolves to the correct platform variant |
| `readability4k-android` | Android | Auto-resolved by Gradle |
| `readability4k-jvm` | JVM/Desktop | Auto-resolved by Gradle |
| `readability4k-iosarm64` | iOS Device | Auto-resolved by Gradle |
| `readability4k-iossimulatorarm64` | iOS Simulator (M1+) | Auto-resolved by Gradle |
| `readability4k-iosx64` | iOS Simulator (Intel) | Auto-resolved by Gradle |

**Just use:** `implementation("com.sermilion:readability4k:1.0.0")` and Gradle does the rest!

---

## Troubleshooting

### "Could not find com.sermilion:readability4k-android:1.0.0"

**Solution:**
1. Make sure you ran `./gradlew publishToMavenLocal`
2. Check that `mavenLocal()` is in your repositories
3. Try: **File → Invalidate Caches → Restart**

### "Duplicate class" errors

**Solution:**
- Make sure you're not including the library multiple times
- Check for conflicting versions
- Clean build: `./gradlew clean`

### Changes not reflected in Android project

**Solution:**
1. Republish: `./gradlew clean publishToMavenLocal`
2. In Android Studio: **File → Invalidate Caches → Restart**
3. Delete `~/.gradle/caches` if issues persist

### Build fails with "Cannot access class"

**Solution:**
- Make sure you're using `parseAsync()` instead of `parse()` on Android
- Check that coroutines dependency is included (should be transitive)

---

## Publishing to Remote Repository (Future)

When ready to publish publicly:

### Maven Central

1. Set up Sonatype account
2. Add signing configuration
3. Configure publishing:

```kotlin
publishing {
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}
```

### JitPack

1. Push to GitHub
2. Create a release tag
3. Visit https://jitpack.io
4. Use dependency:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.sermilion:readability4k:TAG")
}
```

---

## Quick Reference

```bash
# Publish locally
./gradlew publishToMavenLocal

# Clean and publish
./gradlew clean publishToMavenLocal

# Check published files
ls ~/.m2/repository/com/sermilion/readability4k/1.0.0/

# In your Android/iOS/JVM project - just use the common artifact!
implementation("com.sermilion:readability4k:1.0.0")
```

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with 100% Mozilla Readability API parity |

---

For more information:
- [Main README](README.md) - Library documentation and features
- [CLI README](cli/README.md) - Command-line tool usage
