Panteamiento :

he he creado una clases con la estenxion thread para mirar si los arrays ya estaban cargados luego de
comprobarlo actualizo una variable booleana sincronizada y dejo que se proceda a cargar la imagen y la frase

para la configuracion se neceita:

    compile 'com.loopj.android:android-async-http:1.4.9'
    compile 'com.squareup.okhttp3:okhttp:3.9.0'
    compile 'com.squareup.picasso:picasso:2.5.2'
    compile 'com.jakewharton.picasso:picasso2-okhttp3-downloader:1.1.0'

y los permisos:


    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

Mejoras :
pues que para la pasar las imagenes no permito que vuelva a leer hasta que termina el intervalo
