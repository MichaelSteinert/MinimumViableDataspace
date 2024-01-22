plugins {
    `java-library`
}

val rsApi: String by project

dependencies {
    implementation(libs.edc.ext.http)
    implementation(libs.edc.ext.configuration.filesystem)
    implementation(libs.jakarta.rsApi)
}
