package com.motor.sentinel.obd.commands;

import com.motor.sentinel.connecting.Gateway;
import com.motor.sentinel.config.ObdConfig;

/*
 *     Gateway <-- interface <-- BluetoothService(Handler,ObdConfig)
 *        |
 *        | wrap
 *        v
 *    ObdStream(Gateway)
 *        |
 *        | extends
 *        v
 *    ObdCommand
 *
 */
/*
 * OBD command.
 */
public abstract class ObdStream {

  private Gateway gateway;

  /**
   * Error messages:
   *  UNABLE TO CONNECT
   *  BUS INIT... ERROR
   *  ?
   *  NO DATA
   *  STOPPED
   *  ERROR
   *
   */
  ObdStream(Gateway gateway) {
    this.gateway = gateway;
  }

  protected ObdConfig getObdConfig(){
    return gateway.getObdConfig();
  }

  protected boolean connect(){
    return gateway.linkSocket();
  }

  protected void send(String s){
    gateway.sendBytes((s + "\r").getBytes());
  }

  protected void close(){
    gateway.closeSocket();
  }

  protected int getState(){
    return gateway.getStateCode();
  }

  protected boolean resend(){
    gateway.sendBytes(("\r").getBytes());
    return true;
  }

}
