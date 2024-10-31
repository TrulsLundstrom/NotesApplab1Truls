package com.example.notesapplab1truls

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class NotesViewModel : ViewModel() {
    var notes = mutableStateListOf<Note>()
        private set

    fun addNote(note: Note) {
        notes.add(note)
    }

    fun updateNote(index: Int, note: Note) {
        if (index in notes.indices) {
            notes[index] = note
        }
    }

    fun removeNote(note: Note) {
        notes.remove(note)
    }
}


