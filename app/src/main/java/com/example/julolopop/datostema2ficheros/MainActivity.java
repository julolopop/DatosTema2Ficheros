package com.example.julolopop.datostema2ficheros;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julolopop.datostema2ficheros.pojo.Memoria;
import com.example.julolopop.datostema2ficheros.pojo.RestClient;
import com.example.julolopop.datostema2ficheros.pojo.Resultado;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public EditText edt_Imagenes;
    public EditText edt_Frases;
    public Button btn_Conectar;
    public TextView frase;
    public ImageView imagen;
    public RestClient restClient;
    public Random rnd;
    public static ArrayList<String> imagenes;
    public static ArrayList<String> frases;
    public static final int MAX_TIMEOUT = 2000;
    String textimagenes;
    String textfrases;
    Mostrar m;
    ProgressDialog progreso;
    AsyncHttpClient client;
    long intervalo;
    Memoria miMemoria;
    Resultado resultado;
    String Errores;

    public static final String RAW = "intervalo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializar();

        btn_Conectar.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if (v == btn_Conectar) {


                    textimagenes = edt_Imagenes.getText().toString();
                    textfrases = edt_Frases.getText().toString();
                    imagenes = new ArrayList<>();
                    frases = new ArrayList<>();
                    restClient.setMostar(false);
                    descargarImagenes();
                    descargarFrases();

                    try {
                        m = new Mostrar();
                        m.start();


                    } catch (Exception e) {
                        //
                    }

                }
            }
        });

    }

    void inicializar() {
        rnd = new Random();
        imagen = (ImageView) findViewById(R.id.imgv_imagen);
        frase = (TextView) findViewById(R.id.txv_Frase);
        edt_Imagenes = (EditText) findViewById(R.id.edt_imagenes);
        edt_Frases = (EditText) findViewById(R.id.edt_frases);
        btn_Conectar = (Button) findViewById(R.id.btn_Conectar);
        imagenes = new ArrayList<>();
        frases = new ArrayList<>();
        restClient = new RestClient();
        progreso = new ProgressDialog(MainActivity.this);
        client = new AsyncHttpClient();
        miMemoria = new Memoria(MainActivity.this);
        resultado = new Resultado();

        resultado = this.miMemoria.leerRaw(this.RAW);
        if (resultado.getCodigo()) {
            intervalo = Long.parseLong(resultado.getContenido());
        } else {
            Toast.makeText(MainActivity.this, "Error al leer raw", Toast.LENGTH_SHORT).show();
        }
    }


    public void mostrar(Context context) {
        long timeout = System.currentTimeMillis() + 500;
        long time;
        do {
            time = System.currentTimeMillis();
            if (timeout <= time) {
                try {
                    if (Errores == null) {
                        descargarImagenes();
                        descargarFrases();
                        return;
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {

                    return;
                }
            }
        } while (!restClient.isMostar());


        Picasso.with(context)
                .load(imagenes.get(rnd.nextInt(imagenes.size())))
                .into(imagen);


        frase.setText(frases.get(rnd.nextInt(frases.size())));


        final Runnable CargarI = new Runnable() {
            public void run() {


                String uri = imagenes.get(rnd.nextInt(imagenes.size()));
                URL url = null;
                try {
                    url = new URL(uri);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imagen.setImageBitmap(bmp);
            }
        };
        final Runnable CargarF = new Runnable() {
            public void run() {


                frase.setText(frases.get(rnd.nextInt(frases.size())));
            }
        };
        final Runnable BotonA = new Runnable() {
            public void run() {


                btn_Conectar.setClickable(true);

            }
        };

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();


        for (int i = MAX_TIMEOUT; i <= intervalo; i += MAX_TIMEOUT) {
            timer.scheduleAtFixedRate(CargarF, i, 1, TimeUnit.MILLISECONDS);
            timer.scheduleAtFixedRate(CargarI, i, 1, TimeUnit.MILLISECONDS);
        }
        btn_Conectar.setClickable(false);

        timer.scheduleAtFixedRate(BotonA, intervalo, 1, TimeUnit.MILLISECONDS);

    }

    public class Mostrar extends Thread {


        @Override
        public void run() {

            restClient.setMostar(true);


        }
    }


    public void descargarImagenes() {


        if (URLUtil.isValidUrl(textimagenes)) {
            client.get(textimagenes, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Errores = "Error al leer imagenes " + Calendar.DATE;
                    subida();

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    String texto = responseString;
                    String linea;
                    int ini;
                    while ((ini = texto.indexOf("\n")) != -1) {
                        linea = texto.substring(0, ini);
                        texto = texto.substring(ini + 1, texto.length());
                        imagenes.add(linea);
                    }
                    linea = texto.substring(0, texto.length());
                    imagenes.add(linea);
                    progreso.dismiss();

                }

                @Override
                public void onStart() {
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Descargando ...");
                    progreso.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            client.cancelAllRequests(true);
                        }
                    });
                    progreso.show();
                }


            });
        } else {
            Errores = "Url no Valida  imagenes" + Calendar.DATE;
            subida();
        }


    }


    public void descargarFrases() {


        if (URLUtil.isValidUrl(textfrases)) {
            client.get(textfrases, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Errores = "Error al leer Frases " + Calendar.DATE;
                    subida();

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    String texto = responseString;
                    String linea;
                    int ini;
                    while ((ini = texto.indexOf("\n")) != -1) {
                        linea = texto.substring(0, ini);
                        texto = texto.substring(ini + 1, texto.length());
                        frases.add(linea);
                    }
                    linea = texto.substring(0, texto.length());
                    frases.add(linea);
                    progreso.dismiss();

                    mostrar(MainActivity.this);


                }

                @Override
                public void onStart() {
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Descargando ...");
                    progreso.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            client.cancelAllRequests(true);
                        }
                    });
                    progreso.show();
                }


            });
        } else {
            Errores = "Url no validad frases " + Calendar.DATE;
            subida();
        }


    }

    public final static String WEB = "http://alumno.mobi/~alumno/superior/diaz/upload.php";

    private void subida() {
        final AsyncHttpClient RestClient = new AsyncHttpClient();
        Date fecha = new Date();
        String fichero = "Errores" + fecha.getDate() + " " + fecha.getMonth() + " " + fecha.getYear() + " " + fecha.getHours() + " " + fecha.getMinutes() + " " + fecha.getSeconds() + ".txt";
        final ProgressDialog progreso = new ProgressDialog(MainActivity.this);
        File myFile;
        Boolean existe = true;
        miMemoria.escribirExterna(fichero, Errores, false, "UTF-8");
        myFile = new File(Environment.getExternalStorageDirectory(), fichero);

        RequestParams params = new RequestParams();
        try {
            params.put("fileToUpload", myFile);
        } catch (FileNotFoundException e) {
            existe = false;
            //Toast.makeText(this, "Error en el fichero: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (existe)
            RestClient.post(WEB, params, new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    // called before request is started
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    //progreso.setCancelable(false);
                    progreso.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            RestClient.cancelRequests(getApplicationContext(), true);
                        }
                    });
                    progreso.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();

                    progreso.dismiss();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {

                    Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                    progreso.dismiss();
                }
            });
    }

}



