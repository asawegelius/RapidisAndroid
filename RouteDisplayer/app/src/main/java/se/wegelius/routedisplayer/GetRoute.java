package se.wegelius.routedisplayer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.esri.core.io.UserCredentials;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import com.esri.core.tasks.ags.geoprocessing.GPJobParameter;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource;
import com.esri.core.tasks.ags.geoprocessing.GPLong;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;
import java.util.ArrayList;
import java.util.List;

public class GetRoute
        extends Fragment
{
    private String URL = "http://logistics-test.rapidis.com:6080/arcgis/rest/services/SecureRlpAppGetRoute/GPServer/RlpAppGetRoute";
    private Handler handler;
    private EditText id = null;
    private ImageView idImage = null;
    private EditText password = null;
    private ImageView passwordImage = null;
    private Runnable r;
    private ProgressBar spinner;
    private TextView status = null;
    private ImageView userImage = null;
    private EditText userName = null;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View localView = inflater.inflate(R.layout.get_route_layout, container, false);
        localView.setTag("tab3");
        this.status = ((TextView)localView.findViewById(R.id.status_label));
        this.userName = ((EditText)localView.findViewById(R.id.get_route_name));
        this.userName.setOnFocusChangeListener(new FocusListener());
        userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if(result == EditorInfo.IME_ACTION_DONE ||result == EditorInfo.IME_ACTION_NEXT){
                    if(validateAllFields()) {
                        userName.clearFocus();

                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return true;
            }
        });
        this.password = ((EditText)localView.findViewById(R.id.get_route_password));
        this.password.setOnFocusChangeListener(new FocusListener());
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if(result == EditorInfo.IME_ACTION_DONE||result == EditorInfo.IME_ACTION_NEXT){
                    if(validateAllFields()) {
                        password.clearFocus();
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                }
                return true;
            }
        });
        this.id = ((EditText)localView.findViewById(R.id.get_route_id));
        this.id.setOnFocusChangeListener(new FocusListener());
        id.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if(result == EditorInfo.IME_ACTION_DONE||result == EditorInfo.IME_ACTION_NEXT){
                    if(validateAllFields()) {
                        id.clearFocus();
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return true;
            }
        });
        this.userImage = ((ImageView)localView.findViewById(R.id.ic_name));
        this.passwordImage = ((ImageView)localView.findViewById(R.id.ic_password));
        this.idImage = ((ImageView)localView.findViewById(R.id.ic_id));
        this.spinner = ((ProgressBar)localView.findViewById(R.id.progress_bar));
        this.spinner.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.MULTIPLY);
        this.spinner.setVisibility(View.INVISIBLE);
        return localView;
    }

    public void sendJob(String paramString1, String paramString2, long paramLong)
            throws Exception
    {
        UserCredentials localUserCredentials = new UserCredentials();
        localUserCredentials.setUserAccount(paramString1, paramString2);
        ArrayList paramList = new ArrayList();
        GPLong vehicleID = new GPLong("VehicleID");
        vehicleID.setValue(paramLong);
        GPLong calculationID = new GPLong("CalculationID");
        calculationID.setValue(0L);
        paramList.add(vehicleID);
        paramList.add(calculationID);
        Geoprocessor geoprocessor = new Geoprocessor(this.URL, localUserCredentials);
        if (this.handler != null) {
            this.handler.removeCallbacks(this.r);
        }
        this.handler = new Handler();
        this.spinner.setVisibility(View.VISIBLE);
        submitJobAndPolling(geoprocessor, paramList);
    }

    void submitJobAndPolling(final Geoprocessor gpTask,
                             List<GPParameter> params) {
        try {
            GPJobResource gpJobResource = gpTask.submitJob(params);
            GPJobResource.JobStatus jobstatus = gpJobResource.getJobStatus();
            final String jobid = gpJobResource.getJobID();
            status.setTextColor(getResources().getColor(R.color.dark_grey));
            GPLong routeNo = (GPLong)params.get(0);
            status.setText("Fetching route " + routeNo.getValue());


            if (jobstatus != GPJobResource.JobStatus.SUCCEEDED) {


                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GPJobResource jobResource = gpTask.checkJobStatus(jobid);
                            boolean jobComplete = false;
                            GPJobResource.JobStatus jobStatus = jobResource.getJobStatus();
                            switch (jobStatus) {
                                case CANCELLED:
                                    status.setTextColor(getResources().getColor(R.color.dark_grey));
                                    status.setText("The job has been cancelled based on the client's request. ");
                                    jobComplete = true;
                                    break;
                                case CANCELLING:
                                    break;
                                case DELETED:
                                    status.setTextColor(getResources().getColor(R.color.dark_grey));
                                    status.setText("The job has been deleted. ");
                                    jobComplete = true;
                                    break;
                                case DELETING:
                                    break;
                                case EXECUTING:
                                    break;
                                case FAILED:
                                    status.setTextColor(getResources().getColor(R.color.dark_grey));
                                    status.setText("The job execution has failed because of invalid parameters or other geoprocessing failures. ");
                                    jobComplete = true;
                                    break;
                                case NEW_JOB:
                                    break;
                                case SUBMITTED:
                                    break;
                                case SUCCEEDED:
                                    status.setTextColor(getResources().getColor(R.color.dark_grey));
                                    status.setText("Added the route! ");
                                    jobComplete = true;
                                    spinner.setVisibility(View.INVISIBLE);
                                    break;
                                case TIMED_OUT:
                                    status.setTextColor(getResources().getColor(R.color.dark_grey));
                                    status.setText("The job execution has timed out. ");
                                    jobComplete = true;
                                    break;
                                case WAITING:
                                    break;
                                default:
                                    break;
                            }

                            if (jobComplete) {
                                if (jobStatus == GPJobResource.JobStatus.SUCCEEDED) {
                                    System.out.println("GP succeded");
                                    GPJobParameter[] arrayOfGPJobParameter = jobResource.getOutputParameters();
                                    GPParameter localGPParameter1 = gpTask.getResultData(jobid, arrayOfGPJobParameter[0].getParamName());
                                    GPParameter localGPParameter2 = gpTask.getResultData(jobid, arrayOfGPJobParameter[1].getParamName());
                                    Routes.getInstance().setRoute((GPFeatureRecordSetLayer)localGPParameter1);
                                    Routes.getInstance().setRouteElements((GPFeatureRecordSetLayer)localGPParameter2);
                                    GetRoute.this.status.setTextColor(GetRoute.this.getResources().getColor(R.color.dark_grey));
                                    if (Routes.getInstance().getRouteElements().getGraphics().size() > 0)
                                    {
                                        GetRoute.this.status.setText("Added the route! ");
                                        return;
                                    }



                                }else {
                                    System.out.println("GP failed");
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

    public boolean validateAllFields()
    {
        if (!Validation.validName(this.userName.getText().toString()))
        {
            this.userImage.setImageResource(R.drawable.ic_name_orange);
            this.status.setTextColor(getResources().getColor(R.color.orange));
            this.status.setText(R.string.status_no_name);
            return false;
        }
        if (!Validation.validPassword(this.password.getText().toString()))
        {
            this.userImage.setImageResource(R.drawable.ic_name_green);
            this.passwordImage.setImageResource(R.drawable.ic_password_orange);
            this.status.setTextColor(getResources().getColor(R.color.orange));
            this.status.setText(R.string.status_no_password);
            return false;
        }
        if (!Validation.isInteger(this.id.getText().toString()))
        {
            this.userImage.setImageResource(R.drawable.ic_name_green);
            this.passwordImage.setImageResource(R.drawable.ic_password_green);
            this.idImage.setImageResource(R.drawable.ic_id_orange);
            this.status.setTextColor(getResources().getColor(R.color.orange));
            this.status.setText(R.string.status_no_id);
            return false;
        }
        this.userImage.setImageResource(R.drawable.ic_name_green);
        this.passwordImage.setImageResource(R.drawable.ic_password_green);
        this.idImage.setImageResource(R.drawable.ic_id_green);
        return true;
    }

    private class FocusListener
            implements View.OnFocusChangeListener {
        private FocusListener() {
        }

        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                validateAllFields();
            }
            if (validateAllFields()) {
                try {
                    sendJob(userName.getText().toString().trim(), password.getText().toString().trim(), new Long(id.getText().toString().trim()));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (view.getId() == GetRoute.this.userName.getId()) {
                GetRoute.this.userImage.setImageResource(R.drawable.ic_name_green);
                return;
            }
            if (view.getId() == GetRoute.this.password.getId()) {
                GetRoute.this.passwordImage.setImageResource(R.drawable.ic_password_green);
                return;
            }
            while (view.getId() != GetRoute.this.id.getId()) ;
            GetRoute.this.idImage.setImageResource(R.drawable.ic_id_orange);
        }
    }
}
