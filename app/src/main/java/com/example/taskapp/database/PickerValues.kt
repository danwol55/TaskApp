package com.example.taskapp.database

val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
val days = Array(31) {i ->
    if(i<10) return@Array "0$i"
    else return@Array i.toString()
}
val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
val minutes = Array(60) {i ->
    if(i<10) return@Array "0$i"
    else return@Array i.toString()
}