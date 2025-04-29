package com.example.week6

import android.content.Context
import androidx.room.*
import java.util.Date

// Tambahkan TypeConverter untuk Date
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

// Naikkan version jika skema berubah
@Database(
    entities = [User::class, Community::class, Post::class, Comment::class, Vote::class],
    version = 2, // Versi dinaikkan karena ada entity baru
    exportSchema = false
)
@TypeConverters(Converters::class) // Daftarkan TypeConverter
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun communityDao(): CommunityDao // Tambahkan DAO baru
    abstract fun postDao(): PostDao         // Tambahkan DAO baru
    abstract fun commentDao(): CommentDao     // Tambahkan DAO baru
    abstract fun voteDao(): VoteDao         // Tambahkan DAO baru

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reddid_app_database"
                )
                    // Gunakan fallbackToDestructiveMigration untuk pengembangan
                    // Di produksi, implementasikan migrasi yang benar
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}