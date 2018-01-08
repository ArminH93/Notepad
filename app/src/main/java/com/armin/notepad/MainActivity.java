package com.armin.notepad;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.armin.notepad.db.TaskDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity 
{

    private static final String TAG = "MainActivity";

    public TaskDbHelper mHelper;
    public ListView mTaskListView;
    public ArrayAdapter<String> mAdapter;

    com.github.clans.fab.FloatingActionMenu fab_menu;
    com.github.clans.fab.FloatingActionButton action_add_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        fab_menu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fab_menu);
        action_add_task = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_add_task);
        mTaskListView = (ListView) findViewById(R.id.list_todo) ;
        mHelper = new TaskDbHelper(this);

        action_add_task.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) 
            {
                final EditText taskEditText = new EditText(MainActivity.this);
                taskEditText.setSingleLine(false);
                taskEditText.setLines(4);
                taskEditText.setMaxLines(5);
                taskEditText.setGravity(Gravity.BOTTOM);
                taskEditText.setHorizontalScrollBarEnabled(false);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Erstelle eine neue Notiz")
                        .setView(taskEditText)
                        .setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }

                        }).setNegativeButton("Abbrechen", null)
                        .create();
                dialog.show();
            }
        });
        updateUI();
    }

    public void deleteTask(View view) 
    {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
        Toast.makeText(MainActivity.this, "Notiz gelöscht", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() 
    {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        
        while (cursor.moveToNext()) 
        {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
    }
        if (mAdapter == null) 
        {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } 
        else 
        {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }
}
