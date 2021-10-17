package cz.feldis.actualspeed.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.feldis.actualspeed.databinding.LayoutSearchItemBinding

class SearchRecyclerViewAdapter(private val onItemClick: (SearchItem) -> Unit = {}) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    private val values: MutableList<SearchItem> = mutableListOf()

    fun setData(newValues: List<SearchItem>) {
        values.clear()
        values.addAll(newValues)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.title
        holder.contentView.text = item.subtitle
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: LayoutSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}