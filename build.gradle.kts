plugins {
    kotlin("jvm") version "1.5.20"
    id("org.jetbrains.dokka") version "1.4.32"
    `maven-publish`
}

repositories {
   mavenCentral()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("NetworkResponseAdapter") {
            groupId = "com.github.haroldadmin"
            artifactId = "NetworkResponseAdapter"
            version = "4.2.1"

            from(components["java"])
        }
    }
}

dependencies {
    val coroutinesVersion = "1.5.0"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.9.1"

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("com.google.guava:guava:26.0-jre")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}

tasks.dokkaGfm.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}