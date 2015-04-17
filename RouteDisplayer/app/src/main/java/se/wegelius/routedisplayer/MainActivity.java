package se.wegelius.routedisplayer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource;
import com.esri.core.tasks.ags.geoprocessing.GPLong;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;
import java.util.ArrayList;
import java.util.List;

/*
    The main activity. Sets up the tabs and content when created. Also handles the upload timestamp job
    that initiates in the list view
 */
public class MainActivity
        extends FragmentActivity
        implements UploadVisitedDialogFragment.UploadVisitedDialogListener
{
    // the url to the ArcGIs rest service for uploading a timestamp (when visiting a route stop)
    private String URL = "http://logistics-test.rapidis.com:6080/arcgis/rest/services/RlpAppSendStatus/GPServer/RlpAppSendStatus";
    private FragmentTabHost mTabHost;
    private Handler handler;
    private Runnable r;

    private View getTabIndicator(Context context, int title, int icon)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ((ImageView)view.findViewById(R.id.imageView)).setImageResource(icon);
        ((TextView)view.findViewById(R.id.textView)).setText(title);
        return view;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mTabHost = ((FragmentTabHost)findViewById(R.id.tabhost));
        this.mTabHost.setup(this, getSupportFragmentManager(), R.id.fragment_container);
        this.mTabHost.addTab(this.mTabHost.newTabSpec("tab1").setIndicator(getTabIndicator(this.mTabHost.getContext(), R.string.map_title_bar,R.drawable.ic_map_view_config)), MapViewFragment.class, null);
        this.mTabHost.addTab(this.mTabHost.newTabSpec("tab2").setIndicator(getTabIndicator(this.mTabHost.getContext(), R.string.route_title_bar, R.drawable.ic_list_view_config)), RouteListFragment.class, null);
        this.mTabHost.addTab(this.mTabHost.newTabSpec("tab3").setIndicator(getTabIndicator(this.mTabHost.getContext(), R.string.get_route_title_bar, R.drawable.ic_get_map_config)), GetRoute.class, null);
    }

    /*
        Prepares the parameters and geoprocessor for submitting a job if status = true.
     */
    public void onDone(boolean status, int position, String username, String password)
    {
        if (status)
        {
            ArrayList localArrayList = new ArrayList();
            GPLong vehicleID = new GPLong("VehicleID");
            vehicleID.setValue(new Long(((Graphic) Routes.getInstance().getRouteElements().getGraphics().get(position)).getAttributeValue("VehicleID").toString()).longValue());
            GPLong calculationID = new GPLong("CalculationID");
            calculationID.setValue(0L);
            localArrayList.add(vehicleID);
            localArrayList.add(calculationID);
            UserCredentials credentials = new UserCredentials();
            if ((username.length() > 0) && (password.length() > 0)) {
                credentials.setUserAccount(username, password);
            }
            if (this.handler != null) {
                this.handler.removeCallbacks(this.r);
            }
            this.handler = new Handler();
            submitJobAndPolling(new Geoprocessor(this.URL, credentials), localArrayList, position);
            return;
        }
    }


    /*
        Submits the job and polls. If it succeeds it changes the status in the singleton Routes for the involved stop to visited
        and notifies the RouteListAdapter that an row entry has changed. (so the image get changed to checked)
     */
    public void submitJobAndPolling(final Geoprocessor gpTask, List<GPParameter> gpParameters, final int position)
    {
        try
        {
            GPJobResource gpJobResource = gpTask.submitJob(gpParameters);
            final GPJobResource.JobStatus jobStatus = gpJobResource.getJobStatus();
            final String jobID = gpJobResource.getJobID();
            if (jobStatus != GPJobResource.JobStatus.SUCCEEDED) {
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GPJobResource jobResource = gpTask.checkJobStatus(jobID);
                            boolean jobComplete = false;
                            GPJobResource.JobStatus jobStatus = jobResource.getJobStatus();
                                switch (jobStatus) {
                                    case CANCELLED:
                                        jobComplete = true;
                                        break;
                                    case CANCELLING:
                                        break;
                                    case DELETED:
                                        jobComplete = true;
                                        break;
                                    case DELETING:
                                        break;
                                    case EXECUTING:
                                        break;
                                    case FAILED:
                                        jobComplete = true;
                                        break;
                                    case NEW_JOB:
                                        break;
                                    case SUBMITTED:
                                        break;
                                    case SUCCEEDED:
                                        jobComplete = true;
                                        break;
                                    case TIMED_OUT:
                                        jobComplete = true;
                                        break;
                                    case WAITING:
                                        break;
                                    default:
                                        break;
                                }


                            if (jobComplete) {
                                if (jobStatus == GPJobResource.JobStatus.SUCCEEDED) {

                                    Routes.getInstance().setVisited(position);
                                    ((RouteListAdapter)((RouteListFragment)MainActivity.this.getSupportFragmentManager().findFragmentByTag("tab2")).getListAdapter()).notifyDataSetChanged();

                                }


                            } else {
                                handler.postDelayed(this, 5000);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    }
                }, 4000);


            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}