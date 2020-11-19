package at.cdfz.jsonsplitter.view

import at.cdfz.jsonsplitter.Styles
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}
