package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.Styles
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class AboutView : View() {

    override val root = vbox {
        setPrefSize(300.0, 200.0)
        alignment = Pos.CENTER
        spacing = 10.0

        imageview("owl.png") {
            fitHeight = 100.0
            isPreserveRatio = true
        }

        text("JsonSplitter") {
            style = """
                -fx-font-size: 20;
                -fx-font-weight: bold;
            """.trimIndent()
        }

        vbox {
            alignment = Pos.CENTER

            text("Version: 0.0.1")
            text("Author: Michael Kudler")
        }
    }
}