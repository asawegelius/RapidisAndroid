package se.wegelius.routedisplayer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/*
   A custom dialog for upload timestamps
 */
public class UploadVisitedDialogFragment
        extends DialogFragment{
    private boolean m_status;
    private UploadVisitedDialogListener mListener;
    private int position;
    private View dialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_visited_dialog_layout, container, false);
        ((Button)view.findViewById(R.id.btnYes)).setOnClickListener(this.onUpload);
        ((Button)view.findViewById(R.id.btnNo)).setOnClickListener(this.onCancel);
        this.dialog = view;
        getDialog().getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_style_roundcorner));
        return view;
    }

    View.OnClickListener onCancel=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    m_status=false;
                    mListener.onDone(m_status, position, "", "");
                    dismiss();
                }
            };

    View.OnClickListener onUpload=
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    m_status=true;
                    String username =  ((EditText)dialog.findViewById(R.id.dialog_username)).getText().toString();
                    String password = ((EditText)dialog.findViewById(R.id.dialog_password)).getText().toString();
                    mListener.onDone(m_status, position, username, password);
                    dismiss();
                }
            };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try
        {
            this.mListener = ((UploadVisitedDialogListener)activity);
            return;
        }
        catch (ClassCastException localClassCastException)
        {
            throw new ClassCastException(activity.toString() + " must implement UploadVisitedDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPosition(int i)
    {
        this.position = i;
    }

    public static abstract interface UploadVisitedDialogListener
    {
        public abstract void onDone(boolean status, int position, String username, String password);
    }
}


