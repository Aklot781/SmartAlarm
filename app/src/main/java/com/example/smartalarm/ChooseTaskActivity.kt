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


        binding.cardMath.setOnClickListener { startTask("math") }
        binding.cardTranslate.setOnClickListener { startTask("translate") }
        binding.cardFindNumber.setOnClickListener { startTask("logic") }
        binding.cardFindLetter.setOnClickListener { startTask("attention") }
        binding.cardFindCombo.setOnClickListener { startTask("combo") }
    }

    private fun startTask(type: String) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("taskType", type)
        startActivity(intent)
    }
}