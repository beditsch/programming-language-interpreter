package shared

class Position(
    private var line: Long,
    private var column: Long
) {
    fun moveLine(distance: Int = 1) { line += distance }
    fun moveColumn(distance: Int = 1) { column += distance; line = 0 }
    override fun toString(): String {
        return "(Line: $line; Column: $column)"
    }
}
