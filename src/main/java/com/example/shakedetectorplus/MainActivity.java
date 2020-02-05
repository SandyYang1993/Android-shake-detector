package com.example.shakedetectorplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private TextView a_x,a_y,a_z,shake_result,b;
    private EditText threshold;
    private SensorManager sensorManager;
    private Sensor acc,baro;
    private boolean accon,shake_detect,baroon,baro_detect;
    private double mag=0;
    private final String not_start="Please press start to detect shake";
    private final String no_acc="There is no ACCELEROMETER in your phone";
    private final String no_baro="There is no BAROMETER in your phone";
    private final String shaking="shake";
    private final String no_shaking="no shake";
    FileOutputStream output;
    private ArrayList<ArrayList<Float>> datalist;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accon=false;
        shake_detect=false;
        baro_detect=false;
        baroon=false;
        try {
                output = openFileOutput("mycsv.csv", Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        this.bindviews();
    }
    private void bindviews()
    {
        a_x=findViewById(R.id.a_x);
        a_y=findViewById(R.id.a_y);
        a_z=findViewById(R.id.a_z);
        b=findViewById(R.id.b);
        threshold=findViewById(R.id.threshold);
        shake_result=findViewById(R.id.shake_result);
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER && shake_detect)
        {
            a_x.setText(event.values[0]+"");
            a_y.setText(event.values[1]+"");
            a_z.setText(event.values[2]+"");


            float thresnum;
            mag=Math.sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);
            String magthreshold=threshold.getText().toString();
            if(magthreshold.matches(""))
                thresnum=12;
            else
                thresnum=Float.parseFloat(magthreshold);

            ArrayList<Float> data = new ArrayList<>();
            data.add(event.values[0]);
            data.add(event.values[1]);
            data.add(event.values[2]);
            data.add(Float.valueOf(event.timestamp));
            data.add(thresnum);
            datalist.add(data);

            if(mag>=thresnum)
            {
                shake_result.setText(shaking);
            }
            else
            {
                shake_result.setText(no_shaking);
            }
        }
        else if(event.sensor.getType()==Sensor.TYPE_PRESSURE && baro_detect)
        {
            b.setText(event.values[0]+"");
        }
    }
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do nothing
    }
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null)
        {
            acc=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
            accon=true;
        }
        else
        {
            shake_result.setText(no_acc);
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)!=null)
        {
            baro=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, baro, SensorManager.SENSOR_DELAY_NORMAL);
            baroon=true;
        }
        else
        {
            b.setText(no_baro);
        }
    }
    public void press_start(View view)
    {
        shake_detect=true;
        this.datalist=new ArrayList<>();
    }
    public void press_stop(View view)
    {
        shake_detect=false;
        shake_result.setText(not_start);
        try {
            for(ArrayList<Float> data:datalist)
            {
                for(Float d:data)
                {
                    output.write(String.valueOf(d).getBytes());
                    output.write(",".getBytes());
                }
                output.write("\n".getBytes());
            }
            output.flush();
            output.close();
        }
        catch (IOException e){}

    }
    public void press_start_b(View view)
    {
        baro_detect=true;
    }
    public void press_stop_b(View view)
    {
        baro_detect=false;
        b.setText("0");
    }
    protected void onPause() {
        super.onPause();
        if(accon||baroon)
        {
            sensorManager.unregisterListener(this);
            baro_detect=false;
            baroon=false;
            shake_detect = false;
            shake_result.setText(not_start);
            accon=false;
            b.setText("0");

        }
    }
}
