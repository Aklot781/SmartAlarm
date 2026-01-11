package com.example.smartalarm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivityChooseTaskBinding

class ChooseTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityChooseTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка назад в тулбаре
        binding.btnBack.setOnClickListener {
            finish() // Закрывает активность и возвращает назад
        }

        // Карточки с заданиями
        binding.cardMath.setOnClickListener { startTask("math") }
        binding.cardTranslate.setOnClickListener { startTask("translate") }
        binding.cardFindNumber.setOnClickListener { startTask("logic") }
        binding.cardFindLetter.setOnClickListener { startTask("attention") }
        binding.cardWordPuzzle.setOnClickListener { startTask("wordpuzzle") }
        binding.cardFindCombo.setOnClickListener { startTask("combo") }
    }

    private fun startTask(type: String) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("taskType", type)
        intent.putExtra("isTest", true)
        startActivity(intent)
    }
}