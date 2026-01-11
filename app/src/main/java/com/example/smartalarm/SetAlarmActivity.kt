package com.example.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivitySetAlarmBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class SetAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetAlarmBinding
    private var isEditMode = false
    private var editAlarmId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        // Проверяем режим редактирования
        isEditMode = intent.getBooleanExtra("edit_mode", false)

        // TimePicker режим 24h
        binding.timePicker.setIs24HourView(true)

        // Обновляем описание при выборе типа задачи
        binding.radioGroupTask.setOnCheckedChangeListener { group, checkedId ->
            updateTaskDescription(checkedId)
        }

        if (isEditMode) {
            // Режим редактирования - загружаем данные будильника
            editAlarmId = intent.getIntExtra("alarm_id", -1)
            val alarmTime = intent.getLongExtra("alarm_time", System.currentTimeMillis())
            val alarmTaskType = intent.getStringExtra("alarm_task_type") ?: "math"

            // Устанавливаем время в TimePicker
            val cal = Calendar.getInstance().apply { timeInMillis = alarmTime }
            binding.timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = cal.get(Calendar.MINUTE)

            // Выбираем RadioButton по типу задачи
            when (alarmTaskType) {
                "math" -> binding.rbMath.isChecked = true
                "translate" -> binding.rbTranslate.isChecked = true
                "logic" -> binding.rbLogic.isChecked = true
                "attention" -> binding.rbAttention.isChecked = true
                "find_symbol" -> binding.rbGenerateWordPuzzle.isChecked = true
                "combo" -> binding.rbCombo.isChecked = true
            }

            binding.btnSaveAlarm.text = "Обновить будильник"
        }

        binding.btnSaveAlarm.setOnClickListener {
            val hour: Int = binding.timePicker.hour
            val minute: Int = binding.timePicker.minute

            // Определяем тип задачи
            val selectedRadioId = binding.radioGroupTask.checkedRadioButtonId
            val taskType = if (selectedRadioId != -1) {
                val rb = findViewById<RadioButton>(selectedRadioId)
                rb.tag?.toString() ?: "math"
            } else {
                "math"
            }

            // Создаём Calendar для времени будильника
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Если время в прошлом — ставим на следующий день
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }

            setAlarm(cal.timeInMillis, taskType)
        }

        // Устанавливаем начальное описание
        updateTaskDescription(binding.radioGroupTask.checkedRadioButtonId)
    }

    private fun setupClickListeners() {
        // Кнопка назад в тулбаре
        binding.btnBack.setOnClickListener {
            finish() // Закрывает активность и возвращает назад
        }
    }

    private fun updateTaskDescription(checkedId: Int) {
        val description = when (checkedId) {
            R.id.rbMath -> "Решите математические примеры для отключения будильника"
            R.id.rbTranslate -> "Переведите английские слова на русский язык"
            R.id.rbLogic -> "Продолжите числовые последовательности или найдите лишнее"
            R.id.rbAttention -> "Найдите самое большое число в наборе"
            R.id.rbGenerateWordPuzzle -> "Собери слово из букв"
            R.id.rbCombo -> "Случайный набор разных типов задач"
            else -> "Выберите тип задания"
        }
        binding.tvTaskDescription.text = description
    }

    private fun setAlarm(triggerAtMillis: Long, taskType: String) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Разрешите точные будильники в настройках",
                    Toast.LENGTH_LONG
                ).show()

                startActivity(
                    Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                )
                return
            }
        }

        // Генерируем ID для будильника
        val alarmId = if (isEditMode) editAlarmId else generateAlarmId()

        // Сохраняем будильник в список
        saveToAlarmList(alarmId, triggerAtMillis, taskType)

        // Устанавливаем системный будильник
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("taskType", taskType)
            putExtra("alarm_id", alarmId)
        }

        val pending = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pending
        )

        val message = if (isEditMode) "Будильник обновлен" else "Будильник установлен"
        Toast.makeText(this, "$message на ${formatTime(triggerAtMillis)}", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun generateAlarmId(): Int {
        // Генерируем уникальный ID на основе текущего времени
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    private fun saveToAlarmList(id: Int, time: Long, taskType: String) {
        val prefs = getSharedPreferences("alarms_list", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("alarms", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        if (isEditMode) {
            // Режим редактирования, удаляем старую запись
            for (i in 0 until jsonArray.length()) {
                if (jsonArray.getJSONObject(i).getInt("id") == id) {
                    jsonArray.remove(i)
                    break
                }
            }
        }

        // Добавляем новую/обновленную запись
        val obj = JSONObject().apply {
            put("id", id)
            put("time", time)
            put("taskType", taskType)
            put("isActive", true) // По умолчанию активен
        }
        jsonArray.put(obj)

        // Сохраняем обратно
        prefs.edit().putString("alarms", jsonArray.toString()).apply()
    }

    private fun formatTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
}