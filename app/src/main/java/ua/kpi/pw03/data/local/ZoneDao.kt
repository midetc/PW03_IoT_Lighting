package ua.kpi.pw03.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ZoneDao {
    @Query("SELECT * FROM zones ORDER BY name")
    suspend fun all(): List<ZoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ZoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ZoneEntity)

    @Query("DELETE FROM zones")
    suspend fun clear()
}
