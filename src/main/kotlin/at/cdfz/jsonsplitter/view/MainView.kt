package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.controller.JsonDocument
import at.cdfz.jsonsplitter.controller.ProcessingController
import javafx.scene.Parent
import javafx.stage.FileChooser
import tornadofx.*

class MainView : View() {

    val processingController: ProcessingController by inject()

    override val root = vbox {

        setPrefSize(600.0, 400.0)

        menubar {
            menu("Help") {
                item("About JsonSplitter").action {
                    find<AboutView>().openModal()
                }
            }
        }

        vbox {
            spacing = 10.0
            paddingAll = 10.0

            vbox {

                label("Source documents")

                tableview(processingController.documents) {
                    useMaxWidth = true

                    readonlyColumn("Path", JsonDocument::path)
                    readonlyColumn("Name", JsonDocument::path).cellFormat {
                        graphic = vbox {
                            button("Delete").action { processingController.removeDocument(it) }
                        }
                    }

                    smartResize()
                }

                hbox {

                    button("Add source file").action {
                        val files = chooseFile(
                                "Select input files",
                                filters = arrayOf(FileChooser.ExtensionFilter("JSON document", "*.json")),
                                mode = FileChooserMode.Multi)

                        for (file in files) {
                            val document = JsonDocument(file)
                            processingController.addDocument(document)
                        }
                    }

                    button("Add source folder").action {
                        val directory = chooseDirectory("Add an input folder") ?: return@action

                        val files = directory
                                .walkTopDown()
                                .filter { it.extension == "json" }
                                .toList()

                        for (file in files) {
                            val document = JsonDocument(file)
                            processingController.addDocument(document)
                        }
                    }
                }
            }

            vbox {

                label("output directory")
            }

        }
    }
}