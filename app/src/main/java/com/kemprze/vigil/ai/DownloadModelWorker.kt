package com.kemprze.vigil.ai

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kemprze.vigil.data.SettingsDataStore
import java.io.File

class DownloadModelWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val settingsDataStore = SettingsDataStore(applicationContext)
    companion object {
        const val KEY_VARIANT = "model_variant"
        const val URL_E2B = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.litertlm"
        const val URL_E4B = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it-web.litertlm"

        const val FILE_E2B = "gemma4-e2b-full.litertlm"
        const val FILE_E4B = "gemma4-e4b-full.litertlm"

        const val KEY_PROGRESS = "progress"

        fun modelFile(context: Context, variant: String) = File(context.getExternalFilesDir(null), if (variant == "E4B") FILE_E4B else FILE_E2B)
    }


    override suspend fun doWork(): Result {
        val variant = inputData.getString(KEY_VARIANT) ?: "E2B"
        val url = if (variant == "E4B") URL_E4B else URL_E2B
        val outputFile = modelFile(applicationContext, variant)
        val tempFile = File(applicationContext.getExternalFilesDir(null), "model_download.tmp")

        return try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connect()

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use {
                input ->
                tempFile.outputStream().use {
                    output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also {bytesRead = it} != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val progress = (downloadedBytes * 100 / totalBytes).toInt()
                            setProgress(workDataOf(KEY_PROGRESS to progress))
                        }
                    }
                }
            }
            tempFile.renameTo(outputFile)
            settingsDataStore.saveAiModelReady(true)

            Result.success()

        } catch (e: Exception) {
            android.util.Log.e("DownloadWorker", "Download failed", e)
            tempFile.delete()
            Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))

        }
    }
}