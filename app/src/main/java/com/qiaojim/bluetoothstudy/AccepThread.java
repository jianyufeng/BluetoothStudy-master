package com.qiaojim.bluetoothstudy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * author : 简玉锋
 * e-mail : yufeng_jian@fpi-inc.com
 * date   : 2019/8/11 11:18
 * desc   :
 * version: 1.0
 */
public class AccepThread extends Thread {

    /**
     * 连接的名称
     */
    private static final String NAME = "BluetoothClass";
    /**
     * UUID
     */
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);
    /**
     * 服务端蓝牙Sokcet
     */
    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    /**
     * 线程中通信的更新UI的Handler
     */
    private final Handler mHandler;
    /**
     * 监听到有客户端连接，新建一个线程单独处理，不然在此线程中会堵塞
     */
    private ConnectedThread mConnectedThread;

    public AccepThread(BluetoothAdapter adapter, Handler handler) throws IOException {
        mBluetoothAdapter = adapter;
        this.mHandler = handler;

        // 获取服务端蓝牙socket
        mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
    }

    @Override
    public void run() {
        super.run();
        // 连接的客户端soacket
        BluetoothSocket socket = null;
        // 服务端是不退出的,要一直监听连接进来的客户端，所以是死循环
        while (true) {

            try {
                // 获取连接的客户端socket
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                // 通知主线程更新UI, 获取异常
                mHandler.sendEmptyMessage(Constant.MSG_ERROR);
                e.printStackTrace();
                // 服务端退出一直监听线程
                break;
            }

            if (socket != null) {
                // 管理连接的客户端socket
                manageConnectSocket(socket);
            }
        }
    }

    /**
     * 管理连接的客户端socket
     *
     * @param socket
     */
    private void manageConnectSocket(BluetoothSocket socket) {
        // 只支持同时处理一个连接
        // mConnectedThread不为空,踢掉之前的客户端
        if (mConnectedThread != null) {
            mConnectedThread.cancle();
        }

        // 新建一个线程,处理客户端发来的数据
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
    }

    /**
     * 断开服务端，结束监听
     */
    public void cancle() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void sendData(byte[] data) {
        if (mConnectedThread != null) {
            mConnectedThread.write(data);
        }
    }

}
