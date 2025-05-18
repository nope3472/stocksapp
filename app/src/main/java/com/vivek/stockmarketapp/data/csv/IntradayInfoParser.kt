package com.vivek.stockmarketapp.data.csv

import android.util.Log
import com.vivek.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.opencsv.CSVReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class IntradayInfoParser @Inject constructor() : CSVParser<IntradayInfoDto> {
    override suspend fun parse(stream: InputStream): List<IntradayInfoDto> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return try {
            csvReader.readAll().drop(1).mapNotNull { line ->
                try {
                    if (line.size < 6) {
                        Log.e("IntradayInfoParser", "Invalid line format: ${line.joinToString()}")
                        return@mapNotNull null
                    }

                    val timestamp = line[0]
                    val high = line[2].toDoubleOrNull()
                    val low = line[3].toDoubleOrNull()

                    if (high == null || low == null) {
                        Log.e("IntradayInfoParser", "Invalid price values: high=$high, low=$low")
                        return@mapNotNull null
                    }

                    IntradayInfoDto(
                        timestamp = timestamp,
                        high = high,
                        low = low
                    )
                } catch (e: Exception) {
                    Log.e("IntradayInfoParser", "Error parsing line: ${line.joinToString()}", e)
                    null
                }
            }.also { parsedData ->
                Log.d("IntradayInfoParser", "Successfully parsed ${parsedData.size} entries")
                if (parsedData.isEmpty()) {
                    Log.e("IntradayInfoParser", "No data was parsed from the CSV")
                } else {
                    Log.d("IntradayInfoParser", "First entry: ${parsedData.first()}")
                }
            }
        } catch (e: Exception) {
            Log.e("IntradayInfoParser", "Error reading CSV", e)
            emptyList()
        } finally {
            try {
                csvReader.close()
            } catch (e: Exception) {
                Log.e("IntradayInfoParser", "Error closing CSV reader", e)
            }
        }
    }
}