package com.example.smartalarm

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        val prefs = getSharedPreferences("alarm_settings", Context.MODE_PRIVATE)

        //грромкость
        val savedVolume = prefs.getInt("volume", 70)
        binding.seekVolume.progress = savedVolume

        binding.seekVolume.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    prefs.edit().putInt("volume", progress).apply()
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )

        //сложность
        val savedDifficulty = prefs.getString("difficulty", "easy")

        when (savedDifficulty) {
            "easy" -> binding.rbEasy.isChecked = true
            "medium" -> binding.rbMedium.isChecked = true
            "hard" -> binding.rbHard.isChecked = true
        }

        binding.radioDifficulty.setOnCheckedChangeListener { _, checkedId ->
            val difficulty = when (checkedId) {
                R.id.rbEasy -> "easy"
                R.id.rbMedium -> "medium"
                R.id.rbHard -> "hard"
                else -> "easy"
            }
            prefs.edit().putString("difficulty", difficulty).apply()
        }
    }
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
