import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
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
    version.set("0.47.1")
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

    api(libs.coroutines)
    api(libs.retrofit)
    api(libs.okhttp)
    api(libs.okio)

    testImplementation(libs.okhttp.mock.webserver)
    testImplementation(libs.guava)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertion)
    testImplementation(libs.moshi)
    testImplementation(libs.retrofit.converter.moshi)
}

tasks.dokkaGfm.configure {
    moduleName.set(ProjectName)
    moduleVersion.set(ProjectVersion)
    outputDirectory.set(buildDir.resolve("dokka"))
}
