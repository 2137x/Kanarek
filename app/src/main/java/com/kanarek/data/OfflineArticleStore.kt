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

    internal data class StagedPayload(
        val temp: File,
        val target: File,
    )

    fun read(expected: SavedArticleRecord): OfflineArticleContent? {
        val id = ArticleStates.id(expected.item)
        if (id.isBlank()) return null
        val file = File(directory, fileName(id))
        if (!file.isFile) return null
        val stored = runCatching { SavedArticleCodec.decodeRecord(file.readText(UTF_8)) }.getOrNull()
        val offline =
            stored
                ?.takeIf {
                    ArticleStates.id(it.item) == id &&
                        it.item == expected.item &&
                        it.savedAtMillis == expected.savedAtMillis
                }?.offline
        if (offline == null) file.delete()
        return offline
    }

    fun stage(records: Iterable<SavedArticleRecord>): List<StagedPayload> {
        val offlineRecords = records.filter { it.offline != null && ArticleStates.id(it.item).isNotBlank() }
        if (offlineRecords.isEmpty()) return emptyList()
        ensureDirectory()
        val staged = mutableListOf<StagedPayload>()
        try {
            offlineRecords.forEach { record ->
                val target = File(directory, fileName(ArticleStates.id(record.item)))
                val temp = File.createTempFile(".pending-${target.name}.", ".tmp", directory)
                staged += StagedPayload(temp = temp, target = target)
                temp.writeText(SavedArticleCodec.encodeRecord(record), UTF_8)
            }
        } catch (error: Exception) {
            discard(staged)
            throw error
        }
        return staged
    }

    fun publish(staged: Iterable<StagedPayload>) {
        staged.forEach { payload -> moveAtomically(payload.temp, payload.target) }
    }

    fun discard(staged: Iterable<StagedPayload>) {
        staged.forEach { payload -> if (payload.temp.exists()) payload.temp.delete() }
    }

    fun writeMigration(records: Iterable<SavedArticleRecord>) {
        val staged = stage(records)
        try {
            publish(staged)
        } finally {
            discard(staged)
        }
    }

    fun pruneTo(records: Iterable<SavedArticleRecord>) {
        val keep =
            records
                .filter { it.offline != null }
                .mapTo(mutableSetOf()) { fileName(ArticleStates.id(it.item)) }
        cleanupPending()
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(SAVED_SUFFIX) && file.name !in keep) file.delete()
        }
    }

    /** Recover committed staging files, then remove payloads outside the compact DataStore snapshot. */
    fun reconcile(records: Iterable<SavedArticleRecord>) {
        val expectedByFile =
            records.associateBy { record -> fileName(ArticleStates.id(record.item)) }

        directory.listFiles()?.filter { it.isFile && !it.name.endsWith(SAVED_SUFFIX) }?.forEach { file ->
            val stored = runCatching { SavedArticleCodec.decodeRecord(file.readText(UTF_8)) }.getOrNull()
            val targetName = stored?.let { fileName(ArticleStates.id(it.item)) }
            val expected = targetName?.let(expectedByFile::get)
            val recoverable =
                stored?.offline != null &&
                    expected != null &&
                    stored.item == expected.item &&
                    stored.savedAtMillis == expected.savedAtMillis
            if (recoverable) {
                moveAtomically(file, File(directory, targetName))
            } else {
                file.delete()
            }
        }

        directory.listFiles()?.filter { it.isFile && it.name.endsWith(SAVED_SUFFIX) }?.forEach { file ->
            val expected = expectedByFile[file.name]
            val stored = runCatching { SavedArticleCodec.decodeRecord(file.readText(UTF_8)) }.getOrNull()
            val valid =
                expected != null &&
                    stored?.offline != null &&
                    stored.item == expected.item &&
                    stored.savedAtMillis == expected.savedAtMillis
            if (!valid) file.delete()
        }
    }

    fun clear() {
        StorageFiles.clearDirectory(directory)
    }

    private fun cleanupPending() {
        directory.listFiles()?.forEach { file ->
            if (file.isFile && !file.name.endsWith(SAVED_SUFFIX)) file.delete()
        }
    }

    private fun ensureDirectory() {
        if (!directory.exists() && !directory.mkdirs()) error("Could not create offline article directory")
    }

    private fun moveAtomically(
        source: File,
        target: File,
    ) {
        try {
            Files.move(source.toPath(), target.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath(), REPLACE_EXISTING)
        }
    }

    private fun fileName(articleId: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(articleId.toByteArray(UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) } + SAVED_SUFFIX

    companion object {
        private const val DIR = "offline-articles"
        private const val SAVED_SUFFIX = ".saved"

        internal fun directory(context: Context): File = File(context.filesDir, DIR)
    }
}
