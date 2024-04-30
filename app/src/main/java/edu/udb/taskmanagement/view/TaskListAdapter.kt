package edu.udb.taskmanagement.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.udb.taskmanagement.R
import edu.udb.taskmanagement.model.Task


class TaskListAdapter(var taskList: List<Task>) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dueDateTextView: TextView = itemView.findViewById(R.id.dueDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.titleTextView.text = task.title
        holder.descriptionTextView.text = task.description
        holder.dueDateTextView.text = task.dueDate.toString()
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
