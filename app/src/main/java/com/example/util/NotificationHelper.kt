package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID_BUDGET_ALERTS = "finpulse_budget_alerts"
    const val CHANNEL_NAME_BUDGET_ALERTS = "FinPulse Budget & Spending Limit Alerts"

    const val CHANNEL_ID_STREAKS = "finpulse_streak_alerts"
    const val CHANNEL_NAME_STREAKS = "FinPulse Daily Streaks & Milestones"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_ID_BUDGET_ALERTS,
                CHANNEL_NAME_BUDGET_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical warnings when nearing or exceeding monthly spending limits"
                enableVibration(true)
            }

            val streakChannel = NotificationChannel(
                CHANNEL_ID_STREAKS,
                CHANNEL_NAME_STREAKS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Habit streak reminders and badge milestone celebration alerts"
            }

            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(streakChannel)
        }
    }

    fun showBudgetLimitAlert(
        context: Context,
        categoryName: String,
        spentAmount: Double,
        limitAmount: Double,
        percent: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isExceeded = spentAmount >= limitAmount
        val title = if (isExceeded) {
            "🚨 Spending Limit Exceeded: $categoryName"
        } else {
            "⚠️ Budget Alert ($percent%): $categoryName"
        }

        val message = if (isExceeded) {
            "You spent $${"%.2f".format(spentAmount)} of your $${"%.2f".format(limitAmount)} monthly limit. FinPulse AI recommends pacing down non-essential purchases."
        } else {
            "You have reached $percent% of your $${"%.2f".format(limitAmount)} budget for $categoryName ($${"%.2f".format(spentAmount)} spent)."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = categoryName.hashCode()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted by user, gracefully handled
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun showStreakMilestoneAlert(
        context: Context,
        streakDays: Int,
        xpAwarded: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "🔥 $streakDays-Day Habit Streak Achieved!"
        val message = "Incredible discipline! You just earned +$xpAwarded XP for tracking your expenses today. Keep the fire burning!"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_STREAKS)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        } catch (e: Exception) {
            // Fallback
        }
    }
}
