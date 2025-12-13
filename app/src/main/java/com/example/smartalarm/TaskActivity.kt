package com.example.smartalarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityTaskBinding
import kotlin.random.Random
import android.os.VibrationEffect
import android.os.Vibrator
import android.annotation.SuppressLint
import org.json.JSONArray

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private var ringtone: Ringtone? = null
    private var correctAnswer: String = ""
    private var currentTaskType: String = "math"
    private var alarmId: Int = 0  // ← ДОБАВЛЕНО: ID будильника
    private var totalTasks = 3
    private var solvedTasks = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем данные из Intent
        currentTaskType = intent.getStringExtra("taskType") ?: "math"
        alarmId = intent.getIntExtra("alarm_id", 0)  // ← ДОБАВЛЕНО: получаем ID будильника

        // Показываем информацию о будильнике (опционально)
        showAlarmInfo()

        blockBackPress()
        playAlarmSound()
        generateTask(currentTaskType)

        binding.btnSubmitAnswer.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rnd = Random.nextInt(100)

                if (rnd < 30) {
                    val parent = binding.root
                    val maxX = parent.width - v.width
                    val maxY = parent.height - v.height

                    if (maxX > 0 && maxY > 0) {
                        v.animate()
                            .x(Random.nextInt(0, maxX).toFloat())
                            .y(Random.nextInt(0, maxY).toFloat())
                            .setDuration(300)
                            .start()
                    }
                    return@setOnTouchListener true
                }
            }
            v.performClick()
            false
        }

        binding.btnSubmitAnswer.setOnClickListener {
            checkAnswer()
        }
    }

    // ← ДОБАВЛЕН МЕТОД: Показываем информацию о будильнике
    private fun showAlarmInfo() {
        if (alarmId != 0) {
            // Можно добавить отображение дополнительной информации
            // Например: "Будильник #$alarmId"
        }
    }

    private fun generateTask(type: String) {
        if (solvedTasks >= totalTasks) {
            stopAlarmSound()
            Toast.makeText(this, "Все задания решены! Будильник отключен", Toast.LENGTH_LONG).show()

            // ← ДОБАВЛЕНО: Отмечаем будильник как выполненный
            markAlarmAsCompleted()

            finish()
            return
        }

        binding.tvQuestion.text = "Задача ${solvedTasks + 1} из $totalTasks\n\n"

        when (type) {
            "math" -> generateMath()
            "translate" -> generateTranslate()
            "logic" -> generateLogic()
            "attention" -> generateAttention()
            "combo" -> generateRandomMixed()
            "find_symbol" -> generateFindSymbol()
            else -> generateMath()
        }
    }

    private fun generateMath() {
        val a = Random.nextInt(2, 20)
        val b = Random.nextInt(2, 20)
        val op = listOf("+", "-", "×").random()

        val question = when (op) {
            "+" -> { correctAnswer = (a + b).toString(); "$a + $b = ?" }
            "-" -> { correctAnswer = (a - b).toString(); "$a - $b = ?" }
            else -> { correctAnswer = (a * b).toString(); "$a × $b = ?" }
        }

        binding.tvQuestion.append(question)
    }

    private fun generateTranslate() {
        val dict = mapOf(
            "cat" to "кот",
            "dog" to "собака",
            "house" to "дом",
            "car" to "машина",
            "apple" to "яблоко",
            "street" to "улица",
            "phone" to "телефон"
        )

        val entry = dict.entries.random()
        correctAnswer = entry.value.lowercase()
        binding.tvQuestion.append("Переведи слово: \"${entry.key}\"")
    }

    private fun generateLogic() {
        val tasks = listOf(
            "Продолжи ряд: 2, 4, 6, 8, ?" to "10",
            "Продолжи ряд: 3, 6, 9, 12, ?" to "15",
            "Что лишнее: кот, собака, яблоко?" to "яблоко",
            "Что лишнее: 2, 4, 6, 7, 8?" to "7",
        )

        val (q, ans) = tasks.random()
        correctAnswer = ans.lowercase()
        binding.tvQuestion.append(q)
    }

    private fun generateAttention() {
        val digits = (0..9).shuffled().take(5)
        correctAnswer = digits.max().toString()
        binding.tvQuestion.append("Найди самое большое число: ${digits.joinToString(", ")}")
    }

    private fun generateRandomMixed() {
        val list = listOf("math", "translate", "logic", "attention", "find_symbol")
        val randomType = list.random()
        generateTask(randomType)
    }

    private fun generateFindSymbol() {
        val isLetter = Random.nextBoolean()

        if (isLetter) {
            val letters = ('a'..'z').toList()
            val target = letters.random()
            val list = List(8) { letters.random() }.toMutableList()
            list[Random.nextInt(list.size)] = target
            correctAnswer = target.toString()
            binding.tvQuestion.append("Найди букву: \"$target\"\n\nВыберите её из ряда:\n${list.joinToString("  ")}")
        } else {
            val numbers = (0..9).toList()
            val target = numbers.random()
            val list = List(8) { numbers.random() }.toMutableList()
            list[Random.nextInt(list.size)] = target
            correctAnswer = target.toString()
            binding.tvQuestion.append("Найди число: $target\n\nВыберите его из ряда:\n${list.joinToString("  ")}")
        }
    }

    private fun checkAnswer() {
        // Safe call с Elvis operator
        val user = binding.etAnswer.text?.toString()?.trim() ?: ""

        if (user.isEmpty()) {
            Toast.makeText(this, "Введите ответ!", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.equals(correctAnswer, ignoreCase = true)) {
            saveHistory(currentTaskType, true)
            solvedTasks++
            binding.etAnswer.text?.clear() // Safe call!
            generateTask(currentTaskType)
        } else {
            saveHistory(currentTaskType, false)
            vibrateOnError()
            Toast.makeText(this, "Неверно! Попробуйте снова.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAlarmSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(this, uri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = false
        }

        ringtone?.play()
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    private fun blockBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@TaskActivity,
                    "Нельзя выйти — решите задачи!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun vibrateOnError() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    // ← ОБНОВЛЕН МЕТОД: Теперь сохраняет alarm_id
    private fun saveHistory(type: String, ok: Boolean) {
        val prefs = getSharedPreferences("task_history", MODE_PRIVATE)
        val old = prefs.getString("history", "[]") ?: "[]"
        val arr = JSONArray(old)

        val obj = org.json.JSONObject().apply {
            put("type", type)
            put("ok", ok)
            put("alarm_id", alarmId)  // ← ДОБАВЛЕНО: ID будильника
            put("time", java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
        }

        arr.put(obj)
        prefs.edit().putString("history", arr.toString()).apply()
    }

    // ← НОВЫЙ МЕТОД: Отмечаем будильник как выполненный
    private fun markAlarmAsCompleted() {
        if (alarmId != 0) {
            // Можно добавить логику, например:
            // - Отметить в истории что будильник был отключен
            // - Удалить из активных будильников
            // - Сохранить статистику

            val prefs = getSharedPreferences("alarms_completed", MODE_PRIVATE)
            val completedAlarms = prefs.getStringSet("completed", mutableSetOf()) ?: mutableSetOf()
            completedAlarms.add(alarmId.toString())
            prefs.edit().putStringSet("completed", completedAlarms).apply()

            // Также можно обновить статус в основном списке будильников
            updateAlarmStatus(false) // false = неактивный (выполнен)
        }
    }

    // ← НОВЫЙ МЕТОД: Обновляем статус будильника
    private fun updateAlarmStatus(isActive: Boolean) {
        val prefs = getSharedPreferences("alarms_list", MODE_PRIVATE)
        val jsonString = prefs.getString("alarms", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getInt("id") == alarmId) {
                obj.put("isActive", isActive)
                break
            }
        }

        prefs.edit().putString("alarms", jsonArray.toString()).apply()
    }
}