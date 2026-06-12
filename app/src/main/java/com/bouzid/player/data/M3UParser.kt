package com.bouzid.player.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader

object M3UParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun parse(url: String): List<Channel> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()
        parseM3U(body)
    }

    private fun parseM3U(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        var line: String?
        var currentName = ""
        var currentLogo = ""
        var currentGroup = ""

        while (reader.readLine().also { line = it } != null) {
            val l = line ?: continue
            if (l.startsWith("#EXTINF:")) {
                val logoMatch = Regex("""tvg-logo="([^"]*)"""").find(l)
                currentLogo = logoMatch?.groupValues?.getOrElse(1) { "" } ?: ""

                val groupMatch = Regex("""group-title="([^"]*)"""").find(l)
                currentGroup = groupMatch?.groupValues?.getOrElse(1) { "" } ?: ""

                val name = l.substringAfterLast(",").trim()
                currentName = name
            } else if (!l.startsWith("#") && l.isNotBlank() && currentName.isNotBlank()) {
                channels.add(Channel(
                    name = currentName,
                    url = l.trim(),
                    logo = currentLogo,
                    group = currentGroup
                ))
                currentName = ""
            }
        }
        return channels
    }
}
