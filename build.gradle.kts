
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files.*

buildscript {
    repositories {
        maven {
            url = uri("https://duncpro-personal-618824625980.d.codeartifact.us-east-1.amazonaws.com/maven/duncpro-personal/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
            }
        }
        maven("https://jitpack.io")
        mavenCentral()
    }
    dependencies {
        classpath("com.duncpro:jackal:1.0-SNAPSHOT-10")
        classpath(platform("software.amazon.awssdk:bom:2.15.0"))
        classpath("software.amazon.awssdk:rdsdata")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.12.4")
        classpath("com.fasterxml.jackson.core:jackson-core:2.12.4")
    }
}

group = "com.duncpro"
version = "1.0-SNAPSHOT"

val cfnOutputs: Configuration by configurations.creating {}

dependencies {
    cfnOutputs(project(":aws-cloud-app", "cfnOutputs"))
}

val initRemoteDatabase by tasks.registering {
    dependsOn(cfnOutputs)

    doLast {
        val cfnOutputsContent = ObjectMapper().readTree(cfnOutputs.singleFile)
        val dbArn = cfnOutputsContent["MainStack"]["MasterDbArn"].textValue()
        val dbSecretArn = cfnOutputsContent["MainStack"]["MasterDbSecretArn"].textValue()
        val dbInitFile = rootProject.projectDir.resolve("setup-database.sql").toPath();
        val db = com.duncpro.jackal.aws.DefaultAuroraServerlessRelationalDatabase(dbArn, dbSecretArn)

        for (statement in readAllLines(dbInitFile).joinToString("").split(";")) {
            if (statement.isBlank()) continue
            db.prepareStatement(statement).executeUpdate()
        }
    }

    outputs.upToDateWhen { false }
}

val deploy by tasks.registering {
    dependsOn(initRemoteDatabase)
    // Transitive dependency on: deployCdkStack
}
