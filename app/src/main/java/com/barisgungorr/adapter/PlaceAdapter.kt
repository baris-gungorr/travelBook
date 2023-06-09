package com.barisgungorr.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Placeholder
import androidx.recyclerview.widget.RecyclerView
import com.barisgungorr.model.Place
import com.barisgungorr.travelbook.databinding.RecyclerRowBinding
import com.barisgungorr.view.MapsActivity

class PlaceAdapter(private var placeList: List<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    private val baslik = "KAYIT GEÇMİŞİ"


    class PlaceHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.PlaceHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: PlaceAdapter.PlaceHolder, position: Int) {

        if (position == 0 ) {
            holder.recyclerRowBinding.textView2.text = baslik
            holder.recyclerRowBinding.textView2.visibility = View.VISIBLE
        }else {
            holder.recyclerRowBinding.textView2.visibility = View.GONE
        }

        holder.recyclerRowBinding.recyclerViewTextView.text = placeList.get(position).name

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
       return placeList.size
    }
}