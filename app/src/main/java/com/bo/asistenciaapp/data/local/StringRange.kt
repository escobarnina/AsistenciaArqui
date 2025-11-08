package com.bo.asistenciaapp.data.local

data class StringRange(val start: String, val end: String) {
    fun overlaps(other: StringRange): Boolean {
        return start < other.end && end > other.start
    }
}
