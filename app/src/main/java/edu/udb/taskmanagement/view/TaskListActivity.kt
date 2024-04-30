package edu.udb.taskmanagement.view

import android.annotation.SuppressLint
import edu.udb.taskmanagement.controller.TaskController
import edu.udb.taskmanagement.model.Task
import edu.udb.taskmanagement.model.TaskRepository
import edu.udb.taskmanagement.network.ApiService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.udb.taskmanagement.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListActivity : AppCompatActivity() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter

   private val taskController = TaskController(TaskRepository(ApiService.create()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        taskRecyclerView = findViewById(R.id.taskRecyclerView)
        taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskListAdapter = TaskListAdapter(emptyList())
        taskRecyclerView.adapter = taskListAdapter

        // Cargar las tareas al iniciar la actividad
        loadTasks()
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = taskController.getTasks()
            withContext(Dispatchers.Main) { updateTaskList(tasks) }
        }
    }


    private fun updateTaskList(tasks: List<Task>) {
        taskListAdapter.taskList = tasks
        taskListAdapter.notifyDataSetChanged()
    }
}
