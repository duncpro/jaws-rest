val cfnOutputs: Configuration by configurations.creating {}

dependencies {
    cfnOutputs(project(":aws-cloud-app", "cfnOutputs"))
}

val run by tasks.registering {
    dependsOn(cfnOutputs)
    doLast {
        exec {
            environment("PATH_TO_CFN_OUTPUTS", cfnOutputs.singleFile.absolutePath)
            commandLine("npm", "test")
        }
    }
}
