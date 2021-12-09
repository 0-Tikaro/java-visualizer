package raro.java_visualizer

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

object FileSystemReader {

    fun readDirectoryRecursively(rootDirectory: Path): List<JavaFile> {
        return Files.walk(rootDirectory)
                .filter(Files::isRegularFile)
                .map(this::processFile)
                .toList()
    }

    private fun processFile(path: Path): JavaFile {
        val imports = readImports(path)
        val file = JavaFile(path.toString(), path.fileName.toString())
        imports.map { Import(it) }
                .forEach { file.imports.add(it) }
        return file
    }

    private fun readImports(path: Path): List<String> {
        val lines = path.toFile().bufferedReader().lines()
        return lines.filter { it.startsWith("import ") }
                .map { it.removePrefix("import ") }
                .toList()
    }
}