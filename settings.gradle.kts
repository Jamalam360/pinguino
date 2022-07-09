include(
    "bot",
    "config",
    "database-converter",
    "logging"
)

rootProject.name = "Pinguino"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
