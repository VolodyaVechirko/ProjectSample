package com.vvechirko.projectsample.ui.address

import com.vvechirko.projectsample.R
import com.vvechirko.projectsample.data.api.PlaceComplete
import com.vvechirko.projectsample.data.api.PlaceDetails
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vvechirko.projectsample.inflate
import kotlinx.android.synthetic.main.item_address_suggest.view.*

class AddressAdapter(
    val interaction: Interaction
) : RecyclerView.Adapter<AddressAdapter.Holder>() {

    interface Interaction {
        fun onCompletionSelected(p: PlaceComplete)
        fun onMyPlaceSelected(p: PlaceDetails)
    }

    private var items: MutableList<Any> = mutableListOf()

    private var myPlace: PlaceDetails? = null
    fun setMyPlace(it: PlaceDetails?) {
        if (myPlace != null) {
            items.remove(myPlace!!)
        }
        myPlace = it
        setItems(items)
    }

    fun setItems(list: List<Any>) {
        items = list.toMutableList()
        if (myPlace != null) {
            items.add(0, myPlace!!)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(parent.inflate(R.layout.item_address_suggest))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(it: Any) {
            with(itemView) {
                if (it is PlaceComplete) {
                    tvAddress.text = it.mainText
                    tvRegion.text = it.secondaryText
                    image.setImageResource(R.drawable.ic_location_pin_black_24dp)
                    setOnClickListener { v ->
                        interaction.onCompletionSelected(it)
                    }
                } else if (it is PlaceDetails) {
                    tvAddress.text = it.mainText
                    tvRegion.text = it.secondaryText
                    image.setImageResource(R.drawable.ic_location_pin_black_24dp)
                    setOnClickListener { v ->
                        interaction.onMyPlaceSelected(it)
                    }
                }
            }
        }
    }
}