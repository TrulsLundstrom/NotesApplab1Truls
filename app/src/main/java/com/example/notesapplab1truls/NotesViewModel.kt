package com.example.notesapplab1truls

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class NotesViewModel : ViewModel(){

    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> = _notes

    fun addNote(note: Note){
        _notes.add(note)
    }

    fun updateNote(index: Int, note: Note){
        _notes[index] = note
    }
}