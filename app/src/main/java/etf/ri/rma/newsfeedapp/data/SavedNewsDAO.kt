package etf.ri.rma.newsfeedapp.data

import androidx.room.*
import etf.ri.rma.newsfeedapp.model.News
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsTags
import etf.ri.rma.newsfeedapp.model.Tags
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Dao
abstract class SavedNewsDAO {

    @Insert
    protected abstract suspend fun insertNews(entity: News): Long

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1")
    protected abstract suspend fun findNewsByUuid(uuid: String): News?

    @Query("SELECT * FROM News")
    protected abstract suspend fun getAllNewsEntities(): List<News>

    @Query("SELECT * FROM News WHERE category = :category")
    protected abstract suspend fun getNewsByCategoryEntities(category: String): List<News>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertTagEntity(tag: Tags): Long

    @Query("SELECT * FROM Tags WHERE value = :value LIMIT 1")
    protected abstract suspend fun findTagByValue(value: String): Tags?

    @Insert
    protected abstract suspend fun insertNewsTags(crossRef: NewsTags): Long

    @Query("SELECT COUNT(*) FROM NewsTags WHERE newsId = :newsId AND tagsId = :tagId")
    protected abstract suspend fun countNewsTag(newsId: Int, tagId: Int): Int

    @Query(
        "SELECT Tags.value FROM Tags INNER JOIN NewsTags ON Tags.id = NewsTags.tagsId WHERE NewsTags.newsId = :newsId"
    )
    protected abstract suspend fun getTagsForNews(newsId: Int): List<String>

    @Query(
        "SELECT DISTINCT News.* FROM News " +
                "INNER JOIN NewsTags ON News.id = NewsTags.newsId " +
                "INNER JOIN Tags ON Tags.id = NewsTags.tagsId " +
                "WHERE Tags.value IN (:tags)"
    )
    protected abstract suspend fun getNewsByTags(tags: List<String>): List<News>

    /** Adds news item to the database if uuid not already present */
    @Transaction
    open suspend fun saveNews(news: NewsItem): Boolean {
        if (findNewsByUuid(news.uuid) != null) {
            return false
        }
        insertNews(news.toEntity())
        return true
    }

    /** Returns all news items with their tags */
    @Transaction
    open suspend fun allNews(): List<NewsItem> {
        return getAllNewsEntities().map { it.toModel(getTagsForNews(it.id)) }
    }

    /** Returns news by category */
    @Transaction
    open suspend fun getNewsWithCategory(category: String): List<NewsItem> {
        return getNewsByCategoryEntities(category).map { it.toModel(getTagsForNews(it.id)) }
    }

    /** Adds tags to given news id. Returns number of new tag entries */
    @Transaction
    open suspend fun addTags(tags: List<String>, newsId: Int): Int {
        var newCount = 0
        for (tagValue in tags) {
            var tag = findTagByValue(tagValue)
            val tagId: Int = if (tag == null) {
                val id = insertTagEntity(Tags(value = tagValue)).toInt()
                newCount++
                id
            } else {
                tag.id
            }
            if (countNewsTag(newsId, tagId) == 0) {
                insertNewsTags(NewsTags(newsId = newsId, tagsId = tagId))
            }
        }
        return newCount
    }

    /** Returns tag strings for a news item */
    @Transaction
    open suspend fun getTags(newsId: Int): List<String> {
        return getTagsForNews(newsId)
    }

    /** Returns news that share at least one of provided tags sorted by date desc */
    @Transaction
    open suspend fun getSimilarNews(tags: List<String>): List<NewsItem> {
        if (tags.isEmpty()) return emptyList()
        val entities = getNewsByTags(tags)
        return entities
            .map { it.toModel(getTagsForNews(it.id)) }
            .sortedByDescending { it.parsedDate() }
    }

    // Helper conversions
    private fun NewsItem.toEntity(): News = News(
        uuid = uuid,
        title = title,
        snippet = snippet,
        imageUrl = imageUrl,
        category = category,
        isFeatured = isFeatured,
        source = source,
        publishedDate = publishedDate
    )

    private fun News.toModel(tags: List<String>): NewsItem = NewsItem(
        uuid = uuid,
        title = title,
        snippet = snippet,
        imageUrl = imageUrl,
        category = category,
        isFeatured = isFeatured,
        imageTags = ArrayList(tags.map { etf.ri.rma.newsfeedapp.data.ImaggaTag(it) }),

        source = source,
        publishedDate = publishedDate
    )

    private fun NewsItem.parsedDate(): LocalDate {
        return try {
            LocalDate.parse(publishedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (e: Exception) {
            LocalDate.MIN
        }
    }
}