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
            sb.append("Задача: ${obj.getString("type")}\n")
            sb.append("Результат: ${if (obj.getBoolean("ok")) "✔ Верно" else "✖ Ошибка"}\n")
            sb.append("Время: ${obj.getString("time")}\n\n")
        }

        binding.tvHistory.text = sb.toString()
    }
}