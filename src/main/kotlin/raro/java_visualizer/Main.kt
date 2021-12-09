package raro.java_visualizer

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.swing_viewer.ViewPanel
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


var fileList: MutableList<JavaFile> = mutableListOf();

fun main(args: Array<String>) {
    System.setProperty("org.graphstream.ui", "swing")

    assert(args.isNotEmpty())
    val path: String = args[0]
    Files.walk(Paths.get(path))
            .filter(Files::isRegularFile)
            .forEach(FileProcessor::processFile)

    GraphVisualizer.visualize()
}


object FileProcessor {
    fun processFile(path: Path) {
        val imports = readImports(path)

        val file = JavaFile(path.toString(), path.fileName.toString())
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

data class JavaFile(
        val filepath: String,
        val filename: String,
        val imports: MutableSet<Import> = mutableSetOf()
)

data class Import(val contents: String)


object GraphVisualizer {

    val graph: Graph = SingleGraph("Master graph")

    fun visualize() {
        setupGraph()
        populateGraph()
        displayGraph()
    }

    private fun setupGraph() {
        graph.setAttribute("ui.quality")
        graph.setAttribute("ui.antialias")
        graph.setAttribute("ui.stylesheet", loadCss())
    }

    private fun loadCss(): String {
        val classloader: ClassLoader = Thread.currentThread().contextClassLoader
        val cssStream: InputStream = classloader.getResourceAsStream("graph.css")
        return String(cssStream.readAllBytes())
    }

    private fun populateGraph() {
        val COMMON_IMPORT_PREFIX = "cz.quanti.visap.logistics"

        for (javaFile in fileList) {
            val fileNode = graph.addNode(javaFile.filepath)
            fileNode.setAttribute("ui.class", "file")
            fileNode.setAttribute("ui.label", javaFile.filename)

            javaFile.imports
                    .filter { it.contents.contains(COMMON_IMPORT_PREFIX) }
                    .map { it.contents.removePrefix(COMMON_IMPORT_PREFIX) }
                    .forEach { serialziedImport ->
                        val node = graph.getNode(serialziedImport) ?: graph.addNode(serialziedImport)
                        node.setAttribute("ui.class", "import")
                        node.setAttribute("ui.label", serialziedImport)

                        graph.addEdge("${javaFile.filepath} --> $serialziedImport", javaFile.filepath, serialziedImport)
                    }
        }
    }

    private fun displayGraph() {
        val viewer = graph.display(true)
        val view = viewer.defaultView as ViewPanel
    }
}