package com.krashkrosh748199.GFolksy.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.krashkrosh748199.GFolksy.fragments.ContactsFragment
import com.krashkrosh748199.GFolksy.fragments.GroupsFragment

class HomeTabAdapter(
    context: Context,
    fm:FragmentManager,
    internal var totalTabs:Int
):FragmentPagerAdapter(fm){
    override fun getCount(): Int {
     return totalTabs
    }

    override fun getItem(position: Int): Fragment {
          if (position == 0){
                return ContactsFragment()
          }
          if (position==1){
            return GroupsFragment()
          }
        return ContactsFragment()
    }

}