plugins {
    alias(libs.plugins.android.application) apply false
    // hilt injection
    alias(libs.plugins.ksp) apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
}