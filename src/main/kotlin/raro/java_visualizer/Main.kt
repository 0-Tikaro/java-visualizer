package raro.java_visualizer

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.swing_viewer.ViewPanel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


var fileList: MutableList<JavaFile> = mutableListOf();
val graphCss = """
node { size: 8px; text-size: 20px; fill-color: #CCC; }
edge { fill-color: #CCC; }

node.file { fill-color: #00A; }
node.import { fill-color: #A00; }
""".trimIndent()


fun main(args: Array<String>) {
    System.setProperty("org.graphstream.ui", "swing")

    //Read files
    assert(args.isNotEmpty())
    val path: String = args[0]
    Files.walk(Paths.get(path))
            .filter(Files::isRegularFile)
            .forEach(FileProcessor::processFile)


    // Setup graph
    val graph: Graph = SingleGraph("Tutorial 1")
    graph.setAttribute("ui.quality")
    graph.setAttribute("ui.antialias")
    graph.setAttribute("ui.stylesheet", graphCss)



    // Populate graph
    for (javaFile in fileList) {
        val fileNode = graph.addNode(javaFile.filepath)
        fileNode.setAttribute("ui.class", "file")
        fileNode.setAttribute("ui.label", javaFile.filename)

        for (import in javaFile.imports) {
            val node = graph.getNode(import.contents) ?: graph.addNode(import.contents)
            if (import.contents.contains("cz.quanti.visap")) {
                node.setAttribute("ui.class", "import")
                node.setAttribute("ui.label", import.contents)
            }

            graph.addEdge("${javaFile.filepath} --> ${import.contents}", javaFile.filepath, import.contents)
        }
    }
    val viewer = graph.display(true)
    val view = viewer.defaultView as ViewPanel // ViewPanel is the view for gs-ui-swing
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