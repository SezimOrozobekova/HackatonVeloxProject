package com.example.velox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.velox.login.viewModel.Task

// создаем класс адаптера с передачей списка задач
class AdapterCreatedTaskAI(private val taskAiList: List<Task>) : RecyclerView.Adapter<AdapterCreatedTaskAI.TaskViewHolder>() {

    // 1. ViewHolder — держит ссылки на элементы из карточки
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        val tvTaskTimeStartEnd: TextView = itemView.findViewById(R.id.tvTaskTimeStartEnd)
        val tvHobbyType: TextView = itemView.findViewById(R.id.tvHobbyType)
    }

    // 2. Создаем новый ViewHolder (карточку)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return TaskViewHolder(view)
    }

    // 3. Заполняем данные в карточке
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskAiList[position]
        holder.tvTaskName.text = task.title
        holder.tvTaskDate.text = task.date

        if(task.timeStart == "null" && task.timeEnd == "null"){
            holder.tvTaskTimeStartEnd.text = ""
        } else if(task.timeStart != "null" && task.timeEnd == "null"){
            holder.tvTaskTimeStartEnd.text = task.timeStart
        } else if(task.timeStart != "null" && task.timeEnd != "null"){
            holder.tvTaskTimeStartEnd.text = task.timeStart + " - " + task.timeEnd
        }

    }

    // 4. Сколько всего элементов в списке
    override fun getItemCount(): Int {
        return taskAiList.size
    }
}
