package se.wegelius.routedisplayer;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RouteListFragment
        extends ListFragment
{
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
    {
        super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        View localView = paramLayoutInflater.inflate(R.layout.route_list_layout, paramViewGroup, false);
        localView.setTag("tab2");
        setListAdapter(new RouteListAdapter(getActivity().getBaseContext(), getActivity().getSupportFragmentManager()));
        return localView;
    }
}
