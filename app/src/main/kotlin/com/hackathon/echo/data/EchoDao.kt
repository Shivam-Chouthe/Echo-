package com.hackathon.echo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EchoDao {
    @Query("SELECT * FROM echo_items ORDER BY createdAt DESC")
    fun getAllEchoes(): Flow<List<EchoItem>>

    @Query("SELECT * FROM echo_items WHERE id = :id LIMIT 1")
    suspend fun getEchoById(id: Int): EchoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(echoItem: EchoItem): Long

    @Delete
    suspend fun delete(echoItem: EchoItem)
}
