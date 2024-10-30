
package com.example.notesapplab1truls
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

data class Note(
    var title: String,
    var text: String
)

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContent{
            val viewModel: NotesViewModel = viewModel()
            NotesApp(viewModel)
        }
    }
}

@Composable
fun NotesApp(viewModel: NotesViewModel){
    val navController = rememberNavController()
    val notes = viewModel.notes

    NavHost(navController, startDestination = "main"){
        composable("main"){
            MainScreen(notes, navController)
        }
        composable("create_note/{noteIndex}"){ backStackEntry -> // testa om du kan flytta "backStackEntry" till raden nedan. (borde inte påverka något)

            val noteIndex = backStackEntry.arguments?.getInt("noteIndex")
            CreateNoteScreen(notes, navController, noteIndex, viewModel)
        }
    }
}

@Composable
fun MainScreen(notes: List<Note>, navController: NavController){
    Column(modifier = Modifier.fillMaxSize()){
        Button(onClick = { navController.navigate("create_note/-1") }) {
            Text("Create Note")
        }

        LazyColumn{
            items(notes){ note -> //
                NoteItem(note = note, onClick = {
                    navController.navigate("create_note/${notes.indexOf(note)}")
                })
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){ // ändra denna senare
        Column(modifier = Modifier.padding(16.dp)){
            Text(text = note.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = note.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CreateNoteScreen(
    notes: List<Note>,
    navController: NavController,
    noteIndex: Int? = null,
    viewModel: NotesViewModel
){ // ändra

    var title by remember{ mutableStateOf("") }
    var text by remember{ mutableStateOf("") }
    var errorMessage by remember{ mutableStateOf("") }

    LaunchedEffect(noteIndex){
        noteIndex?.let{ index ->
            if(index >= 0 && index < notes.size){ // Check if index is valid
                val note = notes[index]
                title = note.title
                text = note.text
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)){
        TextField(
            value = title, // State variable
            onValueChange = { newTitle -> title = newTitle }, // Update state
            label = { Text("Title") },
            isError = title.length < 3 || title.length > 50
        ) // ändra
        TextField(
            value = text, // State variable
            onValueChange = { newText -> text = newText }, // Update state
            label = { Text("Text") },
            isError = text.length > 120
        )// ändra
        Button(onClick = {
            errorMessage = validateInputs(title, text)
            if(errorMessage.isEmpty()){
                if(noteIndex != null && noteIndex < notes.size){
                    viewModel.updateNote(noteIndex, Note(title, text))
                }
                else{
                    viewModel.addNote(Note(title, text)) // Add to viewModel's list
                }
                navController.navigate("main"){
                    popUpTo("main"){ inclusive = true }
                }
            }
        }) {
            Text("Save Note")
        }
    }
} // enligt mig blev min kod förvirande => ändra kanske koden ovanför så att den blir mer lättläst och lättare att förså.

private fun validateInputs(title: String, text: String): String{
    return when {
        title.length < 3 -> "Title must be at least 3 characters."
        title.length > 50 -> "Title must be at most 50 characters."
        text.length > 120 -> "Text must be at most 120 characters."
        else -> ""
    }
}

class NotesViewModel : ViewModel(){ // gör kanske en helt ny fil för denna klassen.
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> = _notes

    fun addNote(note: Note){
        _notes.add(note)
    }

    fun updateNote(index: Int, note: Note){
        _notes[index] = note
    }
}


