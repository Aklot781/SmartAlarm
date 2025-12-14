package com.example.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalarm.databinding.ActivityAlarmListBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AlarmListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmListBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarms = mutableListOf<AlarmItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadAlarms()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при возвращении на экран
        loadAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            alarms = alarms,
            onEditClick = { alarm -> editAlarm(alarm) },
            onDeleteClick = { alarm -> deleteAlarm(alarm) },
            onToggleClick = { alarm -> toggleAlarm(alarm) }
        )

        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@AlarmListActivity)
            adapter = alarmAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadAlarms() {
        val prefs = getSharedPreferences("alarms_list", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("alarms", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        alarms.clear()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            alarms.add(
                AlarmItem(
                    id = obj.getInt("id"),
                    time = obj.getLong("time"),
                    taskType = obj.getString("taskType"),
                    isActive = obj.getBoolean("isActive")
                )
            )
        }
        alarmAdapter.notifyDataSetChanged()

        // Показать сообщение если список пуст
        if (alarms.isEmpty()) {
            binding.tvEmptyList.visibility = android.view.View.VISIBLE
        } else {
            binding.tvEmptyList.visibility = android.view.View.GONE
        }
    }

    private fun saveAlarms() {
        val jsonArray = JSONArray()
        alarms.forEach { alarm ->
            val obj = JSONObject().apply {
                put("id", alarm.id)
                put("time", alarm.time)
                put("taskType", alarm.taskType)
                put("isActive", alarm.isActive)
            }
            jsonArray.put(obj)
        }

        getSharedPreferences("alarms_list", Context.MODE_PRIVATE)
            .edit()
            .putString("alarms", jsonArray.toString())
            .apply()
    }

    private fun setupClickListeners() {
        binding.fabAddAlarm.setOnClickListener {
            startActivity(Intent(this, SetAlarmActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun editAlarm(alarm: AlarmItem) {
        // Открываем SetAlarmActivity с данными будильника для редактирования
        val intent = Intent(this, SetAlarmActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_time", alarm.time)
            putExtra("alarm_task_type", alarm.taskType)
        }
        startActivity(intent)
    }

    private fun deleteAlarm(alarm: AlarmItem) {
        AlertDialog.Builder(this)
            .setTitle("Удалить будильник")
            .setMessage("Вы уверены, что хотите удалить будильник на ${formatTime(alarm.time)}?")
            .setPositiveButton("Удалить") { dialog, _ ->
                // Отменяем будильник в системе
                cancelAlarmInSystem(alarm.id)

                // Удаляем из списка
                alarms.remove(alarm)
                saveAlarms()
                alarmAdapter.notifyDataSetChanged()

                Toast.makeText(this, "Будильник удален", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun toggleAlarm(alarm: AlarmItem) {
        alarm.isActive = !alarm.isActive
        if (alarm.isActive) {
            // Включаем будильник в системе
            setAlarmInSystem(alarm)
            // Проверяем, установился ли будильник
            if (alarm.isActive) {
                Toast.makeText(this, "Будильник включен", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Отключаем будильник в системе
            cancelAlarmInSystem(alarm.id)
            Toast.makeText(this, "Будильник отключен", Toast.LENGTH_SHORT).show()
        }
        saveAlarms()
        alarmAdapter.notifyDataSetChanged()
    }

    private fun setAlarmInSystem(alarm: AlarmItem) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Проверка для Android 12+ (API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                // Показываем диалог с предложением перейти в настройки
                AlertDialog.Builder(this)
                    .setTitle("Разрешение на точные будильники")
                    .setMessage("Для работы будильника необходимо разрешить точное планирование. Перейти в настройки?")
                    .setPositiveButton("Перейти") { dialog, _ ->
                        startActivity(
                            Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                // Не устанавливаем будильник пока нет разрешения
                alarm.isActive = false
                return
            }
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("taskType", alarm.taskType)
            putExtra("alarm_id", alarm.id)
        }

        val pending = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.time,
                pending
            )

            // Проверяем если время уже прошло
            if (alarm.time <= System.currentTimeMillis()) {
                Toast.makeText(this, "Время будильника уже прошло", Toast.LENGTH_SHORT).show()
                alarm.isActive = false
            }
        } catch (e: SecurityException) {
            // Если возникла SecurityException, запрашиваем разрешение
            Toast.makeText(this, "Необходимо разрешение на точные будильники", Toast.LENGTH_LONG).show()
            alarm.isActive = false

            // Для Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(
                    Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                )
            }
        }
    }

    private fun cancelAlarmInSystem(alarmId: Int) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pending)
    }

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    data class AlarmItem(
        val id: Int,
        val time: Long,
        val taskType: String,
        var isActive: Boolean = true
    )
}