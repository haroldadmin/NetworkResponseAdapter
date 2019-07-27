plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.41")
    maven
}

group = "com.github.haroldadmin"

repositories {
    jcenter()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-RC")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.0.1")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.0.1")
    testImplementation("com.google.guava:guava:26.0-jre")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
}
