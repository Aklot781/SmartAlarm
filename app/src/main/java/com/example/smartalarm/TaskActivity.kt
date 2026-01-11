package com.example.smartalarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityTaskBinding
import kotlin.random.Random
import android.annotation.SuppressLint
import org.json.JSONArray

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private var ringtone: Ringtone? = null
    private var correctAnswer: String = ""
    private var currentTaskType: String = "math"
    private var alarmId: Int = 0
    private var totalTasks = 3
    private var solvedTasks = 0
    private var difficulty: String = "easy"
    private var isTestMode: Boolean = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("alarm_settings", MODE_PRIVATE)
        difficulty = prefs.getString("difficulty", "easy") ?: "easy"

        currentTaskType = intent.getStringExtra("taskType") ?: "math"
        alarmId = intent.getIntExtra("alarm_id", 0)
        isTestMode = intent.getBooleanExtra("isTest", false)

        setupToolbar()

        if (!isTestMode) {
            playAlarmSound()
            blockBackPress()
        }
        generateTask(currentTaskType)

        binding.btnSubmitAnswer.setOnClickListener {
            checkAnswer()
        }
    }


    // Показываем информацию о будильнике
    private fun showAlarmInfo() {
        if (alarmId != 0) {
        }
    }

    private fun setupToolbar() {
        if (isTestMode) {
            //можно выйти
            binding.btnBack.visibility = android.view.View.VISIBLE
            binding.btnBack.setOnClickListener {
                finish()
            }
        } else {
            //выход запрещён
            binding.btnBack.visibility = android.view.View.VISIBLE
            binding.btnBack.setOnClickListener {
                Toast.makeText(
                    this,
                    "Нельзя выйти — решите задачи!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateTask(type: String) {
        if (solvedTasks >= totalTasks) {
            stopAlarmSound()
            Toast.makeText(this, "Все задания решены! Будильник отключен", Toast.LENGTH_LONG).show()

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
            "wordpuzzle" -> generateWordPuzzle()
            else -> generateMath()
        }
    }

    private fun generateMath() {
        val range = when (difficulty) {
            "easy" -> 2..10
            "medium" -> 10..30
            "hard" -> 20..100
            else -> 2..10
        }

        val a = range.random()
        val b = range.random()

        val operations = when (difficulty) {
            "easy" -> listOf("+", "-")
            "medium" -> listOf("+", "-", "×")
            "hard" -> listOf("+", "-", "×")
            else -> listOf("+")
        }

        val op = operations.random()

        val question = when (op) {
            "+" -> {
                correctAnswer = (a + b).toString()
                "$a + $b = ?"
            }
            "-" -> {
                correctAnswer = (a - b).toString()
                "$a - $b = ?"
            }
            else -> {
                correctAnswer = (a * b).toString()
                "$a × $b = ?"
            }
        }

        binding.tvQuestion.append(question)
    }



    private fun generateTranslate() {
        val dict = when (difficulty) {
            "easy" -> mapOf(
                "cat" to "кот", "dog" to "собака", "house" to "дом", "car" to "машина",
                "book" to "книга", "pen" to "ручка", "sun" to "солнце", "water" to "вода",
                "tree" to "дерево", "flower" to "цветок", "bird" to "птица", "fish" to "рыба"
            )
            "medium" -> mapOf(
                "street" to "улица", "phone" to "телефон", "window" to "окно", "water" to "вода",
                "school" to "школа", "teacher" to "учитель", "student" to "ученик", "computer" to "компьютер",
                "music" to "музыка", "picture" to "картина", "country" to "страна", "city" to "город"
            )
            "hard" -> mapOf(
                "environment" to "окружающая среда", "development" to "развитие", "knowledge" to "знание",
                "experience" to "опыт", "application" to "приложение", "algorithm" to "алгоритм",
                "programming" to "программирование", "information" to "информация", "technology" to "технология",
                "communication" to "общение", "education" to "образование", "intelligence" to "интеллект"
            )
            else -> emptyMap()
        }

        val entry = dict.entries.random()
        correctAnswer = entry.value.lowercase()
        binding.tvQuestion.append("Переведи слово: \"${entry.key}\"")
    }


    private fun generateLogic() {
        val tasks = when (difficulty) {
            "easy" -> listOf(
                "Продолжи ряд: 2, 4, 6, ?" to "8",
                "Что лишнее: кот, собака, яблоко?" to "яблоко",
                "Продолжи ряд: 5, 10, 15, ?" to "20",
                "Что лишнее: яблоко, груша, помидор, стол?" to "стол",
                "Продолжи ряд: 1, 3, 5, ?" to "7",
                "Что лишнее: молоко, вода, сок, хлеб?" to "хлеб"
            )
            "medium" -> listOf(
                "Продолжи ряд: 3, 6, 9, 12, ?" to "15",
                "Что лишнее: 2, 4, 6, 7, 8?" to "7",
                "Продолжи ряд: 10, 20, 30, 40, ?" to "50",
                "Что лишнее: понедельник, среда, пятница, лето?" to "лето",
                "Продолжи ряд: 1, 4, 9, 16, ?" to "25",
                "Что лишнее: ручка, карандаш, фломастер, компьютер?" to "компьютер"
            )
            "hard" -> listOf(
                "Продолжи ряд: 5, 10, 20, 40, ?" to "80",
                "Что лишнее: квадрат, круг, треугольник, красный?" to "красный",
                "Продолжи ряд: 1, 1, 2, 3, 5, ?" to "8",
                "Что лишнее: молоток, пила, отвертка, книга?" to "книга",
                "Продолжи ряд: 2, 3, 5, 7, 11, ?" to "13",
                "Что лишнее: басня, рассказ, стихотворение, математика?" to "математика"
            )
            else -> emptyList()
        }

        val (q, ans) = tasks.random()
        correctAnswer = ans.lowercase()
        binding.tvQuestion.append(q)
    }


    private fun generateAttention() {
        val count = when (difficulty) {
            "easy" -> 5
            "medium" -> 7
            "hard" -> 10
            else -> 5
        }

        val maxDigit = when (difficulty) {
            "easy" -> 9
            "medium" -> 20
            "hard" -> 50
            else -> 9
        }

        val digits = (0..maxDigit).shuffled().take(count)
        correctAnswer = digits.max().toString()

        binding.tvQuestion.append(
            "Найди самое большое число:\n${digits.joinToString(", ")}"
        )
    }


    private fun generateRandomMixed() {
        val list = listOf("math", "translate", "logic", "attention", "wordpuzzle")
        val randomType = list.random()
        generateTask(randomType)
    }

    private fun generateWordPuzzle() {
        val words = when (difficulty) {
            "easy" -> listOf("кот", "дом", "мир", "сон", "день", "нос", "рот", "год", "час", "лук", "меч", "шар")
            "medium" -> listOf("окно", "вода", "зима", "лето", "улица", "книга", "ручка", "стол", "стул", "дверь", "океан", "река")
            "hard" -> listOf("будильник", "умный", "задание", "программа", "телефон", "компьютер", "приложение", "алгоритм", "разработка", "программист", "интерфейс", "устройство")
            else -> emptyList()
        }

        val word = words.random()
        val scrambled = word.toList().shuffled().joinToString(" ")

        correctAnswer = word
        binding.tvQuestion.append("Собери слово из букв:\n$scrambled")
    }


    private fun checkAnswer() {
        val user = binding.etAnswer.text?.toString()?.trim() ?: ""

        if (user.isEmpty()) {
            Toast.makeText(this, "Введите ответ!", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.equals(correctAnswer, ignoreCase = true)) {
            saveHistory(currentTaskType, true)
            solvedTasks++
            binding.etAnswer.text?.clear()


            generateTask(currentTaskType) //генерируем новую задачу
        } else {
            saveHistory(currentTaskType, false)
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
        if (isTestMode) return

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


    private fun saveHistory(type: String, ok: Boolean) {
        val prefs = getSharedPreferences("task_history", MODE_PRIVATE)
        val old = prefs.getString("history", "[]") ?: "[]"
        val arr = JSONArray(old)

        val obj = org.json.JSONObject().apply {
            put("type", type)
            put("ok", ok)
            put("alarm_id", alarmId)
            put("time", java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
        }

        arr.put(obj)
        prefs.edit().putString("history", arr.toString()).apply()
    }

    //Отмечаем будильник как выполненный
    private fun markAlarmAsCompleted() {
        if (alarmId != 0) {
            val prefs = getSharedPreferences("alarms_completed", MODE_PRIVATE)
            val completedAlarms = prefs.getStringSet("completed", mutableSetOf()) ?: mutableSetOf()
            completedAlarms.add(alarmId.toString())
            prefs.edit().putStringSet("completed", completedAlarms).apply()

            updateAlarmStatus(false) //неактивный
        }
    }

    // Обновляем статус будильника
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