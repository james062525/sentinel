package com.motor.sentinel.config;

import android.content.Context;
import android.widget.Toast;

public class ExceptionActivity {

  private final Context context;

  ExceptionActivity(Context context) {
    this.context = context;
  }

  public void message(String s) {
      Toast.makeText(context, s, Toast.LENGTH_LONG).show();
  }

  public void message(int resId) {
      Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
  }

}
