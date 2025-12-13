package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM task_table ORDER BY isCompleted ASC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM task_table WHERE category = :cat ORDER BY isCompleted ASC")
    List<Task> getTasksByCategory(String cat);
}