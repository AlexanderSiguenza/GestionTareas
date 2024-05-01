package edu.udb.taskmanagement.view

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import edu.udb.taskmanagement.R
import edu.udb.taskmanagement.controller.TaskController
import edu.udb.taskmanagement.model.Task
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.udb.taskmanagement.model.TaskRepository
import edu.udb.taskmanagement.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailActivity : AppCompatActivity() {

    private val taskController = TaskController(TaskRepository(ApiService.create()))
    private lateinit var currentTask: Task

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dueDateTextView: TextView
    private lateinit var priorityTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        dueDateTextView = findViewById(R.id.dueDateTextView)
        priorityTextView = findViewById(R.id.priorityTextView)

        val taskId = intent.getIntExtra("taskId", -1)
        if (taskId != -1) {
            loadTaskDetails(taskId)
        } else {
            // Manejar el error de taskId inválido
            Toast.makeText(this, getString(R.string.invalid_task_id_error), Toast.LENGTH_SHORT).show()
            finish() // Finaliza la actividad actual
        }

        // Configurar el botón de guardar para actualizar la tarea
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            updateTaskDetails()
        }

    }

    private fun loadTaskDetails(taskId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            currentTask = taskController.getTask(taskId)
            withContext(Dispatchers.Main) {
                displayTaskDetails(currentTask)
            }
        }
    }

    private fun displayTaskDetails(task: Task) {
        titleEditText.setText(task.title)
        descriptionEditText.setText(task.description)
        dueDateTextView.text = task.dueDate.toString()
        priorityTextView.text = task.priority.toString()
    }

    private fun updateTaskDetails() {
        // Actualizar los detalles de la tarea con los valores de los EditText
        val updatedTask = currentTask.copy(
            title = findViewById<EditText>(R.id.titleEditText).text.toString(),
            description = findViewById<EditText>(R.id.descriptionEditText).text.toString(),
            // Actualizar otros campos según sea necesario
        )

        CoroutineScope(Dispatchers.IO).launch {
            taskController.updateTask(updatedTask)
            withContext(Dispatchers.Main) {
                // Manejar la actualización exitosa
                Toast.makeText(this@TaskDetailActivity, getString(R.string.task_updated_message), Toast.LENGTH_SHORT).show()
            }
        }

        val intent = Intent(this, TaskListActivity::class.java)
        startActivity(intent)
    }

    private fun deleteTask() {
        CoroutineScope(Dispatchers.IO).launch {
            taskController.deleteTask(currentTask.id)
            withContext(Dispatchers.Main) {
                // Manejar la eliminación exitosa
                Toast.makeText(this@TaskDetailActivity, getString(R.string.task_deleted_message), Toast.LENGTH_SHORT).show()
                finish() // Finaliza la actividad actual después de la eliminación exitosa
            }
        }
    }
}
