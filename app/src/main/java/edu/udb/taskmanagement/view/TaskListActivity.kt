package edu.udb.taskmanagement.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.udb.taskmanagement.R
import edu.udb.taskmanagement.controller.TaskController
import edu.udb.taskmanagement.model.Task
import edu.udb.taskmanagement.model.TaskRepository
import edu.udb.taskmanagement.network.ApiService
import kotlinx.coroutines.*

class TaskListActivity : AppCompatActivity() {

    private lateinit var taskListView: ListView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var addTaskButton: FloatingActionButton

    private val taskController = TaskController(TaskRepository(ApiService.create()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        addTaskButton = findViewById(R.id.addTaskButton)
        taskListView = findViewById(R.id.taskListView)
        taskListAdapter = TaskListAdapter(this, mutableListOf())
        taskListView.adapter = taskListAdapter

        addTaskButton.setOnClickListener {
            showAddTaskDialog()
            loadTasks()
        }

        // Cargar las tareas al iniciar la actividad
        loadTasks()

        // Registrar un menú contextual para el ListView
        registerForContextMenu(taskListView)

        // Manejar clics en los elementos de la lista
        taskListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val task = parent.getItemAtPosition(position) as Task
                Toast.makeText(this, "Task: ${task.title}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.task_item_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val task = taskListAdapter.getItem(info.position)

        return when (item.itemId) {
            R.id.action_update -> {
                // Crear un Intent para abrir TaskDetailActivity y pasar la tarea seleccionada
                val intent = Intent(this, TaskDetailActivity::class.java).apply {
                    if (task != null) {
                        putExtra("taskId", task.id)
                    }
                    if (task != null) {
                        putExtra("title", task.title)
                    }
                    if (task != null) {
                        putExtra("description", task.description)
                    }
                    if (task != null) {
                        putExtra("dueDate", task.dueDate)
                    }
                    if (task != null) {
                        putExtra("priority", task.priority)
                    }
                }
                startActivity(intent)
                true
            }
            R.id.action_delete -> {
                // Mostrar un diálogo de confirmación antes de eliminar la tarea
                task?.let { showDeleteConfirmationDialog(it) }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
            .setCancelable(false)
            .setPositiveButton("Sí") { dialog, id ->
                // Eliminar la tarea
                deleteTask(task)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
    private fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Llama al método para eliminar la tarea
                taskController.deleteTask(task.id)

                // Muestra un mensaje Toast de éxito en el hilo principal
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskListActivity,
                        "Tarea Eliminada con Exito",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Actualiza la lista de tareas después de eliminar la tarea
                loadTasks()
            } catch (e: Exception) {
                // Muestra un mensaje Toast de error en el hilo principal si ocurre un error
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskListActivity,
                        "Error Eliminar Tarea: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = taskController.getTasks()
                withContext(Dispatchers.Main) { updateTaskList(tasks) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskListActivity,
                        "Error loading tasks: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateTaskList(tasks: List<Task>) {
        taskListAdapter.clear()
        taskListAdapter.addAll(tasks)
        taskListAdapter.notifyDataSetChanged()
    }

    private fun addTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                taskController.addTask(task)
                // Muestra un mensaje Toast de éxito en el hilo principal
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskListActivity,
                        "Tarea Agregada con Exito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                loadTasks()
            } catch (e: Exception) {
                // Muestra un mensaje Toast de error en el hilo principal si ocurre un error
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskListActivity,
                        "Error Agregar Tarea: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Nueva Tarea")

        val inputLayout = LinearLayout(this)
        inputLayout.orientation = LinearLayout.VERTICAL

        val titleEditText = EditText(this)
        titleEditText.hint = "Título"
        inputLayout.addView(titleEditText)

        val descriptionEditText = EditText(this)
        descriptionEditText.hint = "Descripción"
        inputLayout.addView(descriptionEditText)

        val fechaEditText = EditText(this)
        fechaEditText.hint = "Fecha Cierre"
        inputLayout.addView(fechaEditText)

        val prioridadEditText = EditText(this)
        prioridadEditText.hint = "Prioridad"
        inputLayout.addView(prioridadEditText)

        builder.setView(inputLayout)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val fecha = fechaEditText.text.toString()
            val prioridad = prioridadEditText.text.toString()

            val task = Task(
                id = 0,
                title = title,
                description = description,
                dueDate = fecha,
                priority = prioridad
            )

            addTask(task)
            dialog.dismiss()
            Toast.makeText(this, "Tarea guardada: $title", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}




