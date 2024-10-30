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

        composable("create_note/{noteIndex}"){ backStackEntry ->
            val noteIndex = backStackEntry.arguments?.getInt("noteIndex")
            CreateNoteScreen(notes, navController, noteIndex, viewModel)
        }
    }
}

@Composable
fun MainScreen(notes: List<Note>, navController: NavController){

    Column(modifier = Modifier.fillMaxSize()){
        Button(onClick = { navController.navigate("create_note/-1") } ){
            Text("Create Note")
        }

        LazyColumn{
            items(notes){ note ->
                NoteItem(note = note, onClick = {
                    navController.navigate("create_note/${notes.indexOf(note)}")
                })
            }
        }
    }
}


@Composable
fun NoteItem(note: Note, onClick: () -> Unit){

    Card( modifier = Modifier.padding(8.dp).clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) ){
        Column(modifier = Modifier.padding(16.dp)){
            Text(text = note.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = note.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CreateNoteScreen(notes: List<Note>, navController: NavController, noteIndex: Int? = null, viewModel: NotesViewModel){

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var errorMessages by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(noteIndex){
        noteIndex?.let { index ->
            if(index >= 0 && index < notes.size){
                val note = notes[index]
                title = note.title
                text = note.text
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)){
        TextField(
            value = title,
            onValueChange = { newTitle -> title = newTitle },
            label = { Text("Title") },
            isError = title.length < 3 || title.length > 50
        )

        // Visa specifikt felmeddelande för titeln
        if(errorMessages.any { it.contains("Title") }){
            Text(
                text = errorMessages.first { it.contains("Title") },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        TextField(
            value = text,
            onValueChange = { newText -> text = newText },
            label = { Text("Text") },
            isError = text.length > 120
        )

        // Visa specifikt felmeddelande för texten
        if(errorMessages.any { it.contains("Text") }){
            Text(
                text = errorMessages.first { it.contains("Text") },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(onClick = {
            errorMessages = validateInputs(title, text)

            if (errorMessages.isEmpty()){
                if (noteIndex != null && noteIndex < notes.size){
                    viewModel.updateNote(noteIndex, Note(title, text))
                } else {
                    viewModel.addNote(Note(title, text))
                }
                navController.navigate("main"){
                    popUpTo("main") { inclusive = true }
                }
            }
        }){
            Text("Save Note")
        }
    }
}

private fun validateInputs(title: String, text: String): List<String>{

    val errors = mutableListOf<String>()

    if (title.length < 3){
        errors.add("Title must be at least 3 characters long.")
    }
    else if(title.length > 50){
        errors.add("Title must be at most 50 characters long.")
    }
    if(text.length > 120){
        errors.add("Text must be at most 120 characters long.")
    }
    return errors
}

/*
att göra:

-delete note

-browse an overview of all notes

-just nu så kan du endast skapa en note. Lägg till funktionen till att flera stycken.

 */
