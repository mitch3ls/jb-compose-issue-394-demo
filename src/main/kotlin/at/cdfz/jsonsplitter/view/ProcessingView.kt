package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.controller.*
import tornadofx.*

class ProcessingView : View() {

    val processingController: ProcessingController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()

        processingController.startProcessing()
    }

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            println("Closing")
        }
    }

    override val root = vbox {

        setPrefSize(600.0, 400.0)

        vbox {
            spacing = 10.0
            paddingAll = 10.0

            vbox {
                tableview(processingController.documents) {
                    val tableView = this
                    useMaxWidth = true

                    readonlyColumn("Path", JsonDocument::path)

                    column("Progress", JsonDocument::processingStateProperty).cellFormat {
                        graphic = when (it) {
                            is ProcessingInit -> label("Init")
                            is ProcessingProgress -> progressbar(it.progress)
                            is ProcessingDone -> label("Done")
                            is ProcessingError -> label("ERROR")
                            else -> label("ERROR")
                        }
                    }

                    columnResizePolicy = SmartResize.POLICY
                }
            }
        }
    }

}
