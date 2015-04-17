package se.wegelius.routedisplayer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.esri.core.map.Graphic;

/*
    A custom list adapter (a bridge between the list view and its data)
 */
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
        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.route_list_item_layout, parent, false);
        }
        TextView textView = (TextView)convertView.findViewById(R.id.row_route_name);
        ImageButton imageButton = (ImageButton)convertView.findViewById(R.id.row_upload_route);
        if (getCount() > 0) {
            Graphic graphic = getStop(position);
            textView.setText(graphic.getAttributeValue("Description").toString());
            imageButton.setTag(graphic);
            imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    RouteListAdapter.this.showNoticeDialog(position);
                }
            });

            if (Routes.getInstance().getVisited(position)) {
                imageButton.setImageResource(R.drawable.ic_checked_blue);
            } else {
                imageButton.setImageResource(R.drawable.ic_upload_blue);
            }
        }
            return convertView;
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
