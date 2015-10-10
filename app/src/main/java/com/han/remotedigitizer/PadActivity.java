package com.han.remotedigitizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private InputStream input;
    private ProgressDialog dialog;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad);
        Intent intent = this.getIntent();
        ip = intent.getStringExtra("ip");
        dialog = new ProgressDialog(PadActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setTitle("Connecting");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
        //dialog = ProgressDialog.show(PadActivity.this,"Connecting","Please wait...");
        handler = new Handler();
        Thread mThread = new Thread(mThreadRunnable);
        mThread.start();


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
                byte [] recvBuf = new byte[2];
                m_pcSocket = new Socket(ip,pcPort);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                out = m_pcSocket.getOutputStream();
                input = m_pcSocket.getInputStream();
                input.read(recvBuf);

                Bundle bundle = new Bundle();
                bundle.putString("result", new String(recvBuf));
                Message connMsg = new Message();
                connMsg.what = 1;
                connMsg.setData(bundle);
                ConnectionHandler.sendMessage(connMsg);
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

    private Handler ConnectionHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(PadActivity.this,msg.getData().getString("result"),Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
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
