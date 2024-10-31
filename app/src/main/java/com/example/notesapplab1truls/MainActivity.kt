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
import androidx.navigation.navArgument

// en data klass som representerar en anteckning med titel och text
data class Note(
    var title: String,
    var text: String
)

// MainActivity som "får igång/sätter upp/startar" appens UI
class MainActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?){ // under denna så skappas appens UI:n m.h.a NotesApp och viewModel
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

    // NavHost hanterar navigeringen i appen. Dess startdestination är "main". De två "skärmarna" (dvs "screens") är NoteListScreen och EditNoteScreen
    NavHost(navController, startDestination = "main"){
        composable("main"){
            NotesListScreen(notes, navController, viewModel) // visar listan av anteckningar
        }

        composable(
            "create_note/{noteIndex}",
            arguments = listOf(navArgument("noteIndex"){
                    defaultValue = -1
                }
            )
        ){ backStackEntry ->
            val noteIndex = backStackEntry.arguments?.getInt("noteIndex")
            EditNoteScreen(notes, navController, noteIndex, viewModel) // hanterar redigering och skapandet av anteckningarna
        }
    }
}


@Composable
fun NotesListScreen(notes: List<Note>, navController: NavController, viewModel: NotesViewModel){

    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // dessa två ovanför används för att visa dialogrutorna för information (finns ju en knapp "information" som ger information) och dialogrutan som kommer fram när man vill ta bort ( när man klickar på "Delete Note")
    // dessa har boolean tillstånd. de kan vara true om dialogrutan ska visas, annars false.

    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    Column(modifier = Modifier.fillMaxSize()){

        // "Information" knappen
        Button(onClick = { showInfoDialog = true }){
            Text("Information")
        }

        // "Create Note" knappen
        Button(onClick = { navController.navigate("create_note/-1?isNewNote=true") }){
            Text("Create Note")
        }

        // visar anteckningar här som klickbara objekt med NoteItem
        LazyColumn{
            items(notes){ note ->
                NoteItem(
                    note = note,
                    onClick = {
                        navController.navigate("create_note/${notes.indexOf(note)}?isNewNote=false")
                    }
                )
            }
        }

        // dialogruta som förklarar hur appen fungerar
        if(showInfoDialog){
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("App Information") },
                text = {
                    Text("Click on an already created note to update it ('Save Note') or delete it ('Delete Note'). To create a new note, click 'Create Note'. Click  'Cancel' to cancel the changes that you have just made.")
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }){
                        Text("OK")
                    }
                }
            )
        }

        // dialogruta som frågar om man verkligen vill ta bort en anteckning
        if(showDeleteDialog && noteToDelete != null){
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note?") },

                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeNote(noteToDelete!!)
                        showDeleteDialog = false
                        noteToDelete = null
                    }) {
                        Text("Yes")
                    }
                },

                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }){
                        Text("No")
                    }
                }
            )
        }
    }
}


@Composable
fun NoteItem(note: Note, onClick: () -> Unit){

    // komponent som visar en anteckning med titel och text
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)){

            // titel och text för anteckningarna
            Text(text = note.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = note.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun EditNoteScreen( // hanterar redigering och skapandet av anteckningarna
    notes: List<Note>,
    navController: NavController,
    noteIndex: Int? = null,
    viewModel: NotesViewModel
){
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

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

        TextField(
            value = text,
            onValueChange = { newText -> text = newText },
            label = { Text("Text") },
            isError = text.length > 120
        )

        // "Save Note" knappen då den klickas
        Button(onClick = {
            errorMessage = validateNoteInput(title, text)

            if(errorMessage.isEmpty()){
                if(noteIndex != null && noteIndex >= 0){
                    viewModel.updateNote(noteIndex, Note(title, text))
                }
                else{
                    viewModel.addNote(Note(title, text))
                }

                navController.navigate("main"){
                    popUpTo("main") { inclusive = true }
                }
            }
        }){
            Text("Save Note")
        }

        // "Delete Note" knappen då den klickas
        if(noteIndex != null && noteIndex >= 0){
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ){
                Text("Delete Note")
            }
        }

        // "Cancel" knappen då den klickas (dvs för att gå tillbaka utan att spara förändringar)
        Button(
            onClick = {
                navController.navigate("main"){
                    popUpTo("main"){
                        inclusive = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
        ){
            Text("Cancel")
        }

        if(errorMessage.isNotEmpty()){
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }

    // dialogruta om man verkligen vill ta bort en anteckning
    if(showDeleteDialog){
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    if(noteIndex != null && noteIndex >= 0){
                        viewModel.removeNote(notes[noteIndex])
                        showDeleteDialog = false
                        navController.navigate("main"){
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }){
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }){
                    Text("No")
                }
            }
        )
    }
}

// en funktion som validerar (kontrollerar) om antalet tecken i titel och text är ett korrekt antal
private fun validateNoteInput(title: String, text: String): String{

    // deklarerar och initierar en variabel som lagrar felmeddelanden
    var errorMessage = ""

    // validera titeln
    if(title.length < 3){
        errorMessage += "Title must be at least 3 characters long.\n"
    }
    else if(title.length > 50){
        errorMessage += "Title must be at most 50 characters long.\n"
    }

    // validera texten
    if(text.length > 120){
        errorMessage += "Text must be at most 120 characters long.\n"
    }

    // returnera felmeddelanden (eller en tom sträng om inga fel)
    return errorMessage.trim() // trimma för att ta bort extra nya rader
}
