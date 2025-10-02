package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
    
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
