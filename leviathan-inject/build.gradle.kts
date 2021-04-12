plugins {
    id("kotlin")
    id("kotlin-kapt")
}

sourceSets {
    named("main") {
    }
}

dependencies {
    implementation(project(":leviathan"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
    implementation ("com.google.auto.service:auto-service:1.0-rc4")
    kapt ("com.google.auto.service:auto-service:1.0-rc4")
    testImplementation("junit:junit:4.13.2")
}