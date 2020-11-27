package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.controller.*
import javafx.scene.paint.Color
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
                    column("Data Key", JsonDocument::dataKeyStateProperty).cellFormat {
                        graphic = when (it) {
                            is DataKeyInit -> label("")
                            is DataKeyProcessing -> label("processing...")
                            is DataKeyValue ->
                                when {
                                    it.key == null -> label("")
                                    it.allOptions.size == 1 -> label("\"${it.key}\"")
                                    else -> combobox(
                                        it.keyProperty(),
                                        it.allOptions.keys.toList()
                                    ) {
                                        selectionModel.selectedItemProperty().onChange { key ->
                                            if (key != null) {
                                                confirm(
                                                    "Do you want to change to data key for every similar document?",
                                                    "The data under the key \"$key\" will be used for every document that has that key."
                                                ) {
                                                    processingController.setKeyEverywhereIfPossible(key)
                                                }
                                            }
                                        }
                                    }

                                }
                            is DataKeyIsArray -> label("global array")
                            is DataKeyNoneFound -> label("no key found") {
                                style {
                                    textFill = Color.RED
                                }
                            }
                            else -> label("Error")
                        }
                    }

                    readonlyColumn("Delete", JsonDocument::class).cellFormat {
                        graphic = vbox {
                            button("Delete").action { processingController.removeDocument(it.path) }
                        }
                    }

                    smartResize()
                }

                hbox {

                    button("Add source file").action {
                        val files = chooseFile(
                            "Select input files",
                            filters = arrayOf(FileChooser.ExtensionFilter("JSON document", "*.json")),
                            mode = FileChooserMode.Multi
                        )

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
