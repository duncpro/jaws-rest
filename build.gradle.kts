import com.duncpro.jackal.rds.AmazonDataAPIDatabase
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.services.rdsdata.RdsDataAsyncClient
import java.nio.file.Files.*
import java.util.concurrent.Executors

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
        classpath("com.duncpro:jackal:1.0-SNAPSHOT-3")
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
        val transactionExecutor = Executors.newSingleThreadExecutor()
        val rdsClient = RdsDataAsyncClient.create()
        val db = AmazonDataAPIDatabase(rdsClient, dbArn, dbSecretArn, transactionExecutor)

        db.commitTransaction { th ->
            for (statement in readAllLines(dbInitFile).joinToString("").split(";")) {
                if (statement.isBlank()) continue
                th.prepareStatement(statement).executeUpdate().join()
            }
        }.join()


        transactionExecutor.shutdown()
        rdsClient.close()
    }

    outputs.upToDateWhen { false }
}

val deploy by tasks.registering {
    dependsOn(initRemoteDatabase)
    // Transitive dependency on: deployCdkStack
}
