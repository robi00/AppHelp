package com.example.apphelp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private val TABLE_NAME: String = "people_table"
private val COL_ID: String = "ID" //col 0
private val COL_NAME: String = "name" //col 1
private val COL_PHONE: String = "phonenumber" //col 2

class DatabaseHelper(var context: Context): SQLiteOpenHelper (context, TABLE_NAME,null,1){

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLE_NAME + "("+
                COL_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COL_NAME+ " VARCHAR(256), "+
                COL_PHONE+ " VARCHAR(256))"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP IF TABLE EXISTS " +TABLE_NAME)
        onCreate(db)
    }
    fun addData(item:String, phone: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_NAME,item)
        contentValues.put(COL_PHONE,phone)

        Log.d(TAG, "addData: Adding $item and $phone to $TABLE_NAME")
        var result = db.insert(TABLE_NAME,null,contentValues)

        //if data as inserted incorrectly it will return -1
        if (result == (-1).toLong()) {
            return false
        } else {
            Log.d("addData","contact entered in the database")
            return true
        }
    }
    fun getData(): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        var query = "SELECT * FROM $TABLE_NAME"
        var data: Cursor = db.rawQuery(query, null)
        return data
    }
    //return only the ID that matches the name passed in param name
    fun getItemID(name: String): Cursor {
        var db: SQLiteDatabase = this.writableDatabase
        var query = "SELECT $COL_ID FROM $TABLE_NAME WHERE $COL_NAME = '$name'"
        var data: Cursor = db.rawQuery(query, null)
        return data
    }

    //delete the contact that match tha id and the name passed in param
    fun deleteName(id:Int, name: String?){
        var db = this.writableDatabase
        var query = "DELETE FROM $TABLE_NAME WHERE $COL_ID = '$id' AND $COL_NAME = '$name'"
        db.execSQL(query)
    }
    //check if the phone number passed as a parameter is already present in the database
    fun checkDuplicate(phone: String): Boolean {
        var db = this.writableDatabase
        var query = "SELECT $COL_NAME FROM $TABLE_NAME WHERE $COL_PHONE = '$phone'"
        var data: Cursor = db.rawQuery(query,null)
        if (data.count == 0){ // if the phone number passed by parameter is NOT in the db
            return false
        }else{
            return true
        }
    }
}
