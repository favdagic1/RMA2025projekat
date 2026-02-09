package com.favdagic.newsfeedapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.favdagic.newsfeedapp.model.News
import com.favdagic.newsfeedapp.model.NewsTags
import com.favdagic.newsfeedapp.model.Tags

@Database(
    entities = [News::class, Tags::class, NewsTags::class],
    version = 1
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO
}