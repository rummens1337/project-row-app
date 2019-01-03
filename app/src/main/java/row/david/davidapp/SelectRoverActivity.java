package row.david.davidapp;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import row.david.davidapp.fragments.EnterPasswordDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;


public class SelectRoverActivity extends AppCompatActivity {
    private WifiConfiguration wifiConfiguration = new WifiConfiguration();
    private WifiManager wifiManager;
    private ListView wifiList;
    private int listSize = 0;
    private List<ScanResult> results;
    private Button btnScan;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private static boolean arrayStatus = true;
    private String roverPassword;
    // Used to check whether there is a fragment open or not.
    public boolean checkInFragmentOpen = false;

    /*
     * START ONCREATE
     */

    /**
     * onCreate is called when the object is instantiated.
     * @param savedInstanceState state in which you can save instances.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_rover);

        // Add a scanbutton, which calls the scanWifi function.
        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener( new View.OnClickListener() {
            /**
             * Set an onclicklistener for the scan button
             * @param v The view to be altered.
             */
            @Override
            public void onClick(View v) {
                // TODO: Check for available WiFI networks,if none found change UI accordingly.
                // Checks if all permissions are met.
                if(checkPermissions()) {
                    scanWifi();
                } else{
                    showToast("Niet alle permissions zijn ingeschakeld.");
                }
            }
        });
        wifiList = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);



         //wifiList is assigned the values of arrayList, which is where SSID's are stored in.
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, arrayList);
        wifiList.setAdapter(adapter);


        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String SSID = String.format("\"%s\"", arrayList.get(position));
                wifiConfiguration.SSID = SSID;

                int netId = -1;
                boolean isSaved = false;
                for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks()) {
                    // Checks whether selected WiFi is already saved, and handles accordingly.
                    if (tmp.SSID.equals(SSID)) {
                        netId = tmp.networkId;
                        wifiManager.enableNetwork(netId, true);
                        isSaved = true;
                        try {
                            // TODO : Make the thread wait till a connection is made, or connection is refused.
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        checkWifiConnection();
                        break;
                    }
                }
                if(!isSaved){
                    showEditPasswordDialogFragment();
                }
            }
        });
        checkPermissions();
    }

    /*
     * END ONCREATE
     */

    /**
     * Disables the Wifilist until it's done updating, otherwise causes a null-pointer when clicking.
     */
    private void scanWifi() {
        wifiList.setEnabled(false);
        if (!wifiManager.isWifiEnabled()) {
            showToast("WiFi wordt aangezet...");
            wifiManager.setWifiEnabled(true);
        }
        showToast("WiFi netwerken scannen...");
        arrayList.clear();
        wifiManager.startScan();
        results = wifiManager.getScanResults();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }


    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        /**
         *
         * @param context The current context
         * @param intent The current intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            //Status ensures that the list is not clickable till it finishes updating.
            //This prevents null-pointers.
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            // Checks if name has a certain structure, following would be correct: david#1, david#11
            Pattern p = Pattern.compile("^david#[0-9][0-9]?[0-9]?$");
            for (ScanResult result : results) {
                Matcher match = p.matcher(result.SSID.toString());
                if (match.matches()) {
                    arrayList.add(result.SSID);
                    adapter.notifyDataSetChanged();
                }
            }
            // Fills the array with dummy data if no rover networks are found.
            // Also ensures that the list is updated if no results are found, filtering old results.
            // Once again, preventing a null-pointer ^_^.
            if (arrayList.isEmpty()) {
                for (int i = 0; i < 8; i++) {
                    arrayList.add("AndroidWifi");
                    arrayList.add("david#" + (i + 1) + " [Dit is een dummy]");
                }
                adapter.notifyDataSetChanged();
                showToast("Geen rover netwerken gevonden..");
            }
            // Enables wifilist after it's updated. It's now clickable again.
            wifiList.setEnabled(true);
        }
    };


    /**
     * Shows a toast on screen
     * @param message Message to be shown in toast.
     */
    private void showToast(String message) {
        Toast.makeText(SelectRoverActivity.this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Check if all permissions required for the app are granted by the user.
     * @return boolean permissions granted, which can be true or false.
     */
    private boolean checkPermissions() {

        List<String> permissionsList = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.INTERNET);
        }

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    1);
            return false;
        }
        return true;
    }

    /**
     * Attempts to connect the user to the desired WiFi network.
     * @param password The password that was entered.
     * @return boolean wifiConnected, which can be true or false.
     */
    public boolean connectToWifi(String password){
        try {
            wifiConfiguration.preSharedKey = String.format("\"%s\"", password);
            int netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            // Gives the phone enough time to connect.
            // TODO : Make the thread wait till a connection is made, or connection is refused.
            Thread.sleep(500);

        }catch(Exception e){
            e.printStackTrace();
        }
        // Checks if the wifi connection was succesfull, by calling the checkWifiConnection function.
        return checkWifiConnection();
    }

    /**
     * Checks whether the current connection is equal to the desired connection, Thus checking if the connection is succesfull.
     * @return Whether the user is connected to the correct WiFi signal.
     */
    public boolean checkWifiConnection(){
        boolean isConnected = false;
        try{
        if(wifiManager.getConnectionInfo().getSSID().equals(wifiConfiguration.SSID)){
            showToast("Succesvol verbonden met " + wifiConfiguration.SSID);
            isConnected = true;
            startActivity(new Intent(SelectRoverActivity.this, WebViewActivity.class));
        }else{
            showToast("Verbinden met " + wifiConfiguration.SSID + " is niet gelukt..");
            showEditPasswordDialogFragment();
        }
    }catch(Exception e){
        showToast("Er ging iets goed mis in het maken van een verbinding... :O");
        e.printStackTrace();
    }
        return isConnected;
    }


    /**
     * Open Dialogfragment which handles the userinput for password.
     */

    private void showEditPasswordDialogFragment() {
        if (checkInFragmentOpen) return;
        checkInFragmentOpen = true;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("tag");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment enterPassword = EnterPasswordDialogFragment.newInstance();
        enterPassword.setCancelable(false);
        enterPassword.show(ft, "tag");
    }
}
