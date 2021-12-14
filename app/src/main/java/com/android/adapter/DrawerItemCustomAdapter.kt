package com.android.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.android.R
import com.android.models.DataModel


class DrawerItemCustomAdapter(private val mContext: Context, private val layoutResourceId: Int, data: Array<DataModel?>) : ArrayAdapter<DataModel>(mContext, layoutResourceId, data) {
    private var data: Array<DataModel?>

    init {
        this.data = data

    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listItem = convertView

        val inflater = (mContext as Activity).layoutInflater
        listItem = inflater.inflate(layoutResourceId, parent, false)

        val imageViewIcon = listItem!!.findViewById<ImageView>(R.id.imageViewIcon)
        val textViewName = listItem.findViewById<TextView>(R.id.textViewName)


        val folder = data[position]


        imageViewIcon.setImageResource(folder?.icon!!)
        textViewName.text = folder.name

        return listItem
    }
}