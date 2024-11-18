package com.github.sereden.swagger.download

import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI

class DownloadFileManager {
    operator fun invoke(
        urlString: String,
        destination: File
    ) {
        try {
            // Create URL object
            val url = URI.create(urlString).toURL()
            val connection = url.openConnection() as HttpURLConnection

            // Check response code
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to connect: ${connection.responseMessage}")
            }

            // Get input stream
            val inputStream = connection.inputStream

            // Ensure the destination folder exists
            if (!destination.exists()) {
                destination.mkdirs()
            }

            // Save the file
            inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }

            println("File downloaded successfully to: ${destination.absolutePath}")

        } catch (e: Exception) {
            println("Error downloading file: ${e.message}")
        }
    }
}