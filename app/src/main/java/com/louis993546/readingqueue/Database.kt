package com.louis993546.readingqueue

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val url: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "is_queued") val isQueued: Boolean,
)

@Dao
interface ContentDao {
    @Query("SELECT * FROM content")
    fun getAll(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE is_favorite = 1")
    fun getFavorites(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE is_queued = 1")
    fun getQueued(): List<ContentEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // assume this is sth user has saved already
    fun add(vararg content: ContentEntity)

    @Delete
    fun remove(vararg content: ContentEntity)
}

class ContentRepository(
    private val contentDao: ContentDao,
) {
    fun getAll(): Flow<List<Content>> {
        return contentDao.getAll().map { it.toContentList() }
    }

    private fun List<ContentEntity>.toContentList(): List<Content> {
        return this.map {
            Content(
                id = it.url,
                title = it.url,
                subtitle = "TODO also this needs to be nullable",
            )
        }
    }
}

@Database(entities = [ContentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
}
