package raro.java_visualizer

import java.nio.file.Paths

var commonImportPrefix: String = ""

data class JavaFile(
        val filepath: String,
        val filename: String,
        val imports: MutableSet<Import> = mutableSetOf()
)

data class Import(
        val contents: String
)

fun main(args: Array<String>) {
    assert(args.size == 3)
    val rootpath = args[0]
    commonImportPrefix = args[1]

    val root = Paths.get(rootpath)
    val files = FileSystemReader.readDirectoryRecursively(root)
    GraphVisualizer.visualize(files)
}

