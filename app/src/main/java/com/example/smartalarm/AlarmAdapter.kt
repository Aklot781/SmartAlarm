package com.example.smartalarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter(
    private val alarms: List<AlarmListActivity.AlarmItem>,
    private val onEditClick: (AlarmListActivity.AlarmItem) -> Unit,
    private val onDeleteClick: (AlarmListActivity.AlarmItem) -> Unit,
    private val onToggleClick: (AlarmListActivity.AlarmItem) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val btnToggle: ImageView = view.findViewById(R.id.btnToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        // Время
        holder.tvTime.text = formatTime(alarm.time)

        // Тип задачи
        val taskTypeName = when (alarm.taskType) {
            "math" -> "Математика"
            "translate" -> "Перевод"
            "logic" -> "Логика"
            "attention" -> "Внимательность"
            "wordpuzzle" -> "Собери слово"
            "combo" -> "Смешанные"
            else -> "Неизвестно"
        }
        holder.tvTaskType.text = taskTypeName

        // Статус
        if (alarm.isActive) {
            holder.tvStatus.text = "Активен"
            holder.btnToggle.setImageResource(android.R.drawable.ic_lock_idle_alarm)
        } else {
            holder.tvStatus.text = "⏸Отключен"
            holder.btnToggle.setImageResource(android.R.drawable.ic_lock_idle_lock)
        }

        // Кнопки
        holder.btnEdit.setOnClickListener { onEditClick(alarm) }
        holder.btnDelete.setOnClickListener { onDeleteClick(alarm) }
        holder.btnToggle.setOnClickListener { onToggleClick(alarm) }
    }

    override fun getItemCount() = alarms.size

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm, EEEE, d MMMM", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}