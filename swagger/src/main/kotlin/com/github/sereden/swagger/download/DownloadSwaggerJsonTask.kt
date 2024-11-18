package com.github.sereden.swagger.download

import com.github.sereden.swagger.plugin.SwaggerExtensions

class DownloadSwaggerJsonTask(
    private val extension: SwaggerExtensions
) {
    private val downloadFileManager = DownloadFileManager()

    fun execute() {
        val serverUrl =
            extension.serverUrl ?: throw IllegalStateException("The serverUrl must be provided in the plugin")

        val destination =
            extension.path ?: throw IllegalStateException("The path of the JSON file must be provided in the plugin")
        downloadFileManager(
            urlString = serverUrl,
            destination = destination
        )
    }
}