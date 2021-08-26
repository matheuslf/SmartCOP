package com.unesc.smartcop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    // Componentes.
    private Button btnCapturarPlaca;
    private TextView io_lbl_mensagem_excelente;
    private TextView io_lbl_mensagem_hora;
    private EditText io_txf_placa_capturada;
    private EditText io_txf_estado_capturado;

    // Permissão de Leitura, Camera, Estado e Armazenamento.
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE
    };

    private static final int START_PERMISSION = 98;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        io_txf_estado_capturado = findViewById(R.id.io_txf_estado_capturado);
        io_txf_placa_capturada = findViewById(R.id.io_txf_placa_capturada);

        /*
         * Recupera a mensagem de agrado.
         */
        io_lbl_mensagem_excelente = findViewById(R.id.io_lbl_mensagem_excelente);

        /*
         * Recupera a mensagem da hora corrente.
         */
        io_lbl_mensagem_hora = findViewById(R.id.io_lbl_mensagem_hora);

        btnCapturarPlaca = findViewById(R.id.btnCapturarPlaca);
        btnCapturarPlaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, OcrCaptureActivity.class),99);
            }
        });

        /*
         * Esconde o teclado.
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /*
         * Thread para controle da hora.
         */
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread= new Thread(runnable);
        myThread.start();
    }

    /*
     * Método de START da Activity.
     */
    @Override
    protected void onStart() {

        super.onStart();

        /*
        Checa as permissões do usuário ...
        */
        int permission_camera = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA);
        int permission_read = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permission_write = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE);

        /*
        Se não for garantida pelo SO ...
        */
        if (permission_camera != PackageManager.PERMISSION_GRANTED || permission_read != PackageManager.PERMISSION_GRANTED || permission_write != PackageManager.PERMISSION_GRANTED || permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    START_PERMISSION
            );
        }
    }

    /*
     * Método de Resume da Activity.
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /*
     * Método que faz o trabalho para manipular a hora.
     */
    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();
                    String curTime = hours + ":" + (minutes < 10 ? "0"+minutes : minutes) + ":" + (seconds < 10 ? "0"+seconds : seconds);
                    io_lbl_mensagem_hora.setText(curTime);

                    if (hours < 12) {
                        io_lbl_mensagem_excelente.setText("Bom dia, tenha uma excelente "+new DateFormatSymbols().getWeekdays()[new GregorianCalendar().get(Calendar.DAY_OF_WEEK)]+" !");
                    }
                    else if (hours >= 12 && hours <= 18) {
                        io_lbl_mensagem_excelente.setText("Boa tarde, tenha um ótimo dia !");
                    }
                    else {
                        io_lbl_mensagem_excelente.setText("Boa noite, tenha um ótimo final de dia !");
                    }

                } catch (Exception e) {
                }
            }
        });
    }

    /*
     * Thread para rodar o controle da hora.
     */
    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        io_txf_placa_capturada.setText(data.getStringExtra("PLACA"));
        io_txf_estado_capturado.setText(data.getStringExtra("ESTADO"));
    }
}
