package de.mfst.powerMeter.data.csv

import de.mfst.powerMeter.data.entity.MeterReading
import de.mfst.powerMeter.data.repository.MeterReadingRepository
import de.mfst.powerMeter.data.repository.MeterRepository
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

data class ImportResult(val importedCount: Int, val errors: List<String>)

class CsvManager(
    private val meterReadingRepo: MeterReadingRepository,
    private val meterRepo: MeterRepository
) {
    suspend fun exportToCsv(outputStream: OutputStream) {
        val readings = meterReadingRepo.getAllReadingsForExport()
        val meters = meterRepo.getAllMetersList().associateBy { it.id }

        outputStream.bufferedWriter().use { writer ->
            writer.write("date,meter_id,meter_name,reading")
            writer.newLine()
            for (reading in readings) {
                val meterName = meters[reading.meterId]?.name ?: "Unknown"
                writer.write("${reading.date},${reading.meterId},${meterName},${reading.reading}")
                writer.newLine()
            }
        }
    }

    suspend fun importFromCsv(inputStream: InputStream): ImportResult {
        val lines = inputStream.bufferedReader().readLines()
        if (lines.size <= 1) return ImportResult(0, emptyList())

        val readings = mutableListOf<MeterReading>()
        val errors = mutableListOf<String>()

        for ((index, line) in lines.drop(1).withIndex()) {
            if (line.isBlank()) continue
            try {
                val parts = line.split(",")
                if (parts.size < 4) {
                    errors.add("Line ${index + 2}: expected 4 columns, got ${parts.size}")
                    continue
                }
                val date = LocalDate.parse(parts[0].trim())
                val meterId = parts[1].trim().toLong()
                val reading = parts[3].trim().toDouble()
                readings.add(MeterReading(meterId = meterId, date = date, reading = reading))
            } catch (e: Exception) {
                errors.add("Line ${index + 2}: ${e.message}")
            }
        }

        if (readings.isNotEmpty()) {
            meterReadingRepo.insertAll(readings)
        }
        return ImportResult(readings.size, errors)
    }
}
