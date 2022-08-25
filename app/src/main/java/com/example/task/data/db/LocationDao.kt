package com.example.task.data.db

import androidx.room.*
import com.example.task.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    suspend fun updateLocation(location: Location) {
        location.let {
            insertLocation(it)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: Location)

//    @Query("DELETE FROM location_table")
//    suspend fun deleteLocations()

    @Query("SELECT * FROM location_table ORDER BY time")
    fun getLocations(): Flow<List<Location>>
}
