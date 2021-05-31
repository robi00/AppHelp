package com.example.apphelp

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast



val DATABASE_NAME : String = "mydb.db"
val TABLE_NAME : String = "contacts"
val DATABASE_VERSION : Int = 1
val KEY_ROWID : String = "_id"
val KEY_NAME : String = "name"
val KEY_PHONE : String = "phone number"
val CREATE_TABLE : String = "create table " + TABLE_NAME +
        " ( " + KEY_ROWID + " integer primary key autoincrement, " +
        KEY_NAME + " text, " + KEY_PHONE + " text)"
val DROP_TABLE :String = " drop table if exists " + TABLE_NAME


class DataBaseHelper (var context: Context?)  : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        try{
            db?.execSQL(CREATE_TABLE)
            Toast.makeText(context, "onCreate called", Toast.LENGTH_SHORT).show()
        }catch (e : SQLException){
            Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try{
            Toast.makeText(context, "onUpgrade called", Toast.LENGTH_SHORT).show()
            db?.execSQL(DROP_TABLE)
            onCreate(db)
        } catch(e: SQLException){
            Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show()
        }
    }
}