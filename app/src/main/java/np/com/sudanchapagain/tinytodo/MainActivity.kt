package np.com.sudanchapagain.tinytodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.com.sudanchapagain.tinytodo.ui.theme.tinyTodoTheme
import np.com.sudanchapagain.tinytodo.view.TaskViewModel

data class Task(val title: String, val isCompleted: Boolean)

enum class Filter {
    All, Active
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enables edge-to-edge layout <https://developer.android.com/develop/ui/views/layout/edge-to-edge>
        enableEdgeToEdge()

        val viewModel: TaskViewModel by viewModels()
        setContent {
            // custom Material3 theme
            tinyTodoTheme {
                // scaffold provides a layout structure with slots for top bars, FABs, etc.
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    // main composable function
                    todoApp(
                        modifier = Modifier.padding(paddingValues), viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun todoApp(modifier: Modifier = Modifier, viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState() // Observe tasks
    var filter by remember { mutableStateOf(Filter.All) }
    var showDialog by remember { mutableStateOf(false) }

    // main column layout
    Column(modifier = modifier.padding(16.dp)) {
        // header row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "tiny todo", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge
            )
            Row {
                filterOption("All", Filter.All, filter) { filter = it }
                filterOption("Active", Filter.Active, filter) { filter = it }
            }
        }

        // LazyColumn for efficiently rendering the task list
        // <https://developer.android.com/develop/ui/compose/lists>
        LazyColumn(modifier = Modifier.weight(1f)) {
            // filtering based on selected filter
            items(tasks.filter { filter == Filter.All || !it.isCompleted }) { taskEntity ->
                val task = Task(title = taskEntity.title, isCompleted = taskEntity.isCompleted)
                taskItem(task = task, onCheckedChange = { isChecked ->
                    viewModel.updateTask(taskEntity.copy(isCompleted = isChecked))
                }, onDelete = { viewModel.deleteTask(taskEntity) })
            }
        }

        // to open new task dialog
        Button(
            onClick = { showDialog = true }, modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add New Task")
        }
    }

    if (showDialog) {
        addTaskDialog(onDismiss = { showDialog = false }, onAdd = { title ->
            viewModel.addTask(title)
            showDialog = false
        })
    }
}

@Composable
fun addTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    // pretty self-explanatory
    AlertDialog(
        onDismissRequest = onDismiss, title = {
            Text(
                "Add New Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            TextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                placeholder = { Text("Please enter a title") }
            )
        },
        confirmButton = {
            Button(onClick = { if (title.isNotEmpty()) onAdd(title) }, content = { Text("Add") })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        tonalElevation = 0.dp
    )
}

@Composable
fun filterOption(label: String, option: Filter, selectedFilter: Filter, onFilterSelected: (Filter) -> Unit) {
    // TextButton to select a filter (All or Active)
    TextButton(onClick = { onFilterSelected(option) }) {
        Text(
            text = label,
            fontWeight = if (option == selectedFilter) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun taskItem(task: Task, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    var showDeleteBox by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { showDeleteBox = !showDeleteBox })
    }) {
        // Row representing a single task with a checkbox and title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(
                    checkedColor = Color.LightGray
                )
            )
            Text(
                task.title, modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        if (showDeleteBox) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).clickable { onDelete() }.padding(4.dp)
                    .background(MaterialTheme.colorScheme.error, MaterialTheme.shapes.small)
            ) {
                Text("Delete", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
