package com.louis993546.readingqueue

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val url: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "is_queued") val isQueued: Boolean,
    @ColumnInfo(name = "read") val read: Boolean,
    @ColumnInfo(name = "origin") val origin: Origin,
    @ColumnInfo(name = "origin_rss_feed_id") val originRssFeed: String?
) {
    enum class Origin {
        RssFeed, ReadLater, Email
    }
}

class ContentEntityOriginConverter {
    @TypeConverter
    fun fromEnum(origin: ContentEntity.Origin?): String? = origin?.name

    @TypeConverter
    fun toEnum(string: String?): ContentEntity.Origin? = string?.let {
        ContentEntity.Origin.valueOf(it)
    }
}

@Entity(tableName = "rss_feed")
data class RssFeedEntity(
    @PrimaryKey val url: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
)

@Dao
interface ContentDao {
    @Query("SELECT * FROM content")
    fun getAll(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE is_favorite = 1")
    fun getFavorites(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE is_queued = 1")
    fun getQueued(): Flow<List<ContentEntity>>

    @Update
    suspend fun update(vararg content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE) // assume this is sth user has saved already
    suspend fun add(vararg content: ContentEntity)

    @Delete
    suspend fun remove(vararg content: ContentEntity)
}

@Dao
interface RssFeedDao {
    @Query("SELECT * FROM rss_feed")
    suspend fun getAll(): List<RssFeedEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(vararg rssFeeds: RssFeedEntity)

    @Delete
    suspend fun remove(vararg rssFeeds: RssFeedEntity)
}

class ContentRepository(
    private val contentDao: ContentDao,
) {
    fun getAll(): Flow<List<Content>> = contentDao.getAll().map { it.toContentList() }

    fun getQueued(): Flow<List<Content>> = contentDao.getQueued().map { it.toContentList() }

    fun getFavorite(): Flow<List<Content>> = contentDao.getFavorites().map { it.toContentList() }

    private fun List<ContentEntity>.toContentList(): List<Content> = this.map {
        Content(
            id = it.url,
            title = it.url,
            subtitle = "TODO also this needs to be nullable",
        )
    }

    suspend fun add(contents: List<Content>) {
        contentDao.add(*contents.map { it.toContentEntity() }.toTypedArray())
    }

    private fun Content.toContentEntity(): ContentEntity = ContentEntity(
        url = id,
        isFavorite = false,
        isQueued = false,
        origin = ContentEntity.Origin.RssFeed,
        originRssFeed = "",
        read = false,
    )
}

class RssFeedRepository(
    private val rssFeedDao: RssFeedDao,
) {
    suspend fun getAll(): List<RssFeed> = rssFeedDao.getAll().map { it.toRssFeed() }
}

data class RssFeed(
    val url: String,
    val name: String,
)

private fun RssFeedEntity.toRssFeed() = RssFeed(
    url = this.url,
    name = this.name,
)

@Database(
    entities = [
        ContentEntity::class,
        RssFeedEntity::class,
    ],
    version = 5,
)
@TypeConverters(
    ContentEntityOriginConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    abstract fun rssFeedDao(): RssFeedDao
}

fun getDatabase(appContext: Context): AppDatabase = Room.databaseBuilder(
    appContext,
    AppDatabase::class.java,
    "reading-queue"
).fallbackToDestructiveMigration() // TODO debug only
    .build()