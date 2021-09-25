plugins {
    java
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://duncpro-personal-618824625980.d.codeartifact.us-east-1.amazonaws.com/maven/duncpro-personal/")
        credentials {
            username = "aws"
            password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
        }
    }
    maven("https://jitpack.io")
}

dependencies {
    // For local deployment only, not used in production or staging.
    // TODO: Do not include this in the Lambda package
    implementation("com.h2database:h2:1.4.200")

    implementation(platform("software.amazon.awssdk:bom:2.15.0"))
    implementation("software.amazon.awssdk:rdsdata")

    implementation("com.duncpro:jroute:1.0-SNAPSHOT-2")
    implementation("com.duncpro:jackal:1.0-SNAPSHOT-2")
    implementation("com.duncpro:rex:1.0-SNAPSHOT-4")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.31")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.2.0")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")

    // AWS Lambda
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.9")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.4")

    implementation("com.google.inject:guice:5.0.1")

    // Testing Tools
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core:3.+")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// Create an artifact which can be executed by the AWS Lambda platform.
// This artifact is consumed by jaws-rest/aws-cloud-app.
// Based off of https://docs.aws.amazon.com/lambda/latest/dg/java-package.html
val buildLambdaPackage by tasks.registering(Zip::class) {
    dependsOn(tasks.test)
    from(tasks.compileJava)
    from(tasks.processResources)
    into("lib") {
        from(configurations.runtimeClasspath)
    }
}
val lambdaPackage: Configuration by configurations.creating;
artifacts.add(lambdaPackage.name, buildLambdaPackage)

val serve by tasks.registering(JavaExec::class) {
    classpath = sourceSets.main.get().runtimeClasspath
    main = "com.duncpro.jaws.LocalJawsServer"
    standardInput = System.`in`
    environment("DB_INIT_SCRIPT", rootProject.projectDir.resolve("setup-database.sql"));
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
