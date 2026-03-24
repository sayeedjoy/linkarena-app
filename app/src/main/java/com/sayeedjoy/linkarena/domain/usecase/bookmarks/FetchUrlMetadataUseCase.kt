package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.UrlMetadata
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import javax.inject.Inject

class FetchUrlMetadataUseCase @Inject constructor(
    private val okHttpClient: OkHttpClient
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
                val title = extractMetaContent(body, "property", "og:title")
                    ?: extractTitle(body)
                val description = extractMetaContent(body, "property", "og:description")
                    ?: extractMetaContent(body, "name", "description")
                val faviconRaw = extractFaviconHref(body)
                val faviconUrl = faviconRaw?.resolveAgainst(finalUrl)

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

    private fun extractTitle(html: String): String? {
        val match = Regex("(?is)<title[^>]*>(.*?)</title>").find(html) ?: return null
        return match.groupValues.getOrNull(1)
    }

    private fun extractMetaContent(html: String, attrName: String, attrValue: String): String? {
        val quotedAttrValue = Regex.escape(attrValue)
        val attrPattern = "$attrName\\s*=\\s*['\"]$quotedAttrValue['\"]"
        val contentPattern = "content\\s*=\\s*['\"](.*?)['\"]"

        val regex = Regex("(?is)<meta[^>]*$attrPattern[^>]*$contentPattern[^>]*>")
        val directMatch = regex.find(html)?.groupValues?.getOrNull(1)
        if (!directMatch.isNullOrBlank()) return directMatch

        val reverseRegex = Regex("(?is)<meta[^>]*$contentPattern[^>]*$attrPattern[^>]*>")
        return reverseRegex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun extractFaviconHref(html: String): String? {
        val iconRegex = Regex(
            "(?is)<link[^>]*rel\\s*=\\s*['\"][^'\"]*icon[^'\"]*['\"][^>]*href\\s*=\\s*['\"](.*?)['\"][^>]*>"
        )
        val match = iconRegex.find(html)
            ?: Regex("(?is)<link[^>]*href\\s*=\\s*['\"](.*?)['\"][^>]*rel\\s*=\\s*['\"][^'\"]*icon[^'\"]*['\"][^>]*>")
                .find(html)
        return match?.groupValues?.getOrNull(1)
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
