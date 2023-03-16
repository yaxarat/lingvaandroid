// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // TODO: cosolidate to buildSrc
    val kotlin_compiler_version = "1.8.10"

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_compiler_version")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:$kotlin_compiler_version")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
