package com.dnhsolution.restokabmalang.sistem.produk.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.dnhsolution.restokabmalang.MainActivity;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.sistem.produk.server.ProdukMasterFragment;

import org.jetbrains.annotations.NotNull;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
//    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private static int[] TAB_TITLES = new int[]{R.string.makanan,R.string.minuman,R.string.dll};
    private final Context mContext;
    private final String[] argTab;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        if(MainActivity.Companion.getJenisPajak().equalsIgnoreCase("01"))
            TAB_TITLES = new int[]{R.string.fasilitas,R.string.dll};
        mContext = context;
        argTab = MainActivity.Companion.getArgTab();
    }

    @Override
    public @NotNull Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
//        if(position == 0)
//            return ProdukMasterFragment.newInstance("1");
//        else if(position == 1)
//            return ProdukMasterFragment.newInstance("2");
//        else
//            return ProdukMasterFragment.newInstance("3");
        return ProdukMasterFragment.newInstance(argTab[position]);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}