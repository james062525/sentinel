package com.motor.sentinel.views;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.motor.sentinel.R;
import com.motor.sentinel.connecting.RequestCode;
import com.motor.sentinel.utils.myProgressBar;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceListFragment extends Fragment implements OnClickListener, RequestCode {

    private static final Logger LOG =
        LoggerFactory.getLogger(DeviceListFragment.class.getSimpleName());

    private View view;
    private ArrayAdapter < String > foundArrayAdapter;
    private ArrayList < String > foundItems;

    // progressbar
    private myProgressBar bar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_device_list,
            container,
            false);

        bar = new myProgressBar(view, R.id.device_ls_bar);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button buttonScan = (Button) view.findViewById(R.id.device_bt_scan);
        buttonScan.setOnClickListener(this);

        // to find paired Devices
        ArrayList < String > pairedItems = new ArrayList < String > ();

        // to set up the ListView for paired devices
        ListView pairedListView = (ListView) view.findViewById(
            R.id.device_ls_paired_devices);
        pairedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        pairedListView.setTextFilterEnabled(true);

        ArrayAdapter < String > pairedArrayAdapter =
            new ArrayAdapter(
                view.getContext(), android.R.layout.simple_list_item_checked,
                pairedItems);

        pairedListView.setAdapter(pairedArrayAdapter);
        pairedListView.setOnItemClickListener(clickListener);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set < BluetoothDevice > pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            view.findViewById(R.id.device_txt_paired_devices).setVisibility(
                View.VISIBLE);
            int i = pairedDevices.size();

            for (BluetoothDevice bt: pairedDevices) {
                pairedItems.add(bt.getName() + "\n" + bt.getAddress());
            }
            pairedArrayAdapter.notifyDataSetChanged();
        } else {
            view.findViewById(R.id.device_txt_paired_devices).setVisibility(
                View.INVISIBLE);
        }

        /*
         * to find new devices
         *
         */
        foundItems = new ArrayList < String > ();
        ListView foundListView = (ListView) view.findViewById(
            R.id.device_ls_found_devices);
        foundListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        foundListView.setTextFilterEnabled(true);

        foundArrayAdapter =
            new ArrayAdapter(
                getActivity(), android.R.layout.simple_list_item_checked,
                foundItems);

        foundListView.setAdapter(foundArrayAdapter);
        foundListView.setOnItemClickListener(clickListener);

        /*
         *
         * register the broadcastReceiver
         *
         */

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getActivity().registerReceiver(calledReceiver, filter);
    }

    /** selected device for pairing */
    private AdapterView.OnItemClickListener clickListener =
        new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView < ? > av,
                View v,
                int position,
                long id) {

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) v).getText().toString();
                String bluetoothMAC = info.substring(info.length() - 17);
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothMAC);

                if ((device.getBondState() == BluetoothDevice.BOND_BONDED) ||
                    (createBond(device))) {
                    Intent intent = new Intent();
                    intent.putExtra(REQUEST_EXTRA_MAC_ADDRESS_STRING, bluetoothMAC);

                    getTargetFragment()
                        .onActivityResult(
                            getTargetRequestCode(), REQUEST_MAC_ADDRESS, intent);
                }

                getActivity().onBackPressed();
            }
        };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.device_bt_scan:
                startScanDevices();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(calledReceiver);
    }

    private final BroadcastReceiver calledReceiver =
        new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    Toast.makeText(getActivity(), "State changed",
                        Toast.LENGTH_SHORT).show();
                    if (state == BluetoothAdapter.STATE_ON) {
                        Toast.makeText(getActivity(), "Enabled",
                            Toast.LENGTH_SHORT).show();
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(
                        getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                    bar.start();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    bar.stop();
                    Toast.makeText(
                        getActivity(), "Discovery finished", Toast.LENGTH_LONG).show();
                        view.findViewById(R.id.device_txt_found_devices).setVisibility(
                            View.VISIBLE);
                        foundArrayAdapter.notifyDataSetChanged();
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice bt = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);
                    String name = bt.getName();
                    String address = bt.getAddress();

                    if (bt.getBondState() != BluetoothDevice.BOND_BONDED) {
                        Toast.makeText(
                                getActivity(), "Found device: " + name + " : " + address,
                                Toast.LENGTH_SHORT)
                            .show();
                        foundItems.add(name + "\n" + address);
                    }
                }
            }
        };

    private void startScanDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        foundArrayAdapter.clear();
        bluetoothAdapter.startDiscovery();
    }

    private boolean createBond(BluetoothDevice btDevice) {
        Boolean returnValue = false;
        try {
            Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            returnValue = (Boolean) createBondMethod.invoke(btDevice);
        } catch (Exception e) {
            e.getStackTrace();
            return returnValue.booleanValue();
        }
        return returnValue.booleanValue();
    }
}
