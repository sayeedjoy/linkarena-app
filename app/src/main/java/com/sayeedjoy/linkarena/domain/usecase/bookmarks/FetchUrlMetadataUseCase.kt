package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.UrlMetadata
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI
import javax.inject.Inject
import javax.inject.Named

class FetchUrlMetadataUseCase @Inject constructor(
    @Named("metadata") private val okHttpClient: OkHttpClient
) {
    suspend operator fun invoke(rawUrl: String): NetworkResult<UrlMetadata> = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeUrl(rawUrl)
            ?: return@withContext NetworkResult.Error("Enter a valid URL")

        val request = Request.Builder()
            .url(normalizedUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; LinkArena) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
            )
            .get()
            .build()

        return@withContext try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use NetworkResult.Error("Failed to fetch page metadata")
                }

                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@use NetworkResult.Error("No metadata found")
                }

                val finalUrl = response.request.url.toString()
                val doc = Jsoup.parse(body, finalUrl)

                val title = doc.select("meta[property=og:title]").attr("content").takeIf { it.isNotBlank() }
                    ?: doc.title().takeIf { it.isNotBlank() }
                val description = doc.select("meta[property=og:description]").attr("content").takeIf { it.isNotBlank() }
                    ?: doc.select("meta[name=description]").attr("content").takeIf { it.isNotBlank() }
                val faviconUrl = doc.select("link[rel~=(?i)icon]").attr("abs:href").takeIf { it.isNotBlank() }
                    ?: doc.select("link[rel~=(?i)icon]").attr("href").takeIf { it.isNotBlank() }
                        ?.resolveAgainst(finalUrl)

                if (title.isNullOrBlank() && description.isNullOrBlank() && faviconUrl.isNullOrBlank()) {
                    NetworkResult.Error("No metadata found")
                } else {
                    NetworkResult.Success(
                        UrlMetadata(
                            normalizedUrl = finalUrl,
                            title = title?.cleanText(),
                            description = description?.cleanText(),
                            faviconUrl = faviconUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Metadata fetch failed")
        }
    }

    private fun normalizeUrl(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        val candidate = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return try {
            val uri = URI(candidate)
            if (uri.host.isNullOrBlank()) null else uri.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun String.cleanText(): String {
        return replace(Regex("\\s+"), " ").trim()
    }

    private fun String.resolveAgainst(baseUrl: String): String {
        return try {
            val uri = URI(this)
            when {
                uri.isAbsolute -> uri.toString()
                startsWith("//") -> "https:$this"
                else -> URI(baseUrl).resolve(this).toString()
            }
        } catch (e: Exception) {
            this
        }
    }
}
