package com.motor.sentinel.connecting;

import com.motor.sentinel.config.ObdConfig;


public interface Gateway
{
  /*
   * bluetooth connection status code
   */
  int STATE_IDLE = 10;
  int STATE_STREAM_CONNECTED = 1;
  int STATE_IO_ERROR = 2;
  int MACADDRESS_REQUIRED = 3;
  int SEND_READ_DATA = 4;
  int SEND_WRITE_DATA = 5;
  int STATE_LINK_STREAM_ERROR = 6;
  int STATE_LINK_SOCKET_ERROR = 7;
  int STATE_CREATE_SOCKET_ERROR = 8;
  int EXTRASTATE_RECEIVE_ERROR = 1;
  int EXTRASTATE_WRITE_ERROR = 2;


  ObdConfig getObdConfig();

  /**
   * started connecting
   * @return true:success false:fail
   */
  boolean linkSocket();
  void closeSocket();

  /**
   * @param bytes_array
   */
  void sendBytes(byte[] bytes);

  /**
   * @return return status code
   */
  int getStateCode();
}
