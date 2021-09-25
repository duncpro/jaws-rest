val restRequestHandlerPackage: Configuration by configurations.creating {}

dependencies {
    restRequestHandlerPackage(project(":request-handler-lambda", "lambdaPackage"))
}

val destroy by tasks.registering {
    dependsOn(restRequestHandlerPackage)

    doLast {
        exec {
            commandLine("cdk", "destroy", "--force")
            environment("PATH_TO_REQUEST_HANDLER_PACKAGE", restRequestHandlerPackage.singleFile.absolutePath)
        }
    }
}


val deployCdkStack by tasks.registering {
    dependsOn(restRequestHandlerPackage)

    doLast {
        exec {
            commandLine("cdk", "deploy", "--outputs-file", "latest-deployment-cfn-outputs.json",
                "--require-approval", "never")
            environment("PATH_TO_REQUEST_HANDLER_PACKAGE",
                restRequestHandlerPackage.singleFile.absolutePath)
        }
    }

    outputs.file("./latest-deployment-cfn-outputs.json");
    outputs.upToDateWhen { false } // Defer diffing to AWS CDK CLI
}

// Expose the deployed REST API URL to the integration testing module.
val cfnOutputs by configurations.registering {}
artifacts.add(cfnOutputs.name, deployCdkStack)
