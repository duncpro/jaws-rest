val dtoInterfaces: Configuration by configurations.creating {}

dependencies {
    dtoInterfaces(project(":request-handler-lambda", "dtoInterfaces"))
}

fun buildLocalNodeModule(dir: File) {

}
