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

dependencies {
    cfnOutputs(project(":aws-cloud-app", "cfnOutputs"))
}

val runLocal by tasks.registering {
    doLast {
        exec {
            environment("API_URL", "http://localhost:8000")
            commandLine("npm", "test")
        }
    }
}

val run by tasks.registering {
    dependsOn(cfnOutputs)
    doLast {
        exec {
            environment("API_URL", readDeployedApiUrl(cfnOutputs.singleFile.absolutePath))
            commandLine("npm", "test")
        }
    }
}

fun readDeployedApiUrl(cdkOutputsPath: String): String {
    val outputs = ObjectMapper().readTree(File(cdkOutputsPath))
    return outputs["MainStack"]["MainRestApiUrl"].textValue();
}
