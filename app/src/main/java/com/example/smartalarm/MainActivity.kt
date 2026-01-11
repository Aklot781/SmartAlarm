package com.example.smartalarm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toast.makeText(this, "Умный будильник запущен", Toast.LENGTH_SHORT).show()

        binding.btnSetAlarm.setOnClickListener {
            startActivity(Intent(this, SetAlarmActivity::class.java))
        }

        binding.btnTestTasks.setOnClickListener {
            startActivity(Intent(this, ChooseTaskActivity::class.java))
        }

        binding.btnTaskInfo.setOnClickListener {
            showTaskInfoDialog()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAlarmList.setOnClickListener {
            startActivity(Intent(this, AlarmListActivity::class.java))
        }
    }

    private fun showTaskInfoDialog() {
        val infoText = """
            Доступные форматы задач:

            1. Математика
               - Примеры на сложение, вычитание, умножение.

            2. Перевод слов
               - Английский → Русский.

            3. Логические задачи
               - Продолжить последовательность чисел и выбор лишнего.
            
            4. Поиск чисел
               - Найди самое большое число.
            
            5. Собери слово
               - Собери слово из имеющихся букв.
            
            6. Комбинированные задачи
               - Несколько различных типов задач подряд.
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Форматы задач")
            .setMessage(infoText)
            .setPositiveButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}