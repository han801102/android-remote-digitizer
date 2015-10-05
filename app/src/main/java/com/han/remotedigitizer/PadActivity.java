package com.han.remotedigitizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PadActivity extends AppCompatActivity {
    private EditText usrInput;
    private TextView printUsrInput;
    private String ip;
    private Socket m_pcSocket;
    private OutputStream out;
    private ProgressDialog dialog;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad);
        Intent intent = this.getIntent();
        ip = intent.getStringExtra("ip");
        dialog = ProgressDialog.show(PadActivity.this,"Connecting","Please wait...");
        handler = new Handler();
        Thread mThread = new Thread(mThreadRunnable);
        mThread.start();


        printUsrInput  = (TextView)findViewById(R.id.textView2);
        usrInput = (EditText)findViewById(R.id.editText2);
        usrInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        usrInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(before == 1){
                            try {
                                byte[] sendstr_big5 = s.toString().getBytes("Big5");
                                out.write(sendstr_big5);
                                usrInput.setText("");
                            }catch (Exception e){
                                System.out.println("Error:" + e.toString());
                            }
                            printUsrInput.setText( s);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                }
        );
    }

    private Runnable mThreadRunnable = new Runnable() {
        @Override
        public void run() {
            int pcPort = 40;
            try {
                m_pcSocket = new Socket(ip,pcPort);
                out = m_pcSocket.getOutputStream();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }catch (Exception e){
                System.out.println("Error:" + e.toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
