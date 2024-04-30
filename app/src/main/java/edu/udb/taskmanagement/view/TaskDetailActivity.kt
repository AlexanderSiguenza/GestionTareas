package edu.udb.taskmanagement.view

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import edu.udb.taskmanagement.R
import edu.udb.taskmanagement.controller.TaskController
import edu.udb.taskmanagement.model.Task
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var taskController: TaskController
    private lateinit var currentTask: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

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

        // Configurar el botón de eliminar para eliminar la tarea
        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteTask()
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
        findViewById<TextView>(R.id.titleTextView).text = task.title
        findViewById<TextView>(R.id.descriptionTextView).text = task.description
        findViewById<TextView>(R.id.dueDateTextView).text = task.dueDate.toString()
        findViewById<TextView>(R.id.priorityTextView).text = task.priority.toString()
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
