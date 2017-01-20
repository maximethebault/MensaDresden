package org.tud.mensaapp.ui.finding.mensa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.tud.mensaapp.R;

import java.util.ArrayList;
import java.util.List;


public class TabMensaFragment extends Fragment {

    private class TabPagerAdapter extends FragmentPagerAdapter {
        private final List<Class<? extends Fragment>> mFragmentClassList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        TabPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return mFragmentClassList.get(position).newInstance();
            } catch (java.lang.InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mFragmentClassList.size();
        }

        void addFragment(Class<? extends Fragment> fragmentClass, String title) {
            mFragmentClassList.add(fragmentClass);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private static final String TAG = "TabMensaFragment";

    private static final String FLAG_CURRENTLY_SELECTED_TAB_INDEX = "FLAG_CURRENTLY_SELECTED_TAB_INDEX";

    private TabPagerAdapter adapter;
    private ViewPager viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new TabPagerAdapter(getChildFragmentManager());
        adapter.addFragment(ListMensaFragment.class, "List");
        adapter.addFragment(MapMensaFragment.class, "Map");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.mensa_offers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.job_offers_pager);
        //set adapter to ViewPager
        viewPager.setAdapter(adapter);
        if (savedInstanceState != null) {
            final int currentlySelectedTabIndex = savedInstanceState.getInt(FLAG_CURRENTLY_SELECTED_TAB_INDEX, -1);
            if (currentlySelectedTabIndex > -1) {
                viewPager.setCurrentItem(currentlySelectedTabIndex);
            }
        }


        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.job_offers_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        /*inflater.inflate(R.menu.job_offers_options, menu);
        menu.findItem(R.id.job_offers_filter).setIcon(
                new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_filter_list).actionBar().color(Color.WHITE)
        );*/
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.job_offers_filter:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (viewPager != null) {
            int i = viewPager.getCurrentItem();
            outState.putInt(FLAG_CURRENTLY_SELECTED_TAB_INDEX, i);
        }
    }
}
