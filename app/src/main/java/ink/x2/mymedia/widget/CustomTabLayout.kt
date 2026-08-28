package ink.x2.mymedia.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

data class TabItemData(
    val id: Int,
    val title: String,
    val count: Int = 0
)

class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?=null,
    defStyleAttr: Int = 0
) : TabLayout(context,attrs,defStyleAttr){

    init {
        addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: Tab?) {
                (tab?.customView as? CustomTabItemView)
                    ?.setSelectedState(true)
            }

            override fun onTabUnselected(tab: Tab?) {
                (tab?.customView as? CustomTabItemView)
                    ?.setSelectedState(false)
            }

            override fun onTabReselected(tab: Tab?) {

            }
        })
    }

    fun createTab(tabData: TabItemData){
        addTab(newTab().apply {
            customView = CustomTabItemView(context).apply {
                bind(tabData)
            }
        })
    }
    fun createTabByIndex(tabData: TabItemData,index: Int){
        addTab(newTab().apply {
            customView = CustomTabItemView(context).apply {
                bind(tabData)
            }
        },index)
    }


    private var currentTabData : List<TabItemData> = emptyList()

    fun renderTabs(newData: List<TabItemData>) {
        if (currentTabData.isEmpty()) {
            newData.forEach {
                createTab(it)
            }
            currentTabData = newData
            return
        }

        // 1. 删除 newData 中已经不存在的 Tab
        currentTabData
            .mapIndexedNotNull { index, item ->
                if (newData.none { it.id == item.id }) {
                    index
                } else {
                    null
                }
            }
            .sortedDescending()
            .forEach { index ->
                removeTabAt(index)
            }

        // 2. 按照 newData 的顺序对齐
        newData.forEachIndexed { index, newItem ->

            val currentView =
                getTabAt(index)?.customView as? CustomTabItemView

            val currentData = currentView?.data

            // 当前位置已经是目标 Tab
            if (currentData?.id == newItem.id) {

                // id 没变，但 title/count 变化
                if (currentData != newItem) {
                    currentView.bind(newItem)
                }

                return@forEachIndexed
            }

            // 当前位置不是目标 Tab
            // 看目标 Tab 是否已经存在于后面
            val existIndex = (index + 1 until tabCount)
                .firstOrNull { position ->
                    val data =
                        (getTabAt(position)?.customView as? CustomTabItemView)?.data

                    data?.id == newItem.id
                }

            // 如果存在，先把旧位置删除
            if (existIndex != null) {
                removeTabAt(existIndex)
            }

            // 再插入到正确位置
            createTabByIndex(newItem, index)
        }

        currentTabData = newData
    }
}