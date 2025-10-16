plugins {
    java
    alias(libs.plugins.quarkus)
}

group = "io.fouad"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform(libs.quarkus.bom))
    implementation(libs.quarkus.picocli)
    implementation(libs.quarkus.awt)
    implementation(libs.quarkiverse.barcode.zxing)
    implementation(libs.quarkiverse.pdfbox)
    implementation(libs.quarkiverse.poi)
}

quarkus {
    set("package.jar.enabled", "false")
    set("native.enabled", "true")
}