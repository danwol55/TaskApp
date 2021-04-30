package com.example.taskapp.utilities

import androidx.appcompat.widget.SearchView

inline fun SearchView.onQueryTextChange(crossinline listener: (String) -> Unit)
{
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(p0: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            listener(p0.orEmpty())
            return true
        }
    })
}