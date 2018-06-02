package com.motor.sentinel.obd.commands;

import android.content.Context;

import com.motor.sentinel.connecting.Gateway;
import com.motor.sentinel.obd.operations.CommandMode;

/*
 * Base OBD command.
 */
public class ObdCommand extends ObdStream implements CommandMode {

  private final Context context;
  private final Gateway gateway;

  /**
   *
   *
   */
  public ObdCommand(Context context, Gateway gateway) {
    super(gateway);
    this.context = context;
    this.gateway = gateway;
  }

  public void init(){
    // fixme: comment out this statement?
    if (getState() != Gateway.STATE_STREAM_CONNECTED) {
      return;
    }

    String protocol = gateway.getObdConfig().getObdProtocol();

    // send AT commands
    send(CM_DEFAULT);
    send(CM_RESET);
    send(CM_ECHO + CM_OFF);
    send(CM_LINE_FEED + CM_OFF);
    send(CM_PROTOCOL + protocol);
  }

  public int getState(){
    return super.getState();
  }

  public boolean connect(){
    return super.connect();
  }

  public void send(String s){
    super.send(s);
  }

  public void close(){
    super.close();
  }

  public boolean resend(){
    return super.resend();
  }

}
