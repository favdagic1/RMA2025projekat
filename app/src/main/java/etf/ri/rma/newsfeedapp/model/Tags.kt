package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tags")
data class Tags(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val value: String
)