package raro.java_visualizer

import org.graphstream.graph.implementations.SingleGraph

private const val GRAPH_CSS_FILEPATH = "graph.css"

object GraphVisualizer {

    private val graph = SingleGraph("Master graph")
    private var fileList: List<JavaFile>? = null

    fun visualize(fileList: List<JavaFile>) {
        System.setProperty("org.graphstream.ui", "swing")

        this.fileList = fileList

        setupGraph()
        populateGraph()
        displayGraph()
    }

    private fun setupGraph() {
        graph.setAttribute("ui.quality")
        graph.setAttribute("ui.antialias")
        graph.setAttribute("ui.stylesheet", loadCss())
    }

    private fun loadCss(): String? {
        val classloader = Thread.currentThread().contextClassLoader
        val css = classloader.getResourceAsStream(GRAPH_CSS_FILEPATH)
                ?.readAllBytes()
        if (css == null) {
            println("WARN: Could not load CSS")
            return null
        }
        return String(css)
    }

    private fun populateGraph() {
        assert(fileList != null) //todo better solution?

        for (javaFile in fileList!!) {
            val fileNode = graph.addNode(javaFile.filepath)
            fileNode.setAttribute("ui.class", "file")
            fileNode.setAttribute("ui.label", javaFile.filename)
            fileNode.setAttribute("ui.size", javaFile.imports.size)

            javaFile.imports
                    .filter { it.contents.contains(commonImportPrefix) }
                    .map { it.contents.removePrefix(commonImportPrefix) }
                    .forEach { serialziedImport ->
                        val node = graph.getNode(serialziedImport) ?: graph.addNode(serialziedImport)
                        node.setAttribute("ui.class", "import")
                        node.setAttribute("ui.label", serialziedImport.split(".").last())
                        val edgeId = "${javaFile.filepath}->$serialziedImport"
                        graph.addEdge(edgeId, javaFile.filepath, serialziedImport, true)
                    }
        }
    }

    private fun displayGraph() {
        graph.display(true)
    }
}