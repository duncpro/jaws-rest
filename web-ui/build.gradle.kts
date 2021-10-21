import com.fasterxml.jackson.databind.ObjectMapper

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
}
