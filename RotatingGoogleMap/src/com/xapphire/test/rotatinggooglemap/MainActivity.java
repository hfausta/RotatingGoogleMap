package com.xapphire.test.rotatinggooglemap;
/*
 * http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
 */

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements SensorEventListener {

	private GoogleMap googleMap;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magneticField;
	private float[] accelerometerValues = new float[3];
	private float[] magneticFieldValues = new float[3];
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeMap();
        googleMap.getUiSettings().setCompassEnabled(true);
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void initializeMap() {
    	if(googleMap == null) {
    		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		/*
		 * http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
		 * https://github.com/iutinvg/compass/blob/master/src/com/iutinvg/compass/Compass.java
		 */
		
		final float alpha = 0.99f;
		
		synchronized(this) {
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues[0] = alpha * accelerometerValues[0] + (1 - alpha) * event.values[0];
				accelerometerValues[1] = alpha * accelerometerValues[1] + (1 - alpha) * event.values[1];
				accelerometerValues[2] = alpha * accelerometerValues[2] + (1 - alpha) * event.values[2];
			}
			if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticFieldValues[0] = alpha * magneticFieldValues[0] + (1 - alpha) * event.values[0];
				magneticFieldValues[1] = alpha * magneticFieldValues[1] + (1 - alpha) * event.values[1];
				magneticFieldValues[2] = alpha * magneticFieldValues[2] + (1 - alpha) * event.values[2];
			}
			
			if(accelerometerValues != null && magneticFieldValues != null) {
				float R[] = new float[9];
				float I[] = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I, accelerometerValues, magneticFieldValues);
				
				if(success) {
					float orientation[] = new float[3];
					SensorManager.getOrientation(R, orientation);
					float bearingRotation = (((float) Math.toDegrees(orientation[0])) + 360) % 360;
					Toast.makeText(this, "Rotation " + bearingRotation, Toast.LENGTH_SHORT).show();
					
					googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
			        .target(googleMap.getCameraPosition().target)
			        .zoom(googleMap.getCameraPosition().zoom)
			        .bearing(bearingRotation)
			        .build()), 1, null);
				}
			}	
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}
}
