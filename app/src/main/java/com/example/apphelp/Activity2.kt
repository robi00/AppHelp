package com.example.apphelp

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.apphelp.databinding.Activity2Binding
import kotlinx.android.synthetic.main.activity_2.*


class Activity2 : AppCompatActivity() {

    //view binding
    lateinit var binding: Activity2Binding
    //contact permission code
    private val CONTACT_PERMISSION_CODE=1
    //contact pick code
    private val CONTACT_PICK_CODE = 2

    lateinit var mDatabaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = Activity2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        mDatabaseHelper = DatabaseHelper(this)

        populateListView()


        //handler click to pick contact
        add_data.setOnClickListener {
            //check permission allowed or not
            if(chechConstactPermission()) {
                //allowed
                pickContact()
                populateListView()
            }
            else{
                //not allowed, request
                requestContactPermission()
            }
        }

    }

    private fun chechConstactPermission(): Boolean{
        //check if permission was granted/allowes or not, returns true if allowed, false if not
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestContactPermission(){
        //request READ_CONTACTS permission
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this,permission, CONTACT_PERMISSION_CODE)
    }
    fun pickContact(){
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
                    val contactImm = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()

                    //set details
                    binding.contactIv.append("ID: $contactId")
                    binding.contactIv.append("\nName: $contactName")

                    //set image, first check if uri/thumbnail is not null
                    if(contactImm != null){
                        binding.immContact.setImageURI(Uri.parse(contactImm))
                    }
                    else {
                        binding.immContact.setImageResource(R.drawable.ic_person)
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
                            var check = mDatabaseHelper.checkDuplicate(contactNumber)
                            if (check) {
                                Log.d("ActivityResult", "twice the same number")
                            }else{
                                //set phone number
                                binding.contactIv.append("\nPhone: $contactNumber")

                                var newEntryName: String = contactName.toString() //contains the name of the selected contact
                                var newEntryPhone: String = contactNumber.toString() //contains the phone number of the selected contact
                                if(newEntryName.isNotEmpty() && newEntryPhone.isNotEmpty()) {
                                    addData(newEntryName,newEntryPhone)
                                } else {
                                    toastMessage("You must select a contact!")
                                }
                            }
                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }
        }
        else{
            //cancelled picking contact
            toastMessage("Cancelled")
        }
    }
    fun addData(newEntryName: String, newEntryPhone: String){
        //check if the selected telephone number is already present in the database,
        // so if the user had already entered this contact among the emergency ones
        var check = mDatabaseHelper.checkDuplicate(newEntryPhone)
        if (check) { //if checkDuplicate return true (so if that contact is already saved in the db)
            toastMessage("This contact is already present among the emergency ones")
        }else{
            //passes the name and phone number of the selected contact to the database to insert them
            var insertData: Boolean = mDatabaseHelper.addData(newEntryName, newEntryPhone)
            if(insertData) { //if insertData return true
                toastMessage("Data Successfully Inserted!")
            } else { //return false
                toastMessage("Something went wrong")
            }
        }
    }
    private fun toastMessage(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    private fun populateListView() {
        //get the data and append to the list
        var data: Cursor = mDatabaseHelper.getData()
        var listData: ArrayList<String> = ArrayList()
        while(data.moveToNext()) {
            //get the value from the database in column 1 (name)
            //then add it to the ArrayList
            listData.add(data.getString(1)) //it contains all the names of the contacts saved in the database,
                                                        // so the contacts previously selected as the emergency ones
        }

        //create the list adapter and set the adapter and populate the listView
        users_list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)

        //set click listener to the list view
        users_list.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
                var name: String = adapterView.getItemAtPosition(i).toString()
                var data: Cursor = mDatabaseHelper.getItemID(name) //get the ID associated witch that name
                var itemID: Int = -1

                while (data.moveToNext()) {
                    itemID = data.getInt(0) // contains the ID of the contact selected from the listView
                }
                if (itemID > -1) {

                    AlertDialog.Builder(this) // creates a pop up to ask the user if he wants to
                                                    // delete the one he clicked on from the emergency contacts
                        .setMessage("Do you want to delete this contact from the emergency ones?")
                        .setPositiveButton("yes") { _, _ -> //if the user click on "yes", pass the ID and name of the
                                                                // selected contact to the database to delete them
                            mDatabaseHelper.deleteName(itemID,name)
                        }
                        .setNegativeButton("no") { dialog, _ ->
                            dialog.dismiss() //if the user click on "no", the pop up closes without performing any action
                        }
                        .show()
                } else { // if the user clicks on an item in the list that has already been deleted
                    toastMessage("\n" +
                            "this contact is no longer an emergency contact")
               }

        }
    }
}

