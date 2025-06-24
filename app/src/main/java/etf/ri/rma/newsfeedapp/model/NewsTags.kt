package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "NewsTags",
    foreignKeys = [
        ForeignKey(
            entity = News::class,
            parentColumns = ["id"],
            childColumns = ["newsId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tags::class,
            parentColumns = ["id"],
            childColumns = ["tagsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("newsId"), Index("tagsId")]
)
data class NewsTags(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val newsId: Int,
    val tagsId: Int
)