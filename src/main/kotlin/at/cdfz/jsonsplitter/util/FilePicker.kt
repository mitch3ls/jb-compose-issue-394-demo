package at.cdfz.jsonsplitter.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FilePicker {
    fun chooseFolder(message: String): File? {
        val chosenDirectory: File

        val isMacOs = System.getProperty("os.name").toLowerCase().contains("mac")

        if (isMacOs) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true")

            val fd = FileDialog(null as Frame?, message, FileDialog.LOAD)
            fd.isVisible = true

            if (fd.file == null) {
                return null
            }

            System.setProperty("apple.awt.fileDialogForDirectories", "false")
            chosenDirectory = File(fd.directory).resolve(fd.file)
        } else {
            val chooser = JFileChooser()
            chooser.dialogTitle = message
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            //
            // disable the "All files" option.
            //
            chooser.isAcceptAllFileFilterUsed = false
            //
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chosenDirectory = chooser.selectedFile
            } else {
                return null
            }
        }

        return chosenDirectory

    }
}