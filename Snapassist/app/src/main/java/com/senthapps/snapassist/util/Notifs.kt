package com.senthapps.snapassist.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object Notifs {
  const val CHANNEL_ID = "snapassist"
  private const val CHANNEL_NAME = "Snapassist"
  private const val CHANNEL_DESC = "Foreground & command notifications"

  private fun ensureChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= 26) {
      val nm = ctx.getSystemService(NotificationManager::class.java)
      val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
      ch.description = CHANNEL_DESC
      nm.createNotificationChannel(ch)
    }
  }

  fun foreground(ctx: Context, text: String = "Processing"): Notification {
    ensureChannel(ctx)
    return NotificationCompat.Builder(ctx, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_camera)
      .setContentTitle("Snapassist")
      .setContentText(text)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  fun highPriority(
    ctx: Context,
    title: String,
    text: String,
    contentIntent: PendingIntent? = null
  ): Notification {
    ensureChannel(ctx)
    val b = NotificationCompat.Builder(ctx, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_camera)
      .setContentTitle(title)
      .setContentText(text)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
    contentIntent?.let { b.setContentIntent(it) }
    return b.build()
  }
}