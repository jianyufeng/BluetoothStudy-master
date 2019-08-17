package com.qiaojim.bluetoothstudy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Hu on 2017/4/4.
 */

public class ClientThread implements Runnable {

    final String TAG = "ClientThread";

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    Handler uiHandler;
    BluetoothSocket socket;
    OutputStream out;
    InputStream in;
    Context c;

    public ClientThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device,
                        Handler handler, Context context) {
        c = context;
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;
        this.uiHandler = handler;
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Params.UUID));
        } catch (IOException e) {
            e.printStackTrace();
            Message message = new Message();
            message.what = Params.MSG_lian_jie_error;
            uiHandler.sendMessage(message);

        }
        socket = tmp;
    }

    @Override
    public void run() {

        Log.e(TAG, "----------------- do client thread run()");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        try {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, "开始连接。。。。", Toast.LENGTH_SHORT).show();
                }
            });
            socket.connect();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, "连接成功", Toast.LENGTH_SHORT).show();
                }
            });
            Message message = new Message();
            message.what = Params.MSG_CONNECT_TO_SERVER;
            message.obj = device;
            uiHandler.sendMessage(message);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            //reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "-----------do client read run()");

                    byte[] buffer = new byte[1024];
                    int len;
                    String content;
                    try {
                        while ((len = in.read(buffer)) != -1) {
                            byte[] r = new byte[len];
                            System.arraycopy(buffer, 0, r, 0, len);
                            content = HexConvert.BinaryToHexString(r);
//                            content = new String(buffer, 0, len);
                            Message message = new Message();
                            message.what = Params.MSG_CLIENT_REV_NEW;
                            message.obj = content;
                            uiHandler.sendMessage(message);
                            Log.e(TAG, "------------- client read data in while ,send msg ui" + content);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Message message = new Message();
                        message.what = Params.MSG_Duan_kai;
                        uiHandler.sendMessage(message);
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "-------------- exception");
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
            Message message = new Message();
            message.what = Params.MSG_lian_jie_error;
            uiHandler.sendMessage(message);
        }
    }

    public void write(String data) {
//        data = data+"\r\n";
        try {
            if (out != null) {
                out.write(data.getBytes("utf-8"));
            } else {
                out = socket.getOutputStream();
                out.write(data.getBytes("utf-8"));
            }
            Log.e(TAG, "---------- write data ok " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data) {
//        data = data+"\r\n";
        try {
            if (out != null) {
                out.write(data);
            } else {
                out = socket.getOutputStream();
                out.write(data);
            }
            Log.e(TAG, "---------- write data ok ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancle() {
        if (socket != null) {
            try {
                socket.close();
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
