import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    `maven-publish`
}

val GroupID = "com.github.haroldadmin"
val ArtifactID = "NetworkResponseAdapter"
val ProjectName = "NetworkResponseAdapter"
val ProjectVersion = "5.0.0"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

configure<KtlintExtension> {
    version.set("0.43.0")
    ignoreFailures.set(false)
    disabledRules.set(setOf("no-wildcard-imports"))
}

publishing {
    publications {
        create<MavenPublication>("NetworkResponseAdapter") {
            groupId = GroupID
            artifactId = ArtifactID
            version = ProjectVersion

            from(components["java"])
        }
    }
}

dependencies {
    val coroutinesVersion = "1.5.2"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.9.3"
    val kotestVersion = "5.0.1"

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("com.google.guava:guava:31.0.1-jre")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}

tasks.dokkaGfm.configure {
    moduleName.set(ProjectName)
    moduleVersion.set(ProjectVersion)
    outputDirectory.set(buildDir.resolve("dokka"))
}
