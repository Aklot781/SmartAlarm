package com.example.smartalarm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityHistoryBinding
import org.json.JSONArray

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("task_history", MODE_PRIVATE)
        val jsonString = prefs.getString("history", "[]") ?: "[]"

        val jsonArray = JSONArray(jsonString)
        val sb = StringBuilder()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            sb.append(" ${obj.getString("time")}\n")
            sb.append(" Тип: ${obj.getString("type")}\n")
            sb.append(if (obj.getBoolean("ok")) "✅ Верно" else "❌ Ошибка")

            // Дополнительные поля если есть
            if (obj.has("task")) {
                sb.append("\n🔍 ${obj.getString("task")}")
            }
            if (obj.has("answer")) {
                sb.append("\n📝 Ответ: ${obj.getString("answer")}")
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