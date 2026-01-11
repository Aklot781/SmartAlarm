package com.example.smartalarm

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityHistoryBinding
import org.json.JSONArray

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadHistory()
    }

    private fun setupClickListeners() {
        // Кнопка назад в тулбаре
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Кнопка очистить историю
        binding.btnClearHistory.setOnClickListener {
            showClearHistoryDialog()
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Очистить историю")
            .setMessage("Вы уверены, что хотите удалить всю историю заданий?")
            .setPositiveButton("Очистить") { dialog, _ ->
                clearHistory()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearHistory() {
        // Очистка истории из SharedPreferences
        val prefs = getSharedPreferences("task_history", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Обновление TextView
        binding.tvHistory.text = "История очищена"

        Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show()
    }

    private fun loadHistory() {
        val prefs = getSharedPreferences("task_history", MODE_PRIVATE)
        val jsonString = prefs.getString("history", "[]") ?: "[]"

        val jsonArray = JSONArray(jsonString)
        val sb = StringBuilder()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            sb.append(" ${obj.getString("time")}\n")
            sb.append(" Тип: ${obj.getString("type")}\n")
            sb.append(if (obj.getBoolean("ok")) "Верно" else "Ошибка")

            // Дополнительные поля если есть
            if (obj.has("task")) {
                sb.append("\n ${obj.getString("task")}")
            }
            if (obj.has("answer")) {
                sb.append("\n Ответ: ${obj.getString("answer")}")
            }
            sb.append("\n\n${"-".repeat(30)}\n\n")
        }

        if (sb.isEmpty()) {
            binding.tvHistory.text = "История заданий пуста"
        } else {
            binding.tvHistory.text = sb.toString()
        }
    }
}