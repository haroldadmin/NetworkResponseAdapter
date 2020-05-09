plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.72")
    maven
}

group = "com.github.haroldadmin"

repositories {
    jcenter()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

tasks.wrapper {
    gradleVersion = "6.2"
}

dependencies {

    val coroutinesVersion = "1.3.6"
    val retrofitVersion = "2.8.1"
    val okHttpVersion = "4.6.0"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("com.google.guava:guava:26.0-jre")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}
