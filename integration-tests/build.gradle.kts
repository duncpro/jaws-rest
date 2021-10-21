import com.fasterxml.jackson.databind.ObjectMapper

buildscript {
    repositories {
        mavenCentral();
    }

    dependencies {
        classpath("com.fasterxml.jackson.core:jackson-databind:2.12.4")
        classpath("com.fasterxml.jackson.core:jackson-core:2.12.4")
    }
}

val cfnOutputs: Configuration by configurations.creating {}
val dtoInterfacesNodeModule: Configuration by configurations.creating {}

dependencies {
    cfnOutputs(project(":aws-cloud-app", "cfnOutputs"))
    dtoInterfacesNodeModule(project(":request-handler-lambda", "dtoInterfacesNodeModule"))
}

// Tasks

val runIntegrationTestsLocally by tasks.registering {
    runIntegrationTests("http://localhost:8000")
}

val runIntegrationTests by tasks.registering {
    dependsOn(cfnOutputs)
    dependsOn(tasks.findByPath(":deploy"))
    dependsOn(installDtoInterfaces)
    runIntegrationTests(readDeployedApiUrl(cfnOutputs.singleFile.absolutePath))
}

val npm = "${project.ext["NODE_HOME"]}/npm"
val installDtoInterfaces by tasks.registering(Exec::class) {
    dependsOn(dtoInterfacesNodeModule)
    commandLine(npm, "install", dtoInterfacesNodeModule.asPath)
}

// Helpers

fun Task.runIntegrationTests(againstApiUrl: String) {
    dependsOn(installDtoInterfaces)
    doLast {
        exec {
            environment("API_URL", againstApiUrl)
            commandLine("npm", "test")
        }
    }
}

fun readDeployedApiUrl(cdkOutputsPath: String): String {
    val outputs = ObjectMapper().readTree(File(cdkOutputsPath))
    return outputs["MainStack"]["MainRestApiUrl"].textValue();
}
