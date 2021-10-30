// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // TODO: cosolidate to buildSrc
    val kotlin_compiler_version = "1.5.31"
    val hilt_agp_version = "2.38.1"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_compiler_version")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hilt_agp_version")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:$kotlin_compiler_version")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
