package com.example.apphelp

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.apphelp.databinding.Activity2Binding


class Activity2 : AppCompatActivity() {

    lateinit var databaseHelper : DataBaseHelper

    //view binding
    lateinit var binding: Activity2Binding
    //contact permission code
    private val CONTACT_PERMISSION_CODE=1
    //contact pick code
    private val CONTACT_PICK_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseHelper = DataBaseHelper (this)
        var sqLiteDataBase : SQLiteDatabase = databaseHelper.writableDatabase


        binding = Activity2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //handler click to pick contact
        binding.addFab.setOnClickListener {
            //check permission allowed or not
            if(chechConstactPermission()) {
                //allowed
                pickContact()
            }
            else{
                //not allowed, request
                requestContactPermission()
            }
        }
    }

    private fun chechConstactPermission(): Boolean{
        //check if permission was granted/alloews or not, returns true if allowed, false if not
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestContactPermission(){
        //request READ_CONTACTS permission
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this,permission, CONTACT_PERMISSION_CODE)
    }
    private fun pickContact(){
        //intent to pick contact
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //handle permission request results. calls when user from Permission request dialog press allow or deny
        if(requestCode == CONTACT_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted, can pick contact
            }
            else {
                //permission denied, can't pick contact
                Toast.makeText(this,"Permission denied ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var nameList: MutableList<String> = mutableListOf()
        var idList: MutableList<String> = mutableListOf()
        var numberList: MutableList<String> = mutableListOf()
        //handle intent result. calls when user from intent (pickContact) picks on cancels pick contact
        if(resultCode == RESULT_OK) {
            //calls when user click a contact from contacts (intent) list
            if(requestCode == CONTACT_PICK_CODE) {
                binding.contactIv.text = ""

                val cursor1: Cursor
                val cursor2: Cursor?

                //get data from intent
                val uri = data!!.data
                cursor1 = contentResolver.query(uri!!, null, null, null,null)!!
                if(cursor1.moveToFirst()) {
                    //get contacts details
                    val contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val contactThumbnail = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()

                    idList.add(contactId)
                    nameList.add(contactName)

                    //set details
                    binding.contactIv.append("ID: $contactId")
                    binding.contactIv.append("\nName: $contactName")
                    //set image, first check if uri/thumbnail is not null
                    if(contactThumbnail != null){
                        binding.thumbnailIv.setImageURI(Uri.parse(contactThumbnail))
                    }
                    else {
                        binding.thumbnailIv.setImageResource(R.drawable.ic_person)
                    }
                    //check if contact has a phone number or not
                    if(idResultHold == 1) {
                        cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,
                            null,
                            null)
                        //a contact may have a multiple phone numbers
                        while(cursor2!!.moveToNext()) {
                            //get phone number
                            val contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            //set phone number
                            binding.contactIv.append("\nPhone: $contactNumber")
                            numberList.add(contactNumber)
                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }
        else{
            //cancelled picking contact
            Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
        }
    }
}