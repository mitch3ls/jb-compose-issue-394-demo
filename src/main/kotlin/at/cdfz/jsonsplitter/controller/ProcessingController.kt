package at.cdfz.jsonsplitter.controller

import tornadofx.*

class ProcessingController : Controller() {
    val documents = ArrayList<JsonDocument>().asObservable()

    fun addDocument(document: JsonDocument) {
        documents.add(document)
    }

    fun removeDocument(path: String) {
        documents.removeIf { it.path.equals(path) }
    }
}