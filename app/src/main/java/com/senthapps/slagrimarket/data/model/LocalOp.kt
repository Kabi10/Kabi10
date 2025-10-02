package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
@Entity(tableName = "local_ops")
data class LocalOp(
    @PrimaryKey
    @Json(name = "opId")
    val opId: String,
    
    @Json(name = "type")
    val type: OpType,
    
    @Json(name = "payload")
    val payload: String, // JSON string of the operation payload
    
    @Json(name = "clientTs")
    val clientTs: String = Instant.now().toString(),
    
    @Json(name = "attempts")
    val attempts: Int = 0,
    
    @Json(name = "synced")
    val synced: Boolean = false,
    
    @Json(name = "lastAttemptAt")
    val lastAttemptAt: String? = null,
    
    @Json(name = "errorMessage")
    val errorMessage: String? = null,

    @Json(name = "clientId")
    val clientId: String? = null,

    @Json(name = "entityId")
    val entityId: String? = null
)

enum class OpType {
    @Json(name = "CREATE_LISTING")
    CREATE_LISTING,
    
    @Json(name = "UPDATE_LISTING")
    UPDATE_LISTING,
    
    @Json(name = "DELETE_LISTING")
    DELETE_LISTING,
    
    @Json(name = "CREATE_TRANSACTION")
    CREATE_TRANSACTION,
    
    @Json(name = "UPDATE_TRANSACTION")
    UPDATE_TRANSACTION,
    
    @Json(name = "UPDATE_USER")
    UPDATE_USER
}

// Sync response from server
@JsonClass(generateAdapter = true)
data class SyncResponse(
    @Json(name = "appliedOps")
    val appliedOps: List<String>, // List of opIds that were successfully applied
    
    @Json(name = "conflicts")
    val conflicts: List<ConflictInfo>,
    
    @Json(name = "serverState")
    val serverState: List<ServerStateItem>
)

@JsonClass(generateAdapter = true)
data class ConflictInfo(
    @Json(name = "opId")
    val opId: String,
    
    @Json(name = "reason")
    val reason: String,
    
    @Json(name = "serverObject")
    val serverObject: String? = null // JSON string of server's version
)

@JsonClass(generateAdapter = true)
data class ServerStateItem(
    @Json(name = "type")
    val type: String, // "listing", "transaction", "user"
    
    @Json(name = "id")
    val id: String,
    
    @Json(name = "data")
    val data: String // JSON string of the object
)

// Sync request to server
@JsonClass(generateAdapter = true)
data class SyncRequest(
    @Json(name = "lastSyncAt")
    val lastSyncAt: String?,
    
    @Json(name = "ops")
    val ops: List<LocalOp>
)
