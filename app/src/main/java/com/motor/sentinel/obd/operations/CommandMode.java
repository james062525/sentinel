package com.motor.sentinel.obd.operations;

/**
 *
  CommandMode $01 – Request Live Data
  CommandMode $02 – Request Freeze Frames
  CommandMode $03 – Request Stored Trouble Codes
  CommandMode $04 – Clear/Reset Stored Emissions Related Data
  CommandMode $05 – Request Oxygen Sensors Test Results
  CommandMode $06 – Request On-Board System Tests Results
  CommandMode $07 – Request Pending Trouble Codes
  CommandMode $08 – Request Control of On-Board Systems
  CommandMode $09 – Request Vehicle Information
  CommandMode $0A – Request Permanent Trouble Codes
 *
 */
public interface CommandMode {

  String CM_ON = "1";
  String CM_OFF = "0";
  String CM_DEFAULT = "AT D"; // Set all to defaults
  String CM_RESET = "AT Z";   // Reset Obd
  String CM_ECHO = "AT E"; // E# - echo 0 = off, 1 = on
  String CM_LINE_FEED = "AT L"; // Line feed off:"AT L0"
  String CM_SPACES = "AT S";   // Spaces off:"AT S0"
  String CM_HEADERS = "AT H";  // Headers off
  String CM_PROTOCOL = "AT SP"; // Set Protocol to 0 "Auto", ... etc

  String CM_M01 = "01"; // show live data
  String CM_DTC = "03";   // DTC trouble code
}
