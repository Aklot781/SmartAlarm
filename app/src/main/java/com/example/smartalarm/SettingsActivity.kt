package com.example.smartalarm

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("alarm_settings", Context.MODE_PRIVATE)

        // Загружаем сохранённые значения
        val savedVolume = prefs.getInt("volume", 70)
        val vibrationEnabled = prefs.getBoolean("vibration", true)

        binding.seekVolume.progress = savedVolume
        binding.switchVibration.isChecked = vibrationEnabled

        // Сохраняем изменения громкости
        binding.seekVolume.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    prefs.edit().putInt("volume", progress).apply()
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )

        // Сохраняем изменения вибрации
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration", isChecked).apply()
        }
    }
}