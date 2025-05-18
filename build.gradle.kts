// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    //Dagger Hilt
    id("com.google.dagger.hilt.android") version "2.50" apply false

    //for the Ksp
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
}

allprojects{
    repositories{
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
}
