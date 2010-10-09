package net.wigle.wigleandroid;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DashboardActivity extends Activity {
  private Handler timer;
  private AtomicBoolean finishing;
  private NumberFormat numberFormat;
  
  private static final int MENU_EXIT = 11;
  private static final int MENU_LIST = 12;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate( final Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.dash );
    
    // media volume
    this.setVolumeControlStream( AudioManager.STREAM_MUSIC );  
    
    finishing = new AtomicBoolean( false );
    numberFormat = NumberFormat.getNumberInstance( Locale.US );
    if ( numberFormat instanceof DecimalFormat ) {
      ((DecimalFormat) numberFormat).setMaximumFractionDigits( 2 );
    }
    
    setupTimer();
  }
  
  private void setupTimer() {
    if ( timer == null ) {
      timer = new Handler();
      final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {              
            // make sure the app isn't trying to finish
            if ( ! finishing.get() ) {
              updateUI();
              
              final long period = 1000L;
              // info("wifitimer: " + period );
              timer.postDelayed( this, period );
            }
            else {
              ListActivity.info( "finishing mapping timer" );
            }
        }
      };
      timer.removeCallbacks( mUpdateTimeTask );
      timer.postDelayed( mUpdateTimeTask, 100 );
    }
  }
  
  private void updateUI() {
    TextView tv = (TextView) findViewById( R.id.runnets );
    tv.setText( ListActivity.lameStatic.runNets + " Run");
    
    tv = (TextView) findViewById( R.id.newnets );
    tv.setText( ListActivity.lameStatic.newNets + " New" );
    
    tv = (TextView) findViewById( R.id.currnets );
    tv.setText( "Visible Nets: " + ListActivity.lameStatic.currNets );
    
    updateDist( R.id.rundist, ListActivity.PREF_DISTANCE_RUN, "Run Distance: " );
    updateDist( R.id.totaldist, ListActivity.PREF_DISTANCE_TOTAL, "Total Distance: " );
    updateDist( R.id.prevrundist, ListActivity.PREF_DISTANCE_PREV_RUN, "Previous Run: " );
    
    tv = (TextView) findViewById( R.id.queuesize );
    tv.setText( "DB Queue: " + ListActivity.lameStatic.preQueueSize );
    
    tv = (TextView) findViewById( R.id.dbNets );
    tv.setText( "DB Nets: " + ListActivity.lameStatic.dbNets );
    
    tv = (TextView) findViewById( R.id.dbLocs );
    tv.setText( "DB Locations: " + ListActivity.lameStatic.dbLocs );
    
    tv = (TextView) findViewById( R.id.gpsstatus );
    Location location = ListActivity.lameStatic.location;
    String gpsStatus = "No Location!";
    if ( location != null ) {
      gpsStatus = location.getProvider();
    }
    tv.setText( "Loc: " + gpsStatus );
  }
  
  private void updateDist( final int id, final String pref, final String title ) {
    final SharedPreferences prefs = this.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
    
    float dist = prefs.getFloat( pref, 0f );
    String distString = null;
    // english? metric? we've got the f'in gun!
    if ( dist > 1000f ) {
      distString = numberFormat.format( dist / 1609.344f ) + " miles";
    }
    else {
      distString = numberFormat.format( dist ) + " meters";
    }
    final TextView tv = (TextView) findViewById( id );
    tv.setText( title + distString );
    
  }
  
  @Override
  public void finish() {
    ListActivity.info( "finish dash." );
    finishing.set( true );
    
    super.finish();
  }
  
  @Override
  public void onDestroy() {
    ListActivity.info( "destroy dash." );
    finishing.set( true );
    
    super.onDestroy();
  }
  
  /* Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu( final Menu menu ) {
    MenuItem item = menu.add(0, MENU_EXIT, 0, "Exit");
    item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
        
    item = menu.add(0, MENU_LIST, 0, "List");
    item.setIcon( android.R.drawable.ic_menu_sort_by_size  );
    
    return true;
  }

  /* Handles item selections */
  @Override
  public boolean onOptionsItemSelected( final MenuItem item ) {
      switch ( item.getItemId() ) {
        case MENU_EXIT:
          MainActivity.finishListActivity( this );
          finish();
          return true;
        case MENU_LIST:
          MainActivity.switchTab( this, MainActivity.TAB_LIST );
          return true;
      }
      return false;
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      ListActivity.info( "onKeyDown: not quitting app on back" );
      MainActivity.switchTab( this, MainActivity.TAB_LIST );
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
