package com.example.todolist;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// FIX: Added 'exportSchema = false' to stop the warning
@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}