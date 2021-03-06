package com.crazylegend.kotlinextensions.abstracts

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crazylegend.kotlinextensions.recyclerview.clickListeners.forItemClickListener
import com.crazylegend.kotlinextensions.views.inflate
import com.crazylegend.kotlinextensions.views.setOnClickListenerCooldown


/**
 * Created by hristijan on 8/16/19 to long live and prosper !
 */


/**
 * It's recommended that you use a Kotlin data class so that diff util can work out of the box
 * Also you should add proguard rules if you're optimizing with R8/Proguard for the viewholder to contain
the methods, since it's using reflection to instantiate the constructor
 * @param T
 * @param VH : RecyclerView.ViewHolder
 * @property viewHolder Class<VH>
 * @property getLayout Int
 * @property forItemClickListener forItemClickListener<T>?
 * @property onLongClickListener forItemClickListener<T>?
 * @constructor
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractListAdapter<T, VH : RecyclerView.ViewHolder>(
        private val viewHolder: Class<VH>,
        areItemsTheSameCallback: (old: T, new: T) -> Boolean? = { _, _ -> null },
        areContentsTheSameCallback: (old: T, new: T) -> Boolean? = { _, _ -> null }
) :
        ListAdapter<T, VH>(GenericDiffUtil(areItemsTheSameCallback, areContentsTheSameCallback)) {
    abstract val getLayout: Int
    abstract fun bindItems(item: T, holder: VH, position: Int)

    var forItemClickListener: forItemClickListener<T>? = null
    var onLongClickListener: forItemClickListener<T>? = null

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item: T = getItem(holder.adapterPosition)
        bindItems(item, holder, holder.adapterPosition)
    }

    /**
     * Use proguard rule as the following
    -keep public class mypackagename.ViewHolder {<methods>;}
     * or annotate it with the @Keep method from androidX
     * @param parent ViewGroup
     * @param viewType Int
     * @return VH
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = setViewHolder(parent.inflate(getLayout))

        holder.itemView.setOnClickListenerCooldown {
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                forItemClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)
        }
        holder.itemView.setOnLongClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                onLongClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)
            true
        }
        return holder
    }


    private fun setViewHolder(inflatedView: View): VH = viewHolder.declaredConstructors.first().newInstance(inflatedView) as VH
}