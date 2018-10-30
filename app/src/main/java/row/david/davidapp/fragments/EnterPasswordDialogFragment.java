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
import android.widget.Toast;

import row.david.davidapp.MainActivity;
import row.david.davidapp.R;


public class EnterPasswordDialogFragment extends DialogFragment implements View.OnClickListener {
    private Button btnConnectToRover, btnCancel;
    private EditText etWifiPassword;

    // Reference to MainActivity
    private MainActivity mContext;


    public static EnterPasswordDialogFragment newInstance() {
        EnterPasswordDialogFragment fragment = new EnterPasswordDialogFragment();

        // We need the student card serial in this fragment.
        // Therefor we bundle a string when we opened this fragment.
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // We need a reference to the main activity, we get this in onAttach
        // When a developer opens this fragment from a wrong class, the exception is thrown
        if (context instanceof MainActivity) {
            mContext = (MainActivity) context;
        } else {
            throw new IllegalStateException("Start this function from the MainActivity");
        }
    }

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
                    if(mContext.connectToWifi(etWifiPassword.getText().toString())){
                        dismiss();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // When the fragment is dismissed it is closed, so change boolean to false
        mContext.checkInFragmentOpen = false;
    }
}
