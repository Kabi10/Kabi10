package com.senthapps.slagrimarket.data.sync

import com.senthapps.slagrimarket.data.dao.MessageDao
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.MessageType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRealtimeService @Inject constructor(
    private val supabaseClient: SupabaseClient?,
    private val messageDao: MessageDao
) {
    private val _newMessages = MutableSharedFlow<Message>()
    val newMessages: Flow<Message> = _newMessages.asSharedFlow()

    private var subscriptionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun subscribeToConversation(conversationId: String) {
        if (supabaseClient == null) {
            Timber.w("Supabase client not configured, skipping realtime subscription")
            return
        }

        unsubscribe()

        subscriptionJob = scope.launch {
            try {
                val channel = supabaseClient.channel("messages:$conversationId")

                val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "messages"
                    filter("conversation_id", FilterOperator.EQ, conversationId)
                }

                channel.subscribe()

                changeFlow.collect { change ->
                    try {
                        val record = change.record
                        val message = Message(
                            id = record["id"]?.jsonPrimitive?.content ?: return@collect,
                            conversationId = record["conversation_id"]?.jsonPrimitive?.content ?: conversationId,
                            senderId = record["sender_id"]?.jsonPrimitive?.content ?: "",
                            senderName = "",
                            receiverId = "",
                            content = record["content"]?.jsonPrimitive?.content ?: "",
                            messageType = try {
                                MessageType.valueOf(record["message_type"]?.jsonPrimitive?.content ?: "TEXT")
                            } catch (_: Exception) { MessageType.TEXT },
                            isRead = record["is_read"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                            createdAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                        )

                        messageDao.insertMessage(message)
                        _newMessages.emit(message)
                        Timber.d("Realtime message received in conversation $conversationId")
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing realtime message")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error subscribing to conversation $conversationId")
            }
        }
    }

    fun unsubscribe() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }

    suspend fun disconnect() {
        unsubscribe()
        try {
            supabaseClient?.realtime?.removeAllChannels()
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting realtime")
        }
    }
}
