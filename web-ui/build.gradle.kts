import com.fasterxml.jackson.databind.ObjectMapper

val nodeToolsRoot = System.getProperty("user.home") + "/.nvm/versions/node/v14.17.4/bin"
val npm = "$nodeToolsRoot/npm"
val npx = "$nodeToolsRoot/npx"

buildDir = file("gradleBuild")

buildscript {
    dependencies {
        classpath("com.fasterxml.jackson.core:jackson-databind:2.12.4")
        classpath("com.fasterxml.jackson.core:jackson-core:2.12.4")
    }
    repositories {
        mavenCentral()
    }
}

val dtoInterfaces: Configuration by configurations.creating {}
dependencies {
    dtoInterfaces(project(":request-handler-lambda", "dtoInterfaces"))
}

@OptIn(ExperimentalStdlibApi::class)
val buildDtoInterfacesModule by tasks.registering {
    dependsOn(dtoInterfaces)

    doLast {
        copy {
            from(dtoInterfaces)
            into(buildDir.resolve("dto-interfaces"))
        }
    }

    // Generate package.json
    doLast {
        val packageJsonFile = buildDir.resolve("dto-interfaces/package.json")
        packageJsonFile.parentFile.mkdirs()
        packageJsonFile.createNewFile()
        val packageJsonContents = buildMap<String, Any> {
            put("name", "dto-interfaces")
            put("private", true)
        }
        ObjectMapper().writeValue(packageJsonFile, packageJsonContents)
    }

    outputs.dir(buildDir.resolve("dto-interfaces"))
}

val installDtoInterfaces by tasks.registering(Exec::class) {
    dependsOn(buildDtoInterfacesModule)
    commandLine(npm, "install", buildDtoInterfacesModule.get().outputs.files.asPath)
}

val buildWebUiForProduction by tasks.registering(Exec::class) {
    dependsOn(installDtoInterfaces)
    commandLine(npm, "run", "build")
}
