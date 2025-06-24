package etf.ri.rma.newsfeedapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import etf.ri.rma.newsfeedapp.model.News
import etf.ri.rma.newsfeedapp.model.NewsTags
import etf.ri.rma.newsfeedapp.model.Tags

@Database(
    entities = [News::class, Tags::class, NewsTags::class],
    version = 1
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO
}