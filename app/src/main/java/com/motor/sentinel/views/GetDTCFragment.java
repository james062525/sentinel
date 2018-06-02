package com.motor.sentinel.views;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.motor.sentinel.R;
import com.motor.sentinel.config.ObdConfig;
import com.motor.sentinel.connecting.Gateway;
import com.motor.sentinel.connecting.bluetooth.BluetoothService;
import com.motor.sentinel.connecting.bluetooth.BluetoothServiceStub;
import com.motor.sentinel.obd.commands.ObdCommand;
import com.motor.sentinel.utils.myProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.motor.sentinel.connecting.Gateway.STATE_IDLE;


public class GetDTCFragment extends Fragment {

  private static final Logger LOG =
    LoggerFactory.getLogger( GetDTCFragment.class.getSimpleName() );

  private View view;
  private ArrayAdapter<String> tdcArrayAdapter;
  private ArrayList<String> getItems;

  // progressbar
  private myProgressBar bar;

  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState ) {
    view = inflater.inflate( R.layout.fragment_list, container, false );

    // set title name
    TextView textView = (TextView) view.findViewById( R.id.list_ls_text );
    textView.setText( R.string.tdc_title );

    bar = new myProgressBar( view, R.id.list_ls_bar );

    return view;
  }

  @Override
  public void onActivityCreated( Bundle savedInstanceState ) {
    super.onActivityCreated( savedInstanceState );

    // start obd GDC commands
    getItems = new ArrayList<String>();
    ListView listView = (ListView) view.findViewById( R.id.list_ls_view );

    tdcArrayAdapter =
      new ArrayAdapter( view.getContext(), R.layout.fragment_list_name, getItems );

    listView.setAdapter( tdcArrayAdapter );
  }

  /**
   *
   * started getting obd trouble code TDC
   *
   */
  ObdCommand obdCommand = null;
  StringBuffer stringBuffer;

  /**
   * BluetoothServicestub
   */
  @Override
  public void onStart() {
    super.onStart();

    bar.start();
    if ( obdCommand == null ) {
      // wrap BluetoothService as ObdCommand
      ObdConfig conf = new ObdConfig( getActivity() );
      Gateway gw = (Gateway) new BluetoothServiceStub( msgHandler, conf );
      obdCommand = new ObdCommand( getActivity(), gw );

      // create linkSocket
      if( !obdCommand.connect() ) {
        bar.stop();
        Toast.makeText( getActivity(), R.string.state_create_socket_error,
            Toast.LENGTH_LONG ).show();
        getActivity().onBackPressed();
        return;
      }

      // Initialize the buffer for outgoing messages
      // stringBuffer = new StringBuffer("");
      // trace error code
      obdCommand.init();

      // send tdc code
      // .............
      // .............
    }
  }


  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LOG.debug( "Log onDestroy" );
    bar.stop();

    if ( obdCommand != null ) {
      obdCommand.close();
    }
  }

  /**
   * The Handler that gets information back from the BluetoothChatService
   */
  private final Handler msgHandler = new Handler() {

    @Override
    public void handleMessage( Message msg ) {
      switch ( msg.what ) {
        case Gateway.STATE_STREAM_CONNECTED:
          //setStatus(getString(R.string.title_connected_to,
          // mConnectedDeviceName));
          tdcArrayAdapter.clear();
          break;

        //case BluetoothService.STATE_CONNECTING:
        //  setStatus(R.string.title_connecting);
        //  break;
        //case RequestCode.STATE_LISTEN:
        case STATE_IDLE:
          //setStatus(R.string.title_not_connected);
          break;

        /*
         *     case Gateway.STATE_DATA_WRITE:
         *     byte[] writeBuf = (byte[]) msg.obj;
         *     // construct a string from the buffer
         *     String writeMessage = new String(writeBuf);
         *     tdcArrayAdapter.add(writeMessage);
         *     break;
         */
        case Gateway.SEND_READ_DATA:
          byte[] readBuf = (byte[])msg.obj;
          // construct a string from the valid bytes in the buffer
          String readMessage = new String( readBuf, 0, msg.arg1 );
          tdcArrayAdapter.add( readMessage );
          break;

        /*
         *     case Constants.MESSAGE_DEVICE_NAME:
         *     // save the connected device's name
         *     mConnectedDeviceName =
         * msg.getData().getString(Constants.DEVICE_NAME);
         *     if (null != activity) {
         *     Toast.makeText(activity, "Connected to "
         + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
         +     }
         +     break;
         */
        case Gateway.STATE_IO_ERROR:
          Toast.makeText( getActivity(), "Bluetooth receiving data failed",
            Toast.LENGTH_SHORT ).show();
          break;
      }
    }
  };
}
