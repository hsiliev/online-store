dependencies {
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

tasks.register<Test>("itest") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    setTestClassesDirs(project.sourceSets["test"].output.classesDirs)
    setClasspath(project.sourceSets["test"].runtimeClasspath)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
