package nerd.cave.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringWriter
import java.util.*

class CSVWriter(private val headers: Map<String, String>) {
    private val rows: MutableList<Map<String, String?>> = LinkedList()
    private val keys = headers.keys

    fun addRows(vararg rows: Map<String, String?>): CSVWriter {
        this.rows.addAll(rows)
        return this
    }

    fun toCSVString(): String {
        val sw = StringWriter()
        // BOM character for UTF-8
        sw.write("\uFEFF")
        CSVPrinter(sw, CSVFormat.RFC4180)
            .use { printer ->
                printer.printRecord(headers.values)
                rows.map { row -> keys.map { key -> row[key] } }
                    .map { printer.printRecord(*it.toTypedArray()) }
            }
        return sw.toString()
    }


}