package se.wegelius.routedisplayer;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import java.util.ArrayList;

public class RouteListAdapter
        extends BaseAdapter
{
    private final Context context;
    private final FragmentManager fragmentManager;

    public RouteListAdapter(Context paramContext, FragmentManager paramFragmentManager)
    {
        this.context = paramContext;
        this.fragmentManager = paramFragmentManager;
    }

    private Graphic getStop(int position)
    {
        return (Graphic)Routes.getInstance().getRouteElements().getGraphics().get(position);
    }

    public int getCount()
    {
        if (Routes.getInstance().getRouteElements() == null) {
            return 0;
        }
        return Routes.getInstance().getRouteElements().getGraphics().size();
    }

    public Object getItem(int position)
    {

        return getStop(position);
    }

    public long getItemId(int position)
    {

        return getStop(position).getId();
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.route_list_item_layout, parent, false);
        TextView textView = (TextView)view.findViewById(R.id.row_route_name);
        ImageButton imageButton = (ImageButton)view.findViewById(R.id.row_upload_route);
        if (getCount() > 0) {
            Graphic graphic = getStop(position);
            textView.setText(graphic.getAttributeValue("Description").toString());
            imageButton.setTag(graphic);
            imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramAnonymousView) {
                    RouteListAdapter.this.showNoticeDialog(position);
                }
            });

            if (Routes.getInstance().getVisited(position)) {
                imageButton.setImageResource(R.drawable.ic_checked_blue);
            } else {
                imageButton.setImageResource(R.drawable.ic_upload_blue);
            }
        }
            return view;
    }

    public void showNoticeDialog(int paramInt)
    {
        UploadVisitedDialogFragment localUploadVisitedDialogFragment = new UploadVisitedDialogFragment();
        localUploadVisitedDialogFragment.setPosition(paramInt);
        this.fragmentManager.beginTransaction();
        this.fragmentManager.addOnBackStackChangedListener(null);
        localUploadVisitedDialogFragment.show(this.fragmentManager, "NoticeDialogFragment");
    }
}