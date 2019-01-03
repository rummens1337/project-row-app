package row.david.davidapp.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import row.david.davidapp.SelectRoverActivity;
import row.david.davidapp.R;


public class EnterPasswordDialogFragment extends DialogFragment implements View.OnClickListener {
    private Button btnConnectToRover, btnCancel;
    private EditText etWifiPassword;

    // Reference to SelectRoverActivity
    private SelectRoverActivity mContext;

    public static EnterPasswordDialogFragment newInstance() {
        EnterPasswordDialogFragment fragment = new EnterPasswordDialogFragment();

        // Therefore we bundle a string when we opened this fragment.
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Called when the fragment is shown.
     * @param context a reference to the context (activity or fragment) where it's shown from.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // We need a reference to the main activity, we get this in onAttach
        // When a developer opens this fragment from a wrong class, the exception is thrown
        if (context instanceof SelectRoverActivity) {
            mContext = (SelectRoverActivity) context;
        } else {
            throw new IllegalStateException("Start this function from the SelectRoverActivity");
        }
    }

    /**
     * onCreate is called when the object is instantiated.
     * @param savedInstanceState state in which you can save instances.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.enter_password_dialog_fragment_layout, container, false);
        etWifiPassword = (EditText) view.findViewById(R.id.etWifiPassword);

        btnCancel = view.findViewById(R.id.cancel_entering_password);
        btnCancel.setOnClickListener(this);

        btnConnectToRover = view.findViewById(R.id.connect_to_rover);
        btnConnectToRover.setOnClickListener(this);

        return view;
    }

    /**
     * An onClick listener for the entire fragment.
     * @param v The fragment view, which covers the entire screen.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_entering_password:
                try {
                    dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.connect_to_rover:
                try {
                    // Checks if connection was succesfull. If so, releasing the dialog fragment.
                    // TODO: Create a 2-way authenticator which also asks for the password on the rover.
                    if(mContext.connectToWifi(etWifiPassword.getText().toString())){
                        dismiss();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * Called when the dialogFragment is dismissed.
     * @param dialog the dialogFragment.
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // When the fragment is dismissed it is closed, so change boolean to false
        mContext.checkInFragmentOpen = false;
    }
}
