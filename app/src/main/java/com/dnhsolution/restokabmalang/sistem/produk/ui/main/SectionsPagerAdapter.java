package com.dnhsolution.restokabmalang.sistem.produk.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.data.DataFragment;
import com.dnhsolution.restokabmalang.sistem.produk.lokal.LokalFragment;
import com.dnhsolution.restokabmalang.sistem.produk.server.ServerFragment;
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment;

import org.jetbrains.annotations.NotNull;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
//    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public @NotNull Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position == 0)
            return new ServerFragment();
        else return new LokalFragment();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 1;
    }
}