package sl.wo.ip.uisl.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sl.wo.ip.R
import sl.wo.ip.bean.SlAppBean
import sl.wo.ip.utils.SpaceLockerUtils

class AppListAdapter(private val dataList: MutableList<SlAppBean>,val context: Context) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAppName: TextView = itemView.findViewById(R.id.tv_app_name)
        var imgAppIcon: ImageView = itemView.findViewById(R.id.img_app_icon)
        var imgDownState: ImageView = itemView.findViewById(R.id.img_down_state)
        init {
            imgDownState.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // 处理 item 点击事件
                    MainViewFun.liveItemClick.postValue(position)
                }
            }
        }
    }
    fun addAdapterData(newData: MutableList<SlAppBean>) {
        dataList.removeAll(newData)
        dataList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun setVisibility(isVisible: Boolean, itemView: View) {
        val param = itemView.layoutParams as RecyclerView.LayoutParams
        if (isVisible) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT
            param.width = LinearLayout.LayoutParams.MATCH_PARENT
            itemView.visibility = View.VISIBLE
        } else {
            itemView.visibility = View.GONE
            param.height = 0
            param.width = 0
        }
        itemView.layoutParams = param
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        // 加载自定义的布局文件
        val itemView: View = inflater.inflate(R.layout.item_app, parent, false)
        // 创建ViewHolder对象
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = dataList.getOrNull(position)?: SlAppBean()
        if (SpaceLockerUtils.isOnRight == 0) {
            //在左边
            setVisibility(itemData.isLocked, holder.itemView)
        } else {
            //在右边
            setVisibility(true, holder.itemView)
        }
        holder.imgAppIcon.setImageDrawable(itemData.appIconSl)
        holder.tvAppName.text = itemData.appNameSl
        if (itemData.isLocked) {
            holder.imgDownState.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_lock)
            )
        } else {
            holder.imgDownState.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_no_lock)
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}