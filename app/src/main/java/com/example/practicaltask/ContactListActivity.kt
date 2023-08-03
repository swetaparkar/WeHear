package com.example.practicaltask


import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.net.Uri
import com.example.practicaltask.databinding.ActivityContactListBinding


class ContactListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactListBinding
    private lateinit var contactAdapter: ContactListAdapter

    private val contactsList: MutableList<String> = mutableListOf()

    companion object {
        private const val READ_CONTACTS_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.textTitle.text = getString(R.string.contact_list)

        contactAdapter = ContactListAdapter(this, R.layout.contact_list, R.id.tvContactName, contactsList)
        binding.lvContact.adapter = contactAdapter

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_REQUEST_CODE
            )
        } else {
            loadContacts()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun loadContacts() {
        val contactsUri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor: Cursor? = contentResolver.query(contactsUri, projection, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.run { getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) })
                val number =
                    cursor.getString(cursor.run { getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) })
                contactsList.add("$name : $number")
            }
            cursor.close()
            contactAdapter.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                Toast.makeText(
                    this,
                    "Permission to read contacts denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}







