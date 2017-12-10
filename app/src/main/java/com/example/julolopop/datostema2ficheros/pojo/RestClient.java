package com.example.julolopop.datostema2ficheros.pojo;

import com.example.julolopop.datostema2ficheros.MainActivity;

/**
 * Created by Julolopop on 10/12/2017.
 */

public class RestClient {
    public boolean mostar = false;

    public  synchronized boolean isMostar() {
        if(!mostar){
            notify();
        }
        return mostar;


    }

    public synchronized void setMostar(boolean mostar) {
        if(mostar){
            while (MainActivity.frases.size() == 0 || MainActivity.imagenes.size() == 0){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}
        this.mostar = mostar;

    }
}
