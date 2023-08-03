package com.example.practicaltask

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class ContactListAdapter(
    context: Context, resource: Int, textViewResourceId: Int, objects: List<String>
) : ArrayAdapter<String>(context, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.contact_list, parent, false)

        val contactInfo = getItem(position)
        val tvContactName = view.findViewById<TextView>(R.id.tvContactName)
        val tvContactInitials = view.findViewById<TextView>(R.id.tvContactInitials)

        val parts = contactInfo?.split(" : ") ?: listOf("", "")
        val name = parts[0]

        tvContactInitials.text = getInitials(name)

        tvContactName.text = name

        return view
    }

    private fun getInitials(name: String): String {
        val initials = StringBuilder()
        val words = name.split(" ")

        for (word in words) {
            if (word.isNotBlank()) {
                initials.append(word[0].toUpperCase())
            }
        }

        return initials.toString()
    }
}
