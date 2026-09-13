package com.kanarek.player

import com.kanarek.data.readBytesCapped
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import org.json.JSONObject

internal data class RadioParadiseMetadata(
    val title: String,
    val artist: String,
    val album: String?,
    val artworkUrl: String?,
    val refreshAfterMillis: Long,
) {
    val displayText: String
        get() = listOf(artist, title).filter(String::isNotBlank).joinToString(" — ")
}

internal fun radioParadiseChannel(streamUrl: String): Int? {
    val uri = runCatching { URI(streamUrl) }.getOrNull()
    val host = uri?.host?.lowercase()
    val isRadioParadise =
        uri?.scheme in setOf("http", "https") &&
            host != null &&
            (host == RADIO_PARADISE_HOST || host.endsWith(".$RADIO_PARADISE_HOST"))
    return if (!isRadioParadise) {
        null
    } else {
        val path = uri.path.orEmpty().lowercase()
        when {
            "rock" in path -> 2
            "global" in path || "world" in path -> 3
            "mellow" in path || path.endsWith("/ogg-192m") -> 1
            else -> 0
        }
    }
}

internal fun parseRadioParadiseMetadata(json: String): RadioParadiseMetadata? {
    val payload = runCatching { JSONObject(json) }.getOrNull() ?: return null
    val title = payload.stringOrNull("title").orEmpty()
    val artist = payload.stringOrNull("artist").orEmpty()
    if (title.isEmpty() && artist.isEmpty()) return null

    val seconds = payload.opt("time")?.toString()?.toDoubleOrNull()?.toLong() ?: DEFAULT_REFRESH_SECONDS
    return RadioParadiseMetadata(
        title = title,
        artist = artist,
        album = payload.stringOrNull("album"),
        artworkUrl =
            sequenceOf("cover_med", "cover", "cover_small")
                .mapNotNull(payload::stringOrNull)
                .mapNotNull(::safeRadioParadiseArtworkUrl)
                .firstOrNull(),
        refreshAfterMillis =
            (seconds + REFRESH_GRACE_SECONDS)
                .coerceIn(MIN_REFRESH_SECONDS, MAX_REFRESH_SECONDS) * 1_000,
    )
}

internal fun fetchRadioParadiseMetadata(channel: Int): RadioParadiseMetadata? {
    if (channel !in 0..3) return null
    val connection =
        (URL("$RADIO_PARADISE_API?chan=$channel").openConnection() as HttpURLConnection).apply {
            connectTimeout = HTTP_TIMEOUT_MS
            readTimeout = HTTP_TIMEOUT_MS
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "kanarek/1.0 (Android)")
        }
    return try {
        if (connection.responseCode !in 200..299) return null
        val json =
            connection.inputStream.use { input ->
                input.readBytesCapped(MAX_METADATA_BYTES).toString(Charsets.UTF_8)
            }
        parseRadioParadiseMetadata(json)
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.stringOrNull(key: String): String? =
    takeUnless { isNull(key) }?.optString(key)?.trim()?.takeIf(String::isNotEmpty)

private fun safeRadioParadiseArtworkUrl(value: String): String? {
    val uri = runCatching { URI(value.trim()) }.getOrNull() ?: return null
    if (uri.scheme != "https" || uri.userInfo != null) return null
    val host = uri.host?.lowercase() ?: return null
    return value.takeIf {
        host == RADIO_PARADISE_HOST || host.endsWith(".$RADIO_PARADISE_HOST")
    }
}

private const val RADIO_PARADISE_HOST = "radioparadise.com"
private const val RADIO_PARADISE_API = "https://api.radioparadise.com/api/now_playing"
private const val HTTP_TIMEOUT_MS = 6_000
private const val MAX_METADATA_BYTES = 128 * 1024
private const val DEFAULT_REFRESH_SECONDS = 30L
private const val REFRESH_GRACE_SECONDS = 2L
private const val MIN_REFRESH_SECONDS = 15L
private const val MAX_REFRESH_SECONDS = 5 * 60L
