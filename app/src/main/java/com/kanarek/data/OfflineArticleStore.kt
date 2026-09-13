package com.kanarek.data

import android.content.Context
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest

/** Durable private storage for the large reader-text part of saved articles. */
internal class OfflineArticleStore internal constructor(
    private val directory: File,
) {
    constructor(context: Context) : this(directory(context))

    fun read(articleId: String): OfflineArticleContent? {
        val id = articleId.trim()
        if (id.isEmpty()) return null
        val file = File(directory, fileName(id))
        if (!file.isFile) return null
        val record =
            runCatching { SavedArticleCodec.decodeRecord(file.readText(UTF_8)) }
                .getOrNull()
        val offline =
            record
                ?.takeIf { ArticleStates.id(it.item) == id }
                ?.offline
        if (offline == null) file.delete()
        return offline
    }

    /** Write desired payloads first; pruning happens only after DataStore commits. */
    fun write(records: Iterable<SavedArticleRecord>) {
        val offlineRecords = records.filter { it.offline != null && ArticleStates.id(it.item).isNotBlank() }
        if (offlineRecords.isEmpty()) return
        if (!directory.exists() && !directory.mkdirs()) {
            error("Could not create offline article directory")
        }
        offlineRecords.forEach { record ->
            val id = ArticleStates.id(record.item)
            writeAtomically(File(directory, fileName(id)), SavedArticleCodec.encodeRecord(record))
        }
    }

    fun pruneTo(records: Iterable<SavedArticleRecord>) {
        val keep =
            records
                .filter { it.offline != null }
                .mapTo(mutableSetOf()) { fileName(ArticleStates.id(it.item)) }
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in keep) file.delete()
        }
    }

    fun clear() {
        StorageFiles.clearDirectory(directory)
    }

    private fun writeAtomically(
        target: File,
        value: String,
    ) {
        val temp = File.createTempFile(".${target.name}.", ".tmp", directory)
        try {
            temp.writeText(value, UTF_8)
            try {
                Files.move(temp.toPath(), target.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp.toPath(), target.toPath(), REPLACE_EXISTING)
            }
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private fun fileName(articleId: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(articleId.toByteArray(UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) } + ".saved"

    companion object {
        private const val DIR = "offline-articles"

        internal fun directory(context: Context): File = File(context.filesDir, DIR)
    }
}
