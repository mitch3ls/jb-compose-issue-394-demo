package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.Styles
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View() {

    override val root = vbox {

        setPrefSize(600.0, 400.0)

        menubar {
            menu("Help") {
                item("About JsonSplitter").action {
                    find<AboutView>().openModal()
                }
            }
        }
    }
}