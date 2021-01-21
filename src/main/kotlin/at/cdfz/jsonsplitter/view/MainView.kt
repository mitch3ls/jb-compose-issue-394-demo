package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.controller.*
import javafx.collections.FXCollections
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.util.Duration
import tornadofx.*
import tornadofx.controlsfx.checkcombobox

class MainView : View() {

    val processingController: ProcessingController by inject()

    var dataKeyEdited = false

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
                    val tableView = this
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
                                            if (key != null && dataKeyEdited == false) {
                                                dataKeyEdited = true
                                                confirm(
                                                    "Do you want to change to data key for every similar document?",
                                                    "The data under the key \"$key\" will be used for every document that has that key."
                                                ) {
                                                    processingController.setKeyEverywhereIfPossible(key)
                                                }
                                                runLater {
                                                    dataKeyEdited = false
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

                    column("Id Generation", JsonDocument::idGenerationStateProperty).cellFormat {
                        graphic = when (it) {
                            is IdGenerationInit -> label("")
                            is IdGenerationProcessing -> label("")
                            is IdGenerationDisabled -> checkbox("Generate ID") {
                                this.selectedProperty().onChange { isTicked ->
                                    if (isTicked) {
                                        it.document.idGenerationState =
                                            IdGenerationEnabled(
                                                "".toProperty(),
                                                it.document.getAvailableFields(),
                                                FXCollections.observableArrayList<String>(),
                                                it.document
                                            )

                                        runLater(Duration.millis(200.0)) {
                                            // force eventual adjustment to new cell height
                                            tableView.requestResize()
                                        }
                                    }
                                }
                            }
                            is IdGenerationEnabled -> vbox(spacing = 5) {
                                checkbox("Generate ID") {
                                    // textbox is ticked -> ID generation is enabled here
                                    selectedProperty().set(true)

                                    this.selectedProperty().onChange { isTicked ->
                                        if (isTicked == false) {
                                            it.document.idGenerationState = IdGenerationDisabled(it.document)

                                            runLater(Duration.millis(200.0)) {
                                                // force eventual adjustment to new cell height
                                                tableView.requestResize()
                                            }
                                        }
                                    }
                                }

                                vbox {
                                    label("name of id field:")
                                    textfield {
                                        textProperty().bindBidirectional(it.idField)
                                    }
                                }


                                vbox {
                                    label("fields to hash")
                                    checkcombobox(it.availableFields.asObservable(), it.hashFields)
                                }

                            }
                            else -> label("Error")
                        }
                    }

                    readonlyColumn("Delete", JsonDocument::path).cellFormat { path ->
                        graphic = vbox {
                            button("Delete").action { processingController.removeDocument(path) }
                        }
                    }

                    columnResizePolicy = SmartResize.POLICY
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

                hbox {

                    spacing = 10.0

                    textfield {

                        hgrow = Priority.ALWAYS

                        textProperty().bindBidirectional(processingController.destinationPath)
                    }

                    button("choose").action {
                        val directory = chooseDirectory("Choose an output folder") ?: return@action

                        processingController.destinationPath.set(directory.canonicalPath)
                    }
                }
            }

            button("Split files") {
                disableWhen(
                    processingController.destinationPath.isEmpty
                    .or(booleanBinding(processingController.documents) { isEmpty() }))

                action {
                    println("split")
                }
            }
        }
    }
}
