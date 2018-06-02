package com.motor.sentinel.connecting.bluetooth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.motor.sentinel.config.ObdConfig;
import com.motor.sentinel.obd.operations.CommandMode;

public class BluetoothServiceStub extends BluetoothService implements CommandMode {
  // private final BluetoothAdapter bluetoothAdapter;
  private final Handler handler;
  private final ObdConfig conf;

  private int stateCode;

  private static final Logger LOG =
    LoggerFactory.getLogger( BluetoothServiceStub.class.getSimpleName() );

  public BluetoothServiceStub( Handler hdlr, ObdConfig conf ) {
    super(hdlr, conf);
    this.handler = hdlr;
    this.conf = conf;
    this.stateCode = STATE_IDLE;
  }

  @Override
  public ObdConfig getObdConfig() {
    return conf;
  }

  /**
   * interface Gateway
   * @return stateCode bluetooth status code, ref Gateway.CONST
   */
  @Override
  public int getStateCode() {
    return stateCode;
  }

  /**
   * interface Gateway
   * @param bytes_array
   */
  @Override
  public void sendBytes( byte[] bytes ) {
    if ( (bytes == null) || (bytes.length == 0) ) {
      stateCode = STATE_IDLE;
      return;
    }
    String sz = new String( bytes );

    // byte[] bytes = new byte[256];
    String s = "OK";
    // bytes = s.getBytes();
    byte[] b = s.getBytes();

    LOG.debug( "Log BT sendBytes: '{}'", sz );

    if ( sz.equals( CM_DEFAULT ) ) {
      // handlerSendMessage( SEND_READ_DATA, b.length, b );
      postMessage(b);
    } else if ( sz.equals( CM_RESET ) ) {
      postMessage(b);
    } else if ( sz.equals( CM_ECHO + CM_OFF ) ) {
      postMessage(b);
    } else if ( sz.equals( CM_LINE_FEED + CM_OFF ) ) {
      postMessage(b);
    } else if ( sz.equals( CM_PROTOCOL + "0" ) ) {
      postMessage(b);
    } else {
      postMessage(b);
    }
  }

  /**
   * interface Gateway
   */
  @Override
  public void closeSocket() {
    stateCode = STATE_IDLE;
  }

  @Override
  public boolean linkSocket() {
    stateCode = STATE_STREAM_CONNECTED;
    return true;
  }

  /**
   * send byte[] if handler not null buffer
   * @param mainState
   * @param bufferSize
   * @param buffer
   */
  private void handlerSendMessage( int mainState, int bufferSize, byte[] buffer ) {
    handler.obtainMessage( mainState, bufferSize, -1, buffer ).sendToTarget();
  }

  /**
   * @param mainState ~= what
   * @param extraState ~= arg1, -1 ~= arg2
   */
  private void handlerSendMessageState( int mainState, int extraState ) {
    handler.obtainMessage( mainState, extraState, -1 ).sendToTarget();
  }

  /**
   * @param mainState
   * @param bundle
   */
  private void handlerSendBundle( int mainState, Bundle bundle ) {
    Message msg = handler.obtainMessage( mainState );
    msg.setData( bundle );
    msg.sendToTarget();
  }


  /*
   *
   * delay message sending
   *
   */
  private void postMessage(final byte[] b) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        handlerSendMessage( SEND_READ_DATA, b.length, b );
      }
    }, 500);
  }

  private void postState(final int mainState, final int extraState) {
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        handlerSendMessageState( mainState, extraState );
      }
    }, 500);
  }
}
