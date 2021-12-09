package raro.java_visualizer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


var fileList: MutableList<JavaFile> = mutableListOf();

fun main(args: Array<String>) {
    assert(args.isNotEmpty())

    val path: String = args[0]


    Files.walk(Paths.get(path))
            .filter(Files::isRegularFile)
            .forEach(FileProcessor::processFile)

    fileList.forEach(::println)
}

object FileProcessor {
    fun processFile(path: Path) {
        val imports = readImports(path)

        val file = JavaFile(path.toString())
        imports.map { Import(it) }
                .forEach { file.imports.add(it) }
        fileList.add(file)
    }

    private fun readImports(path: Path): List<String> {
        val lines = path.toFile().bufferedReader().lines()
        return lines.filter { it.startsWith("import ") }
                .map { it.removePrefix("import ") }
                .toList()
    }

}

data class JavaFile(val file: String, val imports: MutableSet<Import> = mutableSetOf()) {
    override fun toString(): String {
        val serializedImports = imports
                .map(Import::toString)
                .joinToString(separator = "\n")
        return """
File: $file
Imports: ${serializedImports}
---
        """.trimIndent()
    }
}

data class Import(val contents: String) {
    override fun toString(): String {
        return contents
    }
}