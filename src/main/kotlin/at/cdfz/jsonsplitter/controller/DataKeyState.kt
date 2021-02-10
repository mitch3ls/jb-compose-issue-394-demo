package at.cdfz.jsonsplitter.controller


sealed class DataKeyState {

    interface HasAvailableRecordFields {
        fun getAvailableFields(): List<String>
    }

    interface ObjectLike {
        val key: String?
    }

    object Init : DataKeyState()
    object Processing : DataKeyState()
    object NoKeyFound : DataKeyState()
    object InvalidDocument : DataKeyState()
    object IncompleteArray : DataKeyState()
    data class Array(val fields: List<String>) : DataKeyState(), HasAvailableRecordFields {
        override fun getAvailableFields() = fields
    }

    data class IncompleteObject(override val key: String?, val allOptions: Map<String, List<String>>) : DataKeyState(),
        HasAvailableRecordFields, ObjectLike {
        override fun getAvailableFields(): List<String> = allOptions[key] ?: emptyList()

    }

    data class Object(override val key: String, val allOptions: Map<String, List<String>>) : DataKeyState(),
        HasAvailableRecordFields, ObjectLike {
        override fun getAvailableFields(): List<String> = allOptions[key] ?: emptyList()

    }
}