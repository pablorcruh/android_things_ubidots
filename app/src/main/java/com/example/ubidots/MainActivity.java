package com.example.ubidots;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String token = "BBFF-q4U92kzOXYKcFq3z7nxI6g4UX9kMR5";
    private final String idIluminacion="5d02adad1d84724c9907b41f";
    private final String idBoton="5d02adc71d84724cfe61fa72";

    private final String PIN_BUTTON = "BCM23";
    private Gpio mButtonGpio;
    private Double buttonstatus= 0.0;
    private Handler handler = new Handler();
    private Runnable runnable  = new UpdateRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PeripheralManager service = PeripheralManager.getInstance();
        try{
            mButtonGpio = service.openGpio(PIN_BUTTON);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(mCallback);

        }catch(IOException  e){
            Log.e(TAG,"Error en PeripheralIO API",e);
        }
        handler.post(runnable);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler =null;
        runnable = null;
        if(mButtonGpio !=null){
            mButtonGpio.unregisterGpioCallback(mCallback);
            try{
                mButtonGpio.close();
            }catch(IOException e){
                Log.e(TAG, "Error en Peripheral API,",e);
            }
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "Boton pulsado");
            if(buttonstatus ==0.0) buttonstatus= 1.0;
            else buttonstatus = 0.0;
            final Data boton =new Data();
            boton.setVariable(idBoton);
            boton.setValue(buttonstatus);
            ArrayList<Data> message = new ArrayList<Data>() {{add(boton);}};
            UbiClient.getClient().sendData(message, token);
            return true;
        }
    };

    private class UpdateRunner implements Runnable{

        @Override
        public void run() {
            readLDR();
            Log.i(TAG,"Ejecución de acción periódica");
            handler.postDelayed(this,5000);
        }
    }

    private void readLDR(){
        Data iluminacion = new Data();
        ArrayList<Data> message = new ArrayList<Data>();
        Random rand = new Random();
        float valor = rand.nextFloat() * 5.0f;
        iluminacion.setVariable(idIluminacion);
        iluminacion.setValue((double) valor);
        message.add(iluminacion);
        UbiClient.getClient().sendData(message, token);
    }

}
