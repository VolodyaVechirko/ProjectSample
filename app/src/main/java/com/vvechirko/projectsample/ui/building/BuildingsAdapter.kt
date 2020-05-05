package com.vvechirko.projectsample.ui.building

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vvechirko.projectsample.*
import com.vvechirko.projectsample.data.model.Building
import kotlinx.android.synthetic.main.item_building.view.*

class BuildingsAdapter(
    private val interaction: Interaction
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Interaction {
        fun onDeliveryClicked(item: Building)
        fun onRestaurantClicked(item: Building)
    }

    private var items: List<Building> = listOf()

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    fun setItems(list: List<Building>) {
        this.items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) Header(parent.inflate(R.layout.item_building_header))
        else Holder(parent.inflate(R.layout.item_building))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Holder) {
            holder.bind(items[position - 1])
        }
    }

    override fun getItemCount(): Int {
        // show header if items is not empty
        return if (items.isNotEmpty()) items.size + 1 else items.size
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.clipToOutline = true
            view.imageView.round(R.dimen.itemLocationImageRadius)
        }

        fun bind(l: Building) = with(itemView) {
            imageView.load(l.image)
            tvTitle.text = l.title
            tvAddress.text = l.address

            var distanceLessRadius = true
            // distance label
            if (l.distance != null && l.active) {
                distanceView.visibility = View.VISIBLE
                tvDistance.text = l.distanceFormatted
                distanceLessRadius = l.distance!! <= l.radius
            } else {
                distanceView.visibility = View.GONE
            }

            isActivated = l.active
            isEnabled = l.active // to disable foreground ripple
            if (l.active) {
//                tvStatus.text = context.getString(R.string.opened)
//                tvTime.text = context.getString(R.string.close_at_s, l.closedAt)
                imageView.saturation(1f)

                divider.visibility = View.VISIBLE
                btnGroup.visibility = View.VISIBLE

                // enable btnDelivery if location has DELIVERY orderType
//                btnDelivery.isEnabled = l.orderTypes.contains(OrderPlace.DELIVERY)
//                        && distanceLessRadius
                btnDelivery.setOnClickListener {
                    interaction.onDeliveryClicked(l)
                }

                // enable btnRestaurant if location has IN_RESTAURANT or WITH_ME orderType
                btnRestaurant.setOnClickListener {
                    interaction.onRestaurantClicked(l)
                }
            } else {
//                tvStatus.text = context.getString(R.string.closed)
//                tvTime.text = context.getString(R.string.open_at_s, l.openedAt)
                imageView.saturation(0f)

                divider.visibility = View.INVISIBLE
                btnGroup.visibility = View.GONE
            }
        }
    }

    class Header(view: View) : RecyclerView.ViewHolder(view)
}