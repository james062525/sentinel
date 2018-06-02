
package com.motor.sentinel.connecting;

import java.util.UUID;


public interface RequestCode {

  // bluetooth
  int REQUEST_ENABLE_BT = 1;
  int REQUEST_MAC_ADDRESS = 2;
  String REQUEST_EXTRA_MAC_ADDRESS_STRING = "2";

  // main fragment
  int REQUEST_DIAGNOSTIC_RESULT = 3;
  int REQUEST_TROUBLECODE_RESULT = 4;
  int REQUEST_INFORMATION = 5;
  int REQUEST_PREFERENCE = 6;

  // common uuid serial number
  UUID SERIAL_UUID =
      UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

}
