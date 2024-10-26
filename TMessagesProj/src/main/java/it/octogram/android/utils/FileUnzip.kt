package it.octogram.android.utils

import android.content.Context
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * A utility object for unzipping files in the background using coroutines.
 *
 * This object provides functions to:
 * - Start unzipping a file.
 * - Check if an unzip operation is running.
 * - Cancel an ongoing unzip operation.
 * - Delete a folder and its contents.
 * - Add listeners to be notified when an unzip operation finishes.
 */
object FileUnzip {

    private val unzipThreads = ConcurrentHashMap<String, Job>()
    private val listeners = ConcurrentHashMap<String, UnzipListener>()

    /**
     * Unzips a file to a specified output directory.
     *
     * This function uses a background thread to perform the unzip operation.
     * If an unzip operation with the same `id` is already running, this function will return immediately.
     * The output directory will be deleted if it already exists before unzipping.
     *
     * @param context The application context.
     * @param id A unique identifier for this unzip operation. Used to prevent multiple unzip operations with the same ID from running concurrently.
     * @param input The input zip file to be unzipped.
     * @param output The output directory where the unzipped files will be placed.
     */
    fun unzipFile(context: Context, id: String, input: File, output: File) {
        if (unzipThreads.containsKey(id)) return
        if (output.exists()) deleteFolder(output)
        val job = CoroutineScope(Dispatchers.IO).launch {
            val unzipThread = UnzipThread(context, output, id)
            unzipThread.unzipFile(input)
            unzipThreads.remove(id)
        }
        unzipThreads[id] = job
    }

    /**
     * Checks if an unzip operation with the given ID is currently running.
     *
     * @param id The unique identifier for the unzip operation.
     * @return `true` if the unzip operation is running, `false` otherwise.
     */
    fun isRunningUnzip(id: String): Boolean {
        return unzipThreads.containsKey(id)
    }

    /**
     * Cancels an ongoing unzip operation with the given ID.
     *
     * @param id The unique identifier for the unzip operation to be canceled.
     */
    fun cancel(id: String) {
        unzipThreads[id]?.cancel()
    }

    /**
     * Recursively deletes a folder and all its contents.
     *
     * This function first iterates through all the files and directories within the given folder.
     * If it encounters a directory, it calls itself recursively to delete the subdirectory.
     * If it encounters a file, it deletes the file directly.
     * Finally, after processing all the contents, it deletes the given folder itself.
     *
     * @param file The File object representing the folder to be deleted.
     */
    fun deleteFolder(file: File) {
        file.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                deleteFolder(child)
            } else {
                child.delete()
            }
        }
        file.delete()
    }

    /**
     * A class representing a thread that performs the unzip operation.
     *
     * @param context The application context.
     * @param output The output directory where the unzipped files will be placed.
     * @param id The unique identifier for this unzip operation.
     */
    private class UnzipThread(
        private val context: Context,
        private val output: File,
        private val id: String
    ) {

        private var wakeLock: PowerManager.WakeLock? = null
        private var isUnzipCanceled = false

        /**
         * Cancels the unzip operation.
         */
        fun cancel() {
            isUnzipCanceled = true
        }

        /**
         * Unzips the input file to the output directory.
         *
         * This function runs in a background thread and uses a wake lock to ensure the device does not go to sleep during the unzip operation.
         *
         * @param inputFile The input zip file to be unzipped.
         */
        suspend fun unzipFile(inputFile: File) = withContext(Dispatchers.IO) {
            try {
                if (!output.exists()) output.mkdir()
                ZipInputStream(FileInputStream(inputFile)).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry
                    while (entry != null) {
                        if (isUnzipCanceled) {
                            zipIn.closeEntry()
                            AndroidUtilities.runOnUIThread { onPostExecute(true) }
                            return@withContext
                        }
                        val target = File(output, entry.name)
                        validateCanonicalPath(target)
                        if (entry.isDirectory) {
                            target.mkdir()
                        } else {
                            BufferedOutputStream(FileOutputStream(target)).use { bos ->
                                val bytesIn = ByteArray(4096)
                                var read: Int
                                while (zipIn.read(bytesIn).also { read = it } != -1) {
                                    if (isUnzipCanceled) {
                                        zipIn.closeEntry()
                                        AndroidUtilities.runOnUIThread { onPostExecute(true) }
                                        return@withContext
                                    }
                                    bos.write(bytesIn, 0, read)
                                }
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                AndroidUtilities.runOnUIThread { onPostExecute(false) }
            } catch (e: IOException) {
                AndroidUtilities.runOnUIThread { onPostExecute(true) }
                e.printStackTrace()
            } catch (e: SecurityException) {
                AndroidUtilities.runOnUIThread { onPostExecute(true) }
                e.printStackTrace()
            }
        }

        /**
         * Validates the canonical path of the target file to ensure it is within the output directory.
         *
         * @param target The target file to be validated.
         * @throws SecurityException if the target file is outside the output directory.
         */
        private fun validateCanonicalPath(target: File) {
            val canonicalPath = target.canonicalPath
            if (!canonicalPath.startsWith(output.absolutePath)) {
                throw SecurityException("Invalid path: $canonicalPath")
            }
        }

        /**
         * Acquires a wake lock to ensure the device does not go to sleep during the unzip operation.
         */
        private fun onPreExecute() {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
            wakeLock?.acquire(10 * 60 * 1000L)
        }

        /**
         * Releases the wake lock and notifies listeners that the unzip operation has finished.
         *
         * @param isCanceled `true` if the unzip operation was canceled, `false` otherwise.
         */
        private fun onPostExecute(isCanceled: Boolean) {
            wakeLock?.release()
            if (isCanceled) {
                deleteFolder(output)
            }
            onFinished(id)
        }
    }

    /**
     * Adds a listener to be notified when an unzip operation finishes.
     *
     * @param unzipId The unique identifier for the unzip operation.
     * @param key A unique key to identify the listener.
     * @param listener The listener to be notified when the unzip operation finishes.
     */
    fun addListener(unzipId: String, key: String, listener: UnzipListener) {
        listeners[unzipId + "_" + key] = listener
    }

    /**
     * Notifies all listeners that an unzip operation has finished.
     *
     * @param id The unique identifier for the unzip operation that has finished.
     */
    private fun onFinished(id: String) {
        listeners.forEach { (key, listener) ->
            if (key.split("_")[0] == id) {
                listener.onFinished(id)
            }
        }
    }

    interface UnzipListener {
        fun onFinished(id: String)
    }
}
