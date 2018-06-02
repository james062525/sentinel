package com.motor.sentinel.utils;

import android.os.Handler;
import android.widget.ProgressBar;
import android.view.View;

/** tutorial purpose:
 *  set progress bar maximum value to 100,
 *  increased by 1,
 *  sleep time by 360ms,
 */

public class myProgressBar {

  private boolean isStart = false;
  private ProgressBar bar;
  private View view;
  private int id;  // R.id.device_ls_bar, blah..blah

  public myProgressBar( View view, int id ) {
    this.view = view;
    this.id = id;
    bar = (ProgressBar) view.findViewById( id );
    bar.setVisibility( View.INVISIBLE );
    bar.setMax( 100 );
  }

  public void start() {
    isStart = true;
    bar.setVisibility( View.VISIBLE );
    myBar().start();
  }

  public void stop() {
    isStart = false;
    myBar();
    bar.setVisibility( View.INVISIBLE );
  }

  private Thread myBar() {
    return new Thread( new Runnable() {

          int i = 0;
          int status = 0;
          Handler barHandler = new Handler();
          boolean isRightDirection = true;

          public void run() {
            while ( isStart ) {
              if ( status >= 100 ) {
                isRightDirection = false;
              } else if (status <= 0) {
                isRightDirection = true;
              }

              status = doWork();
              try {
                Thread.sleep( 360 );
              } catch ( InterruptedException e ) {
                // e.printStackTrace();
              }

              barHandler.post( new Runnable() {
                    public void run() {
                      bar.setProgress( status );
                      if (isRightDirection){
                        i++;
                      }
                      else {
                        i--;
                      }
                    }
                  } ); // barHandler
            } // while
          } // run()

          private int doWork() {
            return isRightDirection? i++: i--;
          }
        } ); // return Thread
  } // Thread myBar
}
