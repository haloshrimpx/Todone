package com.sijayyx.todone.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTodoListData(todoListData: TodoListData): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTodoItemData(todoItemData: TodoItemData): Long

    @Update
    suspend fun updateTodoListData(todoListData: TodoListData)

    @Update
    suspend fun updateTodoItemData(todoItemData: TodoItemData)

    @Query("UPDATE todo_list_items SET isDone =:isDone WHERE todoItemId = :id")
    suspend fun updateTodoItemDoneById(id: Long, isDone: Boolean)

    @Query("UPDATE todo_list_items SET content =:content,todoListId =:selectedListId WHERE todoItemId = :id")
    suspend fun updateTodoItemContentById(id: Long, content: String, selectedListId: Long)

    @Query("UPDATE todo_list_items SET todoListId=:listId WHERE todoItemId=:itemId")
    suspend fun updateTodoItemToListById(itemId:Long, listId: Long)

    @Delete
    suspend fun deleteTodoListData(todoListData: TodoListData)

    @Delete
    suspend fun deleteTodoItemData(todoItemData: TodoItemData)

    @Query("DELETE FROM todo_list_items WHERE todoItemId = :id")
    suspend fun deleteTodoItemDataById(id: Long)

    @Query("DELETE FROM todo_lists WHERE id = :id")
    suspend fun deleteTodoListDataById(id: Long)

    @Query("SELECT * FROM todo_list_items WHERE todoItemId = :itemId")
    suspend fun getTodoItemDataById(itemId: Long): TodoItemData?

    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    suspend fun getTodoListDataById(listId: Long): TodoListData?

    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    fun getTodoListDataFlowById(listId: Long): Flow<TodoListData?>

    @Query("SELECT * FROM todo_lists WHERE listName = :name")
    suspend fun getTodoListDataByName(name: String): TodoListData?

    @Query("SELECT EXISTS(SELECT 1 FROM todo_lists WHERE listName = :listName)")
    suspend fun isTodoListExists(listName: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM todo_lists WHERE id = :listId)")
    suspend fun isTodoListExists(listId: Long): Boolean

    @Query("SELECT * FROM todo_list_items WHERE todoListId = :listId ORDER BY todo_list_items.createAt ASC")
    fun getTodoItemsDataByListId(listId: Long): Flow<List<TodoItemData>>

    @Query(
        "SELECT * FROM todo_lists " +
                "LEFT JOIN todo_list_items ON todo_lists.id = todo_list_items.todoListId " +
                "ORDER BY todo_list_items.createAt ASC"
    )
    fun getTodoListsWithItems(): Flow<Map<TodoListData, List<TodoItemData>>>

    @Query("SELECT * FROM todo_lists LEFT JOIN todo_list_items ON todo_list_items.todoListId = :id WHERE todo_lists.id = :id ORDER BY todo_list_items.createAt ASC ")
    fun getSingleListWithItems(id: Long): Flow<Map<TodoListData, List<TodoItemData>>>

    @Query("SELECT * FROM todo_list_items ORDER BY createAt ASC")
    fun getAllTodoItems(): Flow<List<TodoItemData>>

    @Query("SELECT * FROM todo_lists ORDER BY createAt ASC")
    fun getAllTodoLists(): Flow<List<TodoListData>>

    @Query("SELECT * FROM todo_lists WHERE listName LIKE :pattern OR listName LIKE :originalName ")
    suspend fun getTodoListsWithNamePattern(originalName : String, pattern: String): List<TodoListData>
}