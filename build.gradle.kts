// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.android.library") version "8.5.0" apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply false
}

// Task to check for dependency updates
tasks.register("checkUpdates") {
    group = "help"
    description = "Check for dependency updates"
    doLast {
        println("🔄 Checking for dependency updates...")
        println("Run './gradlew dependencyUpdates' for detailed information")
        println("Or use './gradlew app:dependencyUpdates' for app-specific updates")
    }
}

// Task to show current versions
tasks.register("showVersions") {
    group = "help"
    description = "Show current dependency versions"
    doLast {
        println("📋 Current dependency versions:")
        println("================================")
        val versionsFile = file("gradle/libs.versions.toml")
        if (versionsFile.exists()) {
            versionsFile.readLines()
                .filter { it.startsWith("[versions]") || (it.contains("=") && !it.startsWith("[")) }
                .forEach { println(it) }
        }
    }
}
