package com.mohamedshihaa.faselhd

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.Jsoup
import java.net.URI

class FaselHDProvider : MainAPI() {
    override var mainUrl = "https://web185.faselhd.cafe"
    override var name = "FaselHD"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = listOf(
        MainPageData(
            "أفلام", "$mainUrl/movies", "div.MovieItem"
        ),
        MainPageData(
            "مسلسلات", "$mainUrl/series", "div.MovieItem"
        )
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(request.data).document
        val home = doc.select(request.selector).map {
            val url = it.selectFirst("a")?.attr("href") ?: return@map null
            val title = it.selectFirst("h3")?.text() ?: "لا عنوان"
            val poster = it.selectFirst("img")?.attr("data-src") ?: ""
            val type = if (url.contains("/series/")) TvType.TvSeries else TvType.Movie
            MovieSearchResponse(title, url, this.name, type, poster, null, null)
        }.filterNotNull()

        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/$query"
        val doc = app.get(url).document

        return doc.select("div.MovieItem").mapNotNull {
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val title = it.selectFirst("h3")?.text() ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("data-src") ?: ""
            val type = if (href.contains("/series/")) TvType.TvSeries else TvType.Movie
            MovieSearchResponse(title, href, this.name, type, poster, null, null)
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: "No Title"
        val poster = doc.selectFirst("div.poster img")?.attr("data-src")
        val year = doc.selectFirst("span.year")?.text()?.toIntOrNull()

        val isSeries = url.contains("/series/")
        return if (isSeries) {
            val episodes = doc.select("ul.episodes-list li").mapNotNull {
                val epUrl = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val epName = it.selectFirst("a")?.text()?.trim() ?: return@mapNotNull null
                Episode(epUrl, epName)
            }

            TvSeriesLoadResponse(title, url, this.name, TvType.TvSeries, episodes, poster, year)
        } else {
            MovieLoadResponse(title, url, this.name, TvType.Movie, poster, year)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val iframe = doc.selectFirst("iframe")?.attr("src") ?: return false
        val resolvedUrl = if (iframe.startsWith("http")) iframe else URI(mainUrl).resolve(iframe).toString()

        loadExtractor(resolvedUrl, mainUrl, subtitleCallback, callback)
        return true
    }
}
