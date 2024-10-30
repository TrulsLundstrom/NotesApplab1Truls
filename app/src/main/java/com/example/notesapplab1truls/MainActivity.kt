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

// det ovanför, ändra kanske tillbaka det så att allting "sitter ihop"

data class Note( // This is the data class for the note. A note always contains a title and the text.
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
    // just nu så kan inte både error message för titeln och för texten visas, gör kanske så att båda felmedelanden kan visas. Uppnå detta genom att ha en lista med felmeddelanden i validateInputs funktionen

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(noteIndex) {

        noteIndex?.let { index ->
            if (index >= 0 && index < notes.size) {
                val note = notes[index]
                title = note.title
                text = note.text
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

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

            if(errorMessage.isNotEmpty()){
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(onClick = {
                errorMessage = validateInputs(title, text)

                if(errorMessage.isEmpty()){
                    if(noteIndex != null && noteIndex < notes.size){
                        viewModel.updateNote(noteIndex, Note(title, text))
                    }
                    else{
                        viewModel.addNote(Note(title, text))
                    }
                    navController.navigate("main"){
                        popUpTo("main") { inclusive = true }
                    }
                }
            }) {
                Text("Save Note")
            }
        }
    }

private fun validateInputs(title: String, text: String): String{ // gör så att det som returneras faktiskt används. just nu om du t.ex skriver en titel med 2 tecken så vissas inte något felmedelande

    if(title.length < 3){
        return "Title must be at least 3 characters long."
    }
    else if(title.length > 50){
        return "Title must be at most 50 characters long."
    }
    else if(text.length > 120){
        return "Text must be at most 120 characters long."
    }
    return ""
}

/*
att göra:

-lägg till kommentarer som förklarar delar av koden. Just nu kan det vara svårt för andra programmerare att förstå din kod. Kan även vara bra i framtiden om du kollar tillbaka på din kod och vill förstå vad du gjorde.

-delete note

-browse an overview of all notes

-checkbox (?)

-update note ( just nu använder du en save knapp, men det kanske är tillräkligt)

-just nu så kan du endast skapa en note. Lägg till funktionen till att flera stycken.

-lägg kanske till datum på varje anteckning

- när du uppdaterar anteckningen, gör kanske en "revert back to previous version" knapp.

- när du har skapat din första note, gör kanske så att flera knappar kommer fram längst upp, t.ex delete. Om du inte har skapat en note än, så ska inte en delete knapp finnas.

- gör kanske en informations knapp, om du klickar på den så kommer en beskrivning, t.ex: klicka på en anteckning för att uppdatera och spara förändringar etc...

 - ändra ALLA kommentarer till engelska och ta bort allting som är på svenska och ersätt dem.
 */
