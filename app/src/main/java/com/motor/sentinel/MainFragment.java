package com.motor.sentinel;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.motor.sentinel.config.ObdConfig;
import com.motor.sentinel.config.ObdConfigFragment;
import com.motor.sentinel.connecting.RequestCode;
import com.motor.sentinel.connecting.bluetooth.RequestEnable;
import com.motor.sentinel.views.DeviceListFragment;
import com.motor.sentinel.views.GetDTCFragment;

public class MainFragment extends Fragment implements OnClickListener, RequestCode {

  private static final Logger LOG = LoggerFactory.getLogger(MainFragment.class.getSimpleName());
  private ImageView start, diagnose, troubleCode, information, configuration;
  public static String bluetoothMac;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);
    LOG.debug("Log from {} onCreateView", view);

    start = view.findViewById(R.id.main_start);
    start.setOnClickListener(this);

    diagnose = view.findViewById(R.id.main_diagnose);
    diagnose.setOnClickListener(this);

    troubleCode = view.findViewById(R.id.main_troublecode);
    troubleCode.setOnClickListener(this);

    information = view.findViewById(R.id.main_information);
    information.setOnClickListener(this);

    configuration = view.findViewById(R.id.main_config);
    configuration.setOnClickListener(this);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    RequestEnable req = new RequestEnable();
    if(!req.enableIntent(getActivity(), this)) {
      getActivity().finish();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

  }



  @Override
  public void onClick(View v) {
    ObdConfig conf = new ObdConfig(getActivity());
    switch (v.getId()) {
      case R.id.main_start:
        run(new DeviceListFragment(), REQUEST_MAC_ADDRESS);
        break;
      case R.id.main_diagnose:
        Toast.makeText(getActivity(), "Under Construction", Toast.LENGTH_LONG).show();
        break;
      case R.id.main_troublecode:
        if (!conf.isMacAddress()) {
          Toast.makeText(getActivity(), "Paired bluetooth devices required", Toast.LENGTH_LONG).show();
        } else {
          run(new GetDTCFragment(), REQUEST_TROUBLECODE_RESULT);
        }
        break;
      case R.id.main_information:
        Toast.makeText(getActivity(), "Under Construction", Toast.LENGTH_LONG).show();
        break;
      case R.id.main_config:
        getFragmentManager()
            .beginTransaction()
            .replace(R.id.ContainerLayout, new ObdConfigFragment())
            .addToBackStack(null)
            .commit();

        break;
    }
  }

  private void run(Fragment fg, int requestCode) {
    fg.setTargetFragment(this, requestCode);
    getActivity().getFragmentManager()
        .beginTransaction()
        .replace(R.id.ContainerLayout, fg, fg.getTag())
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    switch (requestCode) {
      case REQUEST_MAC_ADDRESS:
        bluetoothMac = data.getStringExtra(REQUEST_EXTRA_MAC_ADDRESS_STRING);
        new ObdConfig(getActivity()).saveMacAddress(bluetoothMac);
        break;

      case REQUEST_ENABLE_BT:
        if (resultCode == Activity.RESULT_OK) {
          start.setEnabled(true);
          configuration.setEnabled(true);
        } else {
          Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
          getActivity().finish();
        }
      default:
        break;
    }
    // invoke parent activity
    super.onActivityResult(requestCode,resultCode,data);
  }
}
