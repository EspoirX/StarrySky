package com.lzx.musiclib.adapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView

open class EfficientAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val FALLBACK_DELEGATE_VIEW_TYPE = Int.MAX_VALUE - 1
    }

    var items: MutableList<T>? = mutableListOf()
    private val typeHolders: SparseArrayCompat<ViewHolderCreator<T>> = SparseArrayCompat()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = getHolderForViewType(viewType)
            ?: throw NullPointerException("No Holder added for ViewType $viewType")
        return BaseViewHolder(parent, holder.getResourceId())
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(viewHolder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        val holder = getHolderForViewType(viewHolder.itemViewType)
            ?: throw NullPointerException(
                "No Holder added for ViewType " + viewHolder.itemViewType)
        holder.registerItemView(viewHolder.itemView)
        holder.onBindViewHolder(items?.get(position), items, position, holder)
    }

    override fun getItemViewType(position: Int): Int {
        if (items == null) {
            throw NullPointerException("adapter data source is null")
        }
        for (i in 0 until typeHolders.size()) {
            val holder = typeHolders.valueAt(i)
            val data = items?.getOrNull(position)
            if (holder.isForViewType(data, position)) {
                return typeHolders.keyAt(i)
            }
        }

        //找不到匹配的 viewType
        throw NullPointerException(
            "No holder added that matches at position=$position in data source")
    }

    private fun getHolderForViewType(viewType: Int): ViewHolderCreator<T>? {
        return typeHolders.get(viewType)
    }

    /**
     * 直接添加指定 viewType 的 holder
     */
    fun addTypeHolder(holder: ViewHolderCreator<T>) = apply {
        var viewType: Int = typeHolders.size()
        while (typeHolders.get(viewType) != null) {
            viewType++
            require(viewType != FALLBACK_DELEGATE_VIEW_TYPE) {
                "Oops, we are very close to Integer.MAX_VALUE. " +
                    "It seems that there are no more free and " +
                    "unused view type integers left to add another holder."
            }
        }
        return addTypeHolder(viewType, holder)
    }

    /**
     * 直接添加指定 viewType 的 holder
     */
    fun addTypeHolder(viewType: Int, holder: ViewHolderCreator<T>?) = apply {
        if (holder == null) {
            throw java.lang.NullPointerException("holder is null!")
        }
        require(viewType != FALLBACK_DELEGATE_VIEW_TYPE) {
            "The view type = " + FALLBACK_DELEGATE_VIEW_TYPE +
                " is reserved for fallback adapter holder Please use another view type."
        }
        require(typeHolders.get(viewType) == null) {
            "An holder is already registered for the viewType = $viewType. Already registered holder is " +
                typeHolders.get(viewType)
        }
        typeHolders.put(viewType, holder)
    }

    /**
     * 插入数据
     */
    open fun insertedData(position: Int, data: T) = apply {
        try {
            items?.add(position, data)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, items?.size ?: 0 - position)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 删除数据
     */
    open fun removedData(position: Int) = apply {
        try {
            items?.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items?.size ?: 0 - position)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 更新数据
     */
    open fun updateData(position: Int, data: T, payload: Boolean = true) {
        items?.set(position, data)
        if (payload) {
            notifyItemChanged(position, data)
        } else {
            notifyItemChanged(position)
        }
    }

    /**
     * 获取 item
     */
    fun getItem(position: Int): T? = items?.getOrNull(position)

    /**
     * 注册 vieHolder
     */
    fun register(holder: ViewHolderCreator<T>) = apply {
        var viewType: Int = typeHolders.size()
        while (typeHolders.get(viewType) != null) {
            viewType++
            require(viewType != FALLBACK_DELEGATE_VIEW_TYPE) {
                "Oops, we are very close to Integer.MAX_VALUE. " +
                    "It seems that there are no more free" +
                    "and unused view type integers left to add another holder."
            }
        }
        return register(viewType, holder)
    }

    /**
     * 注册 vieHolder
     */
    fun register(viewType: Int, holder: ViewHolderCreator<T>) = apply {
        require(viewType != FALLBACK_DELEGATE_VIEW_TYPE) {
            "The view type = $FALLBACK_DELEGATE_VIEW_TYPE is reserved " +
                "for fallback adapter holder). Please use another view type."
        }
        typeHolders.put(viewType, holder)
    }

    /**
     * 绑定 RecyclerView
     */
    fun attach(recyclerView: RecyclerView) = apply { recyclerView.adapter = this }

    /**
     *  提交数据
     */
    fun submitList(list: MutableList<T>) {
        this.items?.clear()
        this.items?.addAll(list)
        notifyDataSetChanged()
    }
}