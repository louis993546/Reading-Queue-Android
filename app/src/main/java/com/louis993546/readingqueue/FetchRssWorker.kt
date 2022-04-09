package com.louis993546.readingqueue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import timber.log.Timber

class FetchRssWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val db: AppDatabase by lazy { getDatabase(appContext) }
    private val rssFeedRepository by lazy {
        RssFeedRepository(db.rssFeedDao())
    }

    override suspend fun doWork(): Result {
        rssFeedRepository
            .getAll()
            .forEach {
                Timber.tag("qqq from FRW").d(it.toString())
            }

        return Result.success()
    }

}
