package np.com.sudanchapagain.tinytodo.utility

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import np.com.sudanchapagain.tinytodo.Task

object TaskStorage {
    private const val TASKS_KEY = "tasks_key"
    private val gson = Gson()

    fun saveTasks(context: Context, tasks: List<Task>) {
        val sharedPreferences = context.getSharedPreferences("tiny_todo", Context.MODE_PRIVATE)
        val tasksJson = gson.toJson(tasks)
        sharedPreferences.edit().putString(TASKS_KEY, tasksJson).apply()
    }

    fun loadTasks(context: Context): List<Task> {
        val sharedPreferences = context.getSharedPreferences("tiny_todo", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString(TASKS_KEY, null)
        if (tasksJson != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            return gson.fromJson(tasksJson, type)
        }
        return emptyList()
    }
}
