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


public class MainActivity extends AppCompatActivity {
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

    public String getRoverPassword() {
        return roverPassword;
    }

    public void setRoverPassword(String roverPassword) {
        this.roverPassword = roverPassword;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Check for available WiFI networks,if none found change UI accordingly.
                scanWifi();
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
                wifiConfiguration.SSID = String.format("\"%s\"", arrayList.get(position));
                // Opens a dialog which enables the user to fill in a password.
                showEditPasswordDialogFragment();
            }
        });

        // Checks if all required permissions are enabled.
        if (checkPermissions()) {
            showToast("Alle permissions zijn ingeschakeld.");
        } else {
            showToast("Niet alle permissions zijn ingeschakeld.");
        }
    }

    private void scanWifi() {
        //Disables the Wifilist until it's done updating, otherwise causes a null-pointer when clicking.
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


    // Shows a toast on screen
    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }


    // Check if all permissions required for the app are granted by the user.
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

    public boolean connectToWifi(String password){
        boolean isConnected = false;
        // Attempts to connect the user to the desired WiFi network.
        try {
            wifiConfiguration.preSharedKey = String.format("\"%s\"", password);
            int netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            // Gives the phone enough time to connect.
            // TODO : Make the thread wait will a connection is made, or connection is refrused.
            Thread.sleep(500);

            // Checks whether the current connection is equal to the desired connection, Thus checking if the connection is succesfull.
            if(wifiManager.getConnectionInfo().getSSID().equals(wifiConfiguration.SSID)){
                showToast("Succesvol verbonden met " + wifiConfiguration.SSID);
                isConnected = true;
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }else{
                showToast("Verbinden met " + wifiConfiguration.SSID + " is niet gelukt..");
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
