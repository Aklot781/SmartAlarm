package com.example.smartalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Получаем тип задания
        val taskType = intent.getStringExtra("taskType") ?: "math"
        val alarmId = intent.getIntExtra("alarm_id", 0)

        val activityIntent = Intent(context, TaskActivity::class.java).apply {
            putExtra("taskType", taskType)
            putExtra("alarm_id", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(activityIntent)
    }
}