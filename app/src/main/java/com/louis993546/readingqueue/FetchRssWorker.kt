package com.louis993546.readingqueue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import timber.log.Timber
import java.net.URL

class FetchRssWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val db: AppDatabase by lazy { getDatabase(appContext) }
    private val rssFeedRepository by lazy {
        RssFeedRepository(db.rssFeedDao())
    }
    private val contentRepository by lazy {
        ContentRepository(db.contentDao())
    }

    override suspend fun doWork(): Result {
        val contents = rssFeedRepository
            .getAll()
            .map { feed ->
                Timber.tag("qqq from FRW").d(feed.toString())

                // TODO https://github.com/rometools/rome/issues/276
                SyndFeedInput().build(XmlReader(URL(feed.url))).run {
//                    Timber.tag("qqq for ${feed.name}").d(this.toString())

                    this.entries.map { entry ->
                        Content(
                            id = entry.uri,
                            title = entry.title,
                            subtitle = "TODO",
                        )
                    }
                }
            }.flatten()

        Timber.tag("qqq").d("count = ${contents.size}")
        contentRepository.add(contents)
        Timber.tag("qqq").d("content should be added???")

        return Result.success()
    }

}
