package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.MatchRecord

@Entity(tableName = "match_history")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val modeSummary: String,
    val predictionsSummary: String,
    val winnersSummary: String,
    val pointsEarned: Int
) {
    fun toModel(): MatchRecord = MatchRecord(
        id = id,
        timestamp = timestamp,
        modeSummary = modeSummary,
        predictionsSummary = predictionsSummary,
        winnersSummary = winnersSummary,
        pointsEarned = pointsEarned
    )

    companion object {
        fun fromModel(record: MatchRecord): MatchRecordEntity = MatchRecordEntity(
            timestamp = record.timestamp,
            modeSummary = record.modeSummary,
            predictionsSummary = record.predictionsSummary,
            winnersSummary = record.winnersSummary,
            pointsEarned = record.pointsEarned
        )
    }
}
