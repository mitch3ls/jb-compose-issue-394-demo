package at.cdfz.jsonsplitter.models

data class FilterItem(val fieldName: String, val valueToBeContained: String) {
    fun check(record: MutableMap<Any?, Any?>): Boolean {
        val value = record[fieldName]

        if (value !is String) {
            return true
        }

        if (value.contains(valueToBeContained)) {
            return true
        }

        return false
    }
}

data class FilterList(val items: Array<FilterItem> = emptyArray()) {

    fun check(document: MutableMap<Any?, Any?>): Boolean {
        return items.all { it.check(document) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterList

        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}