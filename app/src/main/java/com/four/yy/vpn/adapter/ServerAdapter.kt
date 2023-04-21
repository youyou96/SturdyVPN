package com.four.yy.vpn.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.four.yy.vpn.R
import com.four.yy.vpn.entity.Country

class ServerAdapter : RecyclerView.Adapter<ServerAdapter.LocationViewHolder>() {


    private var itemList: MutableList<Country>? = null
    private var onItemClickListener: OnItemClickListener? = null
    fun setList(itemList: MutableList<Country>) {
        this.itemList = itemList
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, country: Country)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var item: Country
        private val cardView = view.findViewById<LinearLayout>(R.id.home_service)
        private val img = view.findViewById<ImageView>(R.id.home_country_logo)
        private val tv = view.findViewById<TextView>(R.id.home_country_tv)
        private val imgChoose = view.findViewById<ImageView>(R.id.item_choose)


        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(item: Country) {
            this.item = item
            item.src?.let { img.setBackgroundResource(it) }
            if (item.name?.contains("Faster Server") == true) {
                tv.text = item.name +"-" +item.city
            } else {
                tv.text = item.name+"-" +item.city
            }

            if (item.isChoose == true) {
                imgChoose.setImageResource(R.mipmap.choose)
                cardView.setBackgroundResource(R.drawable.disconnect_background)
            } else {
                imgChoose.setImageResource(R.mipmap.no_choose)
                cardView.setBackgroundResource(R.color.transparent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_server, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(itemList?.get(position) ?: Country())
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(it, itemList?.get(position) ?: Country())
        }
    }

}