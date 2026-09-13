package com.kanarek.data

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineArticleStoreTest {
    @Test
    fun stagesPublishesReadsAndPrunesOfflinePayloads() {
        val directory = Files.createTempDirectory("kanarek-offline-articles").toFile()
        try {
            val store = OfflineArticleStore(directory)
            val first = record("https://example.com/first", "First body")
            val second = record("https://example.com/second", "Second body")
            val staged = store.stage(listOf(first, second))

            assertNull(store.read(first.copy(offline = null)))
            store.publish(staged)
            store.discard(staged)

            assertEquals(first.offline, store.read(first.copy(offline = null)))
            assertEquals(second.offline, store.read(second.copy(offline = null)))

            store.pruneTo(listOf(first))

            assertEquals(first.offline, store.read(first.copy(offline = null)))
            assertNull(store.read(second.copy(offline = null)))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun reconcileRecoversCommittedStagingAndDropsUnreferencedPayloads() {
        val directory = Files.createTempDirectory("kanarek-offline-reconcile").toFile()
        try {
            val store = OfflineArticleStore(directory)
            val committed = record("https://example.com/committed", "Committed body")
            val orphan = record("https://example.com/orphan", "Orphan body")
            store.stage(listOf(committed, orphan))

            store.reconcile(listOf(committed.copy(offline = null)))

            assertEquals(committed.offline, store.read(committed.copy(offline = null)))
            assertNull(store.read(orphan.copy(offline = null)))
            assertEquals(1, directory.listFiles()?.count { it.isFile } ?: 0)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun readRejectsPayloadForDifferentSavedSnapshot() {
        val directory = Files.createTempDirectory("kanarek-offline-snapshot").toFile()
        try {
            val store = OfflineArticleStore(directory)
            val saved = record("https://example.com/saved", "Offline body")
            val staged = store.stage(listOf(saved))
            store.publish(staged)
            store.discard(staged)

            assertNull(store.read(saved.copy(savedAtMillis = saved.savedAtMillis + 1, offline = null)))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun clearRemovesAllOfflinePayloads() {
        val directory = Files.createTempDirectory("kanarek-offline-articles").toFile()
        try {
            val store = OfflineArticleStore(directory)
            val saved = record("https://example.com/saved", "Offline body")
            val staged = store.stage(listOf(saved))
            store.publish(staged)
            store.discard(staged)

            store.clear()

            assertNull(store.read(saved.copy(offline = null)))
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun record(
        link: String,
        content: String,
    ): SavedArticleRecord =
        SavedArticleRecord(
            item = NewsItem(
                title = "Saved",
                link = link,
                summary = "Summary",
                imageUrl = null,
                source = "Example",
                publishedAtMillis = 10L,
            ),
            savedAtMillis = 20L,
            offline = OfflineArticleContent(
                title = "Offline",
                author = "Author",
                imageUrl = null,
                content = content,
                wordCount = 2,
                storedAtMillis = 30L,
            ),
        )
}
