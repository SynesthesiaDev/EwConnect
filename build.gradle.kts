import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.3.20"
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
    id("com.gradleup.shadow") version "9.2.2"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}


val shade: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations.runtimeOnly.get().extendsFrom(shade)

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("ewconnect") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    implementation(kotlin("stdlib-jdk8"))
    implementation("net.dv8tion:JDA:6.4.1")
    implementation("club.minnced:jda-ktx:0.14.2")

    val jdaVersion = "6.4.1"
    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    shade("net.dv8tion:JDA:$jdaVersion")

    implementation("club.minnced:jda-ktx:0.14.2")
    shade("club.minnced:jda-ktx:0.14.2")
    
    include(implementation("org.mapdb:mapdb:3.1.0")!!)
    include(modImplementation("io.github.revxrsal:lamp.common:4.0.0-rc.16")!!)
    include(modImplementation("io.github.revxrsal:lamp.fabric:4.0.0-rc.16")!!)
    include(modImplementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.16")!!)
    shade("org.mapdb:mapdb:3.1.0")

    modImplementation(include("net.kyori:adventure-platform-fabric:6.8.0")!!)
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "minecraft_version" to project.property("minecraft_version").toString(),
            "loader_version" to project.property("loader_version").toString(),
            "kotlin_loader_version" to project.property("kotlin_loader_version").toString()
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

kotlin {
    compilerOptions {
        javaParameters = true
    }
}

// optional: if you're using Kotlin
tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        javaParameters = true
    }
}

tasks.build {
    dependsOn(tasks.remapJar)
}

tasks.named<RemapJarTask>("remapJar") {
    finalizedBy("finalJar")
}

tasks.withType<ShadowJar>().configureEach {
    isZip64 = true
}

tasks.register<ShadowJar>("finalJar") {
    archiveClassifier.set("final")
    configurations = listOf(shade)

    doFirst {
        val remappedFile = tasks.named<RemapJarTask>("remapJar").get().archiveFile.get().asFile
        from(zipTree(remappedFile))
    }

    relocate("net.dv8tion.jda", "net.ririfa.shadowed.jda")
    relocate("org.yaml.snakeyaml", "net.ririfa.shadowed.snakeyaml")
    relocate("dev.minn.jda.ktx", "net.ririfa.shadowed.ktx")
    relocate("com.fasterxml.jackson", "net.ririfa.shadowed.jackson")
    
    exclude("com/sun/jna/**")
    exclude("tomp2p/**")
}

kotlin {
    jvmToolchain(21)
}
