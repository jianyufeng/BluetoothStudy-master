package com.qiaojim.bluetoothstudy.frag;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiaojim.bluetoothstudy.ChatController;
import com.qiaojim.bluetoothstudy.ClientThread;
import com.qiaojim.bluetoothstudy.DialogUtil;
import com.qiaojim.bluetoothstudy.MainActivity;
import com.qiaojim.bluetoothstudy.Params;
import com.qiaojim.bluetoothstudy.R;
import com.qiaojim.bluetoothstudy.ServerThread;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Hu on 2017/4/4.
 */
public class DeviceListFragment extends Fragment {

    final String TAG = "DeviceListFragment";

    ListView listView;
    MyListAdapter listAdapter;
    List<BluetoothDevice> deviceList = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter;
    MyBtReceiver btReceiver;


    MainActivity mainActivity;
    Handler uiHandler;

    ClientThread clientThread;
    ServerThread serverThread;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean grantedLocation = true;
        if (requestCode == Params.MY_PERMISSION_REQUEST_CONSTANT) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    grantedLocation = false;
                }
            }
        }
        if (!grantedLocation) {
            toast("权限未授权,不能使用");
            mainActivity.finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            toast("系统无蓝牙");
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bt_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.device_list_view);
        listAdapter = new MyListAdapter();
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 关闭服务器监听
                if (serverThread != null) {
                    serverThread.cancel();
                    serverThread = null;
                    Log.e(TAG, "---------------client item click , cancel server thread ," +
                            "server thread is null");
                }
                if (clientThread != null) {
                    clientThread.cancle();
                }
                shoPress();
                BluetoothDevice device = deviceList.get(position);
                peidui(device); //配对

//              ChatController.getInstance().startChatWith(device, bluetoothAdapter, uiHandler);//与服务器连接进行聊天 也就是客户端连接服务端
     /*             // 通知 ui 连接的服务器端设备
                Message message = new Message();
                message.what = Params.MSG_CONNECT_TO_SERVER;
                message.obj = device;
                uiHandler.sendMessage(message);*/
            }
        });

    }

    private Dialog mDialog;

    public void shoPress() {
        dismissProgress();
        mDialog = DialogUtil.getInstance().showProgressDialog(mainActivity, "正在连接...", false);
        mDialog.show();
    }

    /**
     * 隐藏进度框
     */
    public void dismissProgress() {
        if (mDialog != null && mDialog.isShowing()) {
            DialogUtil.getInstance().dismissDialog(mDialog);
        }
    }

    private void lianjie(BluetoothDevice device) {
        // 开启客户端线程，连接点击的远程设备
        clientThread = new ClientThread(bluetoothAdapter, device, uiHandler, mainActivity);
        new Thread(clientThread).start();
    }

    private void peidui(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {  //未配对，就先配对，再连接
            Method m = null;
            try {
                m = BluetoothDevice.class.getMethod("createBond");
                m.invoke(device);
            } catch (Exception e) {
                e.printStackTrace();
                shoPress();
            }
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            lianjie(device);
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
            toast("正在配对");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        uiHandler = mainActivity.getUiHandler();
        IntentFilter intentFilter = new IntentFilter();
        btReceiver = new MyBtReceiver();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getActivity().registerReceiver(btReceiver, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBluetoothAndLocationPermission() {
        //判断是否有访问位置的权限，没有权限，直接申请位置权限
        if ((ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, Params.MY_PERMISSION_REQUEST_CONSTANT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBluetoothAndLocationPermission();
        }
        // 蓝牙未打开，询问打开
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOnBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBtIntent, Params.REQUEST_ENABLE_BT);
        }
        // 蓝牙已开启
        if (bluetoothAdapter.isEnabled()) {
            showBondDevice();
            // 默认开启服务线程监听
            if (serverThread != null) {
                serverThread.cancel();
            }
            Log.e(TAG, "-------------- new server thread");
            serverThread = new ServerThread(bluetoothAdapter, uiHandler);
            new Thread(serverThread).start();
            ChatController.getInstance().waitingForFriends(bluetoothAdapter, uiHandler);//等待客户端来连接
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btReceiver != null) {
            getActivity().unregisterReceiver(btReceiver);
        }
//        ChatController.getInstance().stopChart();//停止聊天
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enable_visibility:
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                enableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
                startActivityForResult(enableIntent, Params.REQUEST_ENABLE_VISIBILITY);
                break;
            case R.id.discovery:
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                break;
            case R.id.disconnect:
                bluetoothAdapter.disable();
                deviceList.clear();
                listAdapter.notifyDataSetChanged();
                toast("蓝牙已关闭");
                break;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Params.REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    showBondDevice();
                }
                break;
            }
            case Params.REQUEST_ENABLE_VISIBILITY: {
                if (resultCode == 600) {
                    toast("蓝牙已设置可见");
                } else if (resultCode == RESULT_CANCELED) {
                    toast("蓝牙设置可见失败,请重试");
                }
                break;
            }
        }
    }

    /**
     * 用户打开蓝牙后，显示已绑定的设备列表
     */
    private void showBondDevice() {
        deviceList.clear();
        Set<BluetoothDevice> tmp = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice d : tmp) {
            deviceList.add(d);
        }
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Toast 提示
     */
    public void toast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 向 socket 写入发送的数据
     *
     * @param dataSend
     */
    public void writeData(String dataSend) {
//        Message message =new Message();
//        message.obj = dataSend;
//        if (serverThread!=null){
//            message.what=Params.MSG_SERVER_WRITE_NEW;
//            serverThread.writeHandler.sendMessage(message);
//        }
//        if (clientThread!=null){
//            message.what=Params.MSG_CLIENT_WRITE_NEW;
//            clientThread.writeHandler.sendMessage(message);
//        }
        try {
            if (serverThread != null) {
                serverThread.write(dataSend);
            } else if (clientThread != null) {
                clientThread.write(dataSend);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message message = new Message();
            message.what = Params.MSG_Duan_kai;
            uiHandler.sendMessage(message);
        }
    }

    public void writeData(byte[] dataSend) {
//        Message message =new Message();
//        message.obj = dataSend;
//        if (serverThread!=null){
//            message.what=Params.MSG_SERVER_WRITE_NEW;
//            serverThread.writeHandler.sendMessage(message);
//        }
//        if (clientThread!=null){
//            message.what=Params.MSG_CLIENT_WRITE_NEW;
//            clientThread.writeHandler.sendMessage(message);
//        }
        try {
            if (serverThread != null) {
                serverThread.write(dataSend);
            } else if (clientThread != null) {
                clientThread.write(dataSend);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message message = new Message();
            message.what = Params.MSG_Duan_kai;
            uiHandler.sendMessage(message);
        }
    }


    /**
     * 设备列表的adapter
     */
    private class MyListAdapter extends BaseAdapter {

        public MyListAdapter() {
        }

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.layout_item_bt_device, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                viewHolder.deviceMac = (TextView) convertView.findViewById(R.id.device_mac);
                viewHolder.deviceState = (TextView) convertView.findViewById(R.id.device_state);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int code = deviceList.get(position).getBondState();
            String name = deviceList.get(position).getName();
            String mac = deviceList.get(position).getAddress();
            String state;
            if (name == null || name.length() == 0) {
                name = "未命名设备";
            }
            if (code == BluetoothDevice.BOND_BONDED) {
                state = "已配对设备";
                viewHolder.deviceState.setTextColor(getResources().getColor(R.color.green));
            } else {
                state = "可用设备";
                viewHolder.deviceState.setTextColor(getResources().getColor(R.color.red));
            }
            if (mac == null || mac.length() == 0) {
                mac = "未知 mac 地址";
            }
            viewHolder.deviceName.setText(name);
            viewHolder.deviceMac.setText(mac);
            viewHolder.deviceState.setText(state);
            return convertView;
        }

    }

    /**
     * 与 adapter 配合的 viewholder
     */
    static class ViewHolder {
        public TextView deviceName;
        public TextView deviceMac;
        public TextView deviceState;
    }

    /**
     * 广播接受器
     */
    private class MyBtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                toast("开始搜索 ...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                toast("搜索结束");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (isNewDevice(device)) {
                    deviceList.add(device);
                    listAdapter.notifyDataSetChanged();
                    Log.e(TAG, "---------------- " + device.getName());
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                Log.d("aaa", "device name: " + name);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                switch (state) {
                    case BluetoothDevice.BOND_NONE:
                        Log.d("aaa", "BOND_NONE 删除配对");
                        toast("配对失败");
                        dismissProgress();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("aaa", "BOND_BONDING 正在配对");
                        toast("正在配对");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("aaa", "BOND_BONDED 配对成功");
                        toast("配对成功");
                        lianjie(device);
                        break;
                }
            }
        }
    }

    /**
     * 判断搜索的设备是新蓝牙设备，且不重复
     *
     * @param device
     * @return
     */
    private boolean isNewDevice(BluetoothDevice device) {
        boolean repeatFlag = false;
        for (BluetoothDevice d : deviceList) {
            if (d.getAddress().equals(device.getAddress())) {
                repeatFlag = true;
            }
        }
        //不是已绑定状态，且列表中不重复
        return device.getBondState() != BluetoothDevice.BOND_BONDED && !repeatFlag;
    }
}
