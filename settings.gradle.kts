pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.cloudstream.cf/repository/maven-public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "CloudstreamPlugins"

// هذا الجزء يتأكد من إدراج كل الإضافات الموجودة تلقائيًا
val disabled = listOf<String>()

File(rootDir, ".").eachDir { dir ->
    if (!disabled.contains(dir.name) && File(dir, "build.gradle.kts").exists()) {
        include(dir.name)
    }
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}
