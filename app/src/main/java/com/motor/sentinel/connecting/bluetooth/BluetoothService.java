package com.motor.sentinel.connecting.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.motor.sentinel.config.ObdConfig;
import com.motor.sentinel.connecting.Gateway;
import com.motor.sentinel.connecting.RequestCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothService implements Gateway {

  private static final Logger LOG = LoggerFactory.getLogger(
       BluetoothService.class.getSimpleName() );

  private final BluetoothAdapter bluetoothAdapter;
  private final String macAddress;
  private final boolean isSecure;
  private final long bluetoothTimeout; // ms

  private int stateCode;
  private static BluetoothSocket bluetoothSocket;

  private final ObdConfig obdConfig; //??
  private final Handler handler;

  /**
   * @param Handler handler (to obtain the callback messages)
   * @param ObdConf OdbConf
   */
  BluetoothService( Handler hdlr, ObdConfig conf ) {
    this.handler = hdlr;
    this.obdConfig = conf;
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    this.macAddress = conf.getMacAddress();
    this.isSecure = conf.isSecure();
    this.bluetoothTimeout = conf.getBluetoothTimeout();
    this.stateCode = STATE_IDLE;
  }

  public ObdConfig getObdConfig() {
    return obdConfig;
  }

  /**
   * interface Gateway
   * @return stateCode bluetooth status code, ref Gateway
   */
  public int getStateCode() {
    return stateCode;
  }

  /**
   * interface Gateway
   * @param bytes
   */
  public void sendBytes( byte[] bytes ) {
    if ( (bytes == null) || (bytes.length == 0) ) {
      stateCode = STATE_IDLE;
    }

    streamThread = new StreamThread( bytes );
    streamThread.start();
  }

  /**
   * interface Gateway
   */
  public void closeSocket() {
    stateCode = STATE_IDLE;
    if ( streamThread != null ) {
      try {
        bluetoothSocket.close();
      } catch ( IOException e ) {
        LOG.debug( "Log BT trying to close socket failed: {}", e );
      }
      streamThread = null;
    }
  }


  /**
   * send byte[] if handler not null buffer
   * @param mainState
   * @param bufferSize
   * @param buffer
   */
  private void handlerSendMessage( final int mainState, final int bufferSize, final byte[] buffer ) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        handler.obtainMessage( mainState, bufferSize, -1, buffer ).sendToTarget();
      }
    }, 500);
  }

  /**
   * @param mainState ~= what
   * @param extraState ~= arg1, -1 ~= arg2
   */
  private void handlerSendMessageState( final int mainState, final int extraState ) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        handler.obtainMessage( mainState, extraState, -1 ).sendToTarget();
      }
    }, 500);
  }

  /**
   * @param mainState
   * @param bundle
   */
  private void handlerSendBundle( final int mainState, final Bundle bundle ) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Message msg = handler.obtainMessage( mainState );
        msg.setData( bundle );
        msg.sendToTarget();
      }
    }, 500);
  }

  /*
   *
   * 1). creates secure / inscure socket, linkSocket()
   * 2). connects socket
   * 3). calls linkStream()
   *
   */

  /**
   * interface Gateway
   * @return true: socket connected
   */
  public boolean linkSocket() {

    if ( !obdConfig.isMacAddress() ) {
      stateCode = MACADDRESS_REQUIRED;
      return false;
    }

    // step 1:
    stateCode = STATE_IDLE;
    if ( streamThread != null ) {
      closeSocket(); // close socket, reset connection
    }

    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice( macAddress );
    bluetoothAdapter.cancelDiscovery();
    try {
      if ( isSecure ) {
        bluetoothSocket =
          bluetoothDevice.createRfcommSocketToServiceRecord( RequestCode.SERIAL_UUID );
      } else {
        bluetoothSocket =
          bluetoothDevice.createInsecureRfcommSocketToServiceRecord(
              RequestCode.SERIAL_UUID );
      }
    } catch ( IOException e ) {
      LOG.debug( "Log BT (in)secure creating failed {}", e.getMessage() );
      stateCode = STATE_CREATE_SOCKET_ERROR;
      return false;
    }

    // step 2:
    try {
      bluetoothSocket.connect();
    } catch ( IOException e ) {
      LOG.debug( "Log BT socket connecting failed {}", e.getMessage() );
      stateCode = STATE_LINK_SOCKET_ERROR;
      try {
        bluetoothSocket.close();
      } catch ( IOException ex ) {
        LOG.debug( "Log BT socket closing failed {}", ex.getMessage() );
      }
      return false;
    }

    return linkStream();
  }

  private InputStream inputStream;
  private OutputStream outputStream;
  private StreamThread streamThread = null;
  private byte[] receivedBytes = null;

  // step 3: creates I/O stream
  private boolean linkStream() {
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    try {
      tmpIn = bluetoothSocket.getInputStream();
      tmpOut = bluetoothSocket.getOutputStream();
    } catch ( IOException e ) {
      LOG.debug( "Log BT IO stream not created {}", e );
      stateCode = STATE_LINK_STREAM_ERROR;
      return false;
    }
    inputStream = tmpIn;
    outputStream = tmpOut;

    stateCode = STATE_STREAM_CONNECTED;
    return true;
  }

  /**
   * stream thread
   */
  private class StreamThread extends Thread {

    private byte[] command;

    public StreamThread( byte[] b ) {
      command = new byte[b.length];
      System.arraycopy( b, 0, command, 0, b.length );
    }

    /** Sends the requested command and deals with the response. */
    public void run() {
      synchronized (StreamThread.class ) {
        if ( stateCode == STATE_STREAM_CONNECTED ) {

          try {
            write();
          } catch ( IOException e ) {
            LOG.debug( "Log BT write stream error {}", e );
            stateCode = STATE_IO_ERROR;
            handlerSendMessageState( STATE_IO_ERROR, EXTRASTATE_WRITE_ERROR );
            return;
          }
          try {
            received();
          } catch ( IOException e ) {
            LOG.debug( "Log BT read stream error {}", e );
            stateCode = STATE_IO_ERROR;
            handlerSendMessageState( STATE_IO_ERROR, EXTRASTATE_RECEIVE_ERROR );
          }
        }
      }  // synchronized
    }

    private void write() throws IOException /*, InterruptedException */ {

      outputStream.write( command );
      outputStream.flush();

      if ( handler != null ) {
        handlerSendMessage( SEND_WRITE_DATA, command.length, command );
      }
    }

    private void received() throws IOException {
      ByteArrayOutputStream tmp = new ByteArrayOutputStream();
      final char terminalChar = '>'; // Obd console promote
      char c;
      byte b = 0;

      long maxTimeMillis = System.currentTimeMillis() + bluetoothTimeout;
      // Keep listening to the InputStream while connected
      while ( (stateCode == STATE_STREAM_CONNECTED)
              && (System.currentTimeMillis() < maxTimeMillis) ) {
        if ( ( ( b = (byte) inputStream.read() ) > -1 ) ) {
          c = (char) b;
          if ( c == terminalChar ) {
            byte[] buffer = tmp.toByteArray();
            handlerSendMessage( SEND_READ_DATA, buffer.length, buffer );
            break;
          }
          tmp.write( b ); // append
        }
      } // while
    } // received method
  } // StreamThread

}
