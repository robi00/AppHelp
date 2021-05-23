package com.example.apphelp

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import kotlinx.android.synthetic.main.activity_2.*

class Activity2 : AppCompatActivity(), AdapterView.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        read()
    }
    fun read(){
        var cursor: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        startManagingCursor(cursor)

        var from =  arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID)
        var to = intArrayOf(android.R.id.text1, android.R.id.text2)
        var simple : SimpleCursorAdapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, cursor, from, to)
        listView?.adapter = simple
        listView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView?.onItemClickListener = this
    }

    override fun onItemClick(parent:AdapterView<*>?, view: View?, position:Int, id: Long){

    }
}