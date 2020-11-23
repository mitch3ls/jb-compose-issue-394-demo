package at.cdfz.jsonsplitter.view

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
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
            style {
                fontSize = 22.px
                fontWeight = FontWeight.BOLD
            }
        }

        vbox {
            alignment = Pos.CENTER

            text("Version: 0.0.1")
            text("Author: Michael Kudler")
        }
    }
}