package com.example.apphelp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_2.*
import kotlinx.android.synthetic.main.activity_main.*

class Activity2 : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var list: ListView ?= null
    var sel = intArrayOf(android.R.id.text1, android.R.id.text2)

    fun read(){
        var cursor: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        startManagingCursor(cursor)

        var from =  arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID)
        var to = intArrayOf(android.R.id.text1, android.R.id.text2)
        var simple : SimpleCursorAdapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, cursor, from, to)
        listView?.adapter = simple


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1){Manifest.permission.READ_CONTACTS}, 111)
        }
        else
            read()


        list = findViewById(R.id.listView)
        list?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        list?.onItemClickListener = this

        buttonSave.setOnClickListener{
            itemSelected()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            read()

    }


    override fun onItemClick(parent:AdapterView<*>?, view: View?, position:Int, id: Long){
    }
    fun itemSelected() {

        val con: ArrayList<String> = ArrayList()
        var itemSelected: String = "Selected contacts: "
        for(i in sel) {
            if(list?.isItemChecked(i) == true) {
                con.add(list?.getItemAtPosition(i) as String)
                itemSelected += list?.getItemAtPosition(i)
            }
        }
        for(i in con.indices){
            println(con[i])
        }
        Toast.makeText(applicationContext, itemSelected, Toast.LENGTH_LONG).show()
    }
}