package com.kanarek.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Files

class OfflineArticleStoreTest {
    @Test
    fun writesReadsAndPrunesOfflinePayloads() {
        val directory = Files.createTempDirectory("kanarek-offline-articles").toFile()
        try {
            val store = OfflineArticleStore(directory)
            val first = record("https://example.com/first", "First body")
            val second = record("https://example.com/second", "Second body")

            store.write(listOf(first, second))

            assertEquals(first.offline, store.read(ArticleStates.id(first.item)))
            assertEquals(second.offline, store.read(ArticleStates.id(second.item)))

            store.pruneTo(listOf(first))

            assertEquals(first.offline, store.read(ArticleStates.id(first.item)))
            assertNull(store.read(ArticleStates.id(second.item)))
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
            store.write(listOf(saved))

            store.clear()

            assertNull(store.read(ArticleStates.id(saved.item)))
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun record(
        link: String,
        content: String,
    ): SavedArticleRecord =
        SavedArticleRecord(
            item =
                NewsItem(
                    title = "Saved",
                    link = link,
                    summary = "Summary",
                    imageUrl = null,
                    source = "Example",
                    publishedAtMillis = 10L,
                ),
            savedAtMillis = 20L,
            offline =
                OfflineArticleContent(
                    title = "Offline",
                    author = "Author",
                    imageUrl = null,
                    content = content,
                    wordCount = 2,
                    storedAtMillis = 30L,
                ),
        )
}
