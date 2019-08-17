package com.qiaojim.bluetoothstudy.frag;

import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiaojim.bluetoothstudy.HexConvert;
import com.qiaojim.bluetoothstudy.MainActivity;
import com.qiaojim.bluetoothstudy.MyTextView;
import com.qiaojim.bluetoothstudy.Params;
import com.qiaojim.bluetoothstudy.R;

/**
 * Created by Hu on 2017/4/4.
 */
public class DataTransFragment extends Fragment implements View.OnClickListener {

    TextView connectNameTv;
    ListView showDataLv;
    EditText inputEt;
    Button sendBt;
    ArrayAdapter<String> dataListAdapter;

    MainActivity mainActivity;
    Handler uiHandler;

    BluetoothDevice remoteDevice;
    Button kaiji;
    Button guanji;
    MyTextView sahngya;
    boolean keyikongzhi = false;
    boolean cancel = false;


    private TextView state_sahngya;
    private TextView state_yunxing;
    private TextView state_guanji;
    private TextView state_daiji;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!cancel && keyikongzhi) {
                sendMsg(HexConvert.hexStringToBytes(sahngya_code));
                sahngya.postDelayed(runnable, 1000);
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_data_trans, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        connectNameTv = (TextView) view.findViewById(R.id.device_name_tv);
        showDataLv = (ListView) view.findViewById(R.id.show_data_lv);
        inputEt = (EditText) view.findViewById(R.id.input_et);
        sendBt = (Button) view.findViewById(R.id.send_bt);
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgSend = inputEt.getText().toString();
                sendMsg(msgSend);
                inputEt.setText("");
//                ChatController.getInstance().sendMessage(msgSend);//发出消息
            }
        });

        dataListAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_item_new_data);
        showDataLv.setAdapter(dataListAdapter);
        kaiji = view.findViewById(R.id.tv_kaiji);
        guanji = view.findViewById(R.id.tv_guanji);
        sahngya = view.findViewById(R.id.tv_sahngya);
        sahngya.setDownUpLister(new MyTextView.DownUpLister() {
            @Override
            public void downLister() {
                if (canSend()) {
                    cancel = false;
                    sahngya.postDelayed(runnable, 1000);
                }
            }

            @Override
            public void upLister() {
                sahngya.removeCallbacks(runnable);
                cancel = true;
                if (canSend()) {
                   //待机
                    sendMsg(HexConvert.hexStringToBytes(sahnya_tanqi_code));
                }
            }
        });
        kaiji.setOnClickListener(this);
        guanji.setOnClickListener(this);

        state_yunxing = view.findViewById(R.id.tv_state_yunxing);
        state_daiji = view.findViewById(R.id.tv_state_daiji);
        state_guanji = view.findViewById(R.id.tv_state_gaunji);
        state_sahngya = view.findViewById(R.id.tv_state_sahngya);
    }

    public boolean canSend() {
        if (!keyikongzhi) {
            Toast.makeText(mainActivity, "设备未连接", Toast.LENGTH_LONG).show();
        }
        return keyikongzhi;
    }

    private void sendMsg(byte[] msg) {
        if (!keyikongzhi) {
            Toast.makeText(mainActivity, "设备未连接", Toast.LENGTH_LONG).show();
            return;
        }
        Message message = new Message();
        message.what = Params.MSG_WRITE_DATA;
        message.obj = msg;
        uiHandler.sendMessage(message);
    }

    private void sendMsg(String msg) {
        Message message = new Message();
        message.what = Params.MSG_WRITE_DATA;
        message.obj = msg;
        uiHandler.sendMessage(message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        uiHandler = mainActivity.getUiHandler();
    }

    /**
     * 显示连接远端(客户端)设备
     */
    public void receiveClient(BluetoothDevice clientDevice) {
        this.remoteDevice = clientDevice;
        connectNameTv.setText("连接设备: " + remoteDevice.getName());
        keyikongzhi = true;
    }

    /**
     * 显示新消息
     *
     * @param newMsg
     */
    public void updateDataView(String newMsg, int role) {

       /* if (role == Params.REMOTE) {
            String remoteName = remoteDevice.getName() == null ? "未命名设备" : remoteDevice.getName();
            newMsg = remoteName + " : " + newMsg;
        } else if (role == Params.ME) {
            newMsg = "我 : " + newMsg;
        }*/
        Drawable d = ContextCompat.getDrawable(mainActivity, R.mipmap.ok);
        d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
        switch (newMsg) {
        /*    case kaiji_code:
                String rk = kaiji_code_return.replace(" ", "");
                sendMsg(HexConvert.hexStringToBytes(rk));
                break;
            case guanji_code:
                String rg = guanji_code_return.replace(" ", "");
                sendMsg(HexConvert.hexStringToBytes(rg));
                break;
            case sahngya_code:
                String sy = sahngya_code_return.replace(" ", "");
                sendMsg(HexConvert.hexStringToBytes(sy));
                break;
            case sahnya_tanqi_code:
                String syt = sahnya_tanqi_code_return.replace(" ", "");
                sendMsg(HexConvert.hexStringToBytes(syt));
                break;*/
            case kaiji_code_return: //运行
                state_yunxing.setCompoundDrawables(d, null, null, null);
                state_sahngya.setCompoundDrawables(null, null, null, null);
                state_daiji.setCompoundDrawables(null, null, null, null);
                state_guanji.setCompoundDrawables(null, null, null, null);
                break;
            case guanji_code_return: //关机
                state_guanji.setCompoundDrawables(d, null, null, null);
                state_sahngya.setCompoundDrawables(null, null, null, null);
                state_daiji.setCompoundDrawables(null, null, null, null);
                state_yunxing.setCompoundDrawables(null, null, null, null);
                break;
            case sahngya_code_return: //上压
                state_sahngya.setCompoundDrawables(d, null, null, null);
                state_guanji.setCompoundDrawables(null, null, null, null);
                state_daiji.setCompoundDrawables(null, null, null, null);
                state_yunxing.setCompoundDrawables(null, null, null, null);
                break;
            case sahnya_tanqi_code_return: //待机
                state_daiji.setCompoundDrawables(d, null, null, null);
                state_guanji.setCompoundDrawables(null, null, null, null);
                state_sahngya.setCompoundDrawables(null, null, null, null);
                state_yunxing.setCompoundDrawables(null, null, null, null);
                break;

            default:
                state_yunxing.setCompoundDrawables(null, null, null, null);
                state_sahngya.setCompoundDrawables(null, null, null, null);
                state_daiji.setCompoundDrawables(null, null, null, null);
                state_guanji.setCompoundDrawables(null, null, null, null);
                break;

        }

//        if (newMsg.equals()){

//        }
        dataListAdapter.add(newMsg);
    }

    /**
     * 客户端连接服务器端设备后，显示
     *
     * @param serverDevice
     */
    public void connectServer(BluetoothDevice serverDevice) {
        keyikongzhi = true;
        this.remoteDevice = serverDevice;
        connectNameTv.setText("连接设备: " + remoteDevice.getName());
    }

    /**
     * 连接断开显示
     *
     * @param
     */
    public void duankaiServer() {
        keyikongzhi = false;
        if (remoteDevice != null) {
            connectNameTv.setText("设备断开连接: " + remoteDevice.getName());
        } else {
            connectNameTv.setText("设备断开连接");
        }
        state_yunxing.setCompoundDrawables(null, null, null, null);
        state_sahngya.setCompoundDrawables(null, null, null, null);
        state_daiji.setCompoundDrawables(null, null, null, null);
        state_guanji.setCompoundDrawables(null, null, null, null);
    }

    /**
     * 连接失败
     *
     * @param
     */
    public void lianjieErrorServer() {
        keyikongzhi = false;
        if (remoteDevice != null) {
            connectNameTv.setText("设备连接失败: " + remoteDevice.getName());
        } else {
            connectNameTv.setText("设备连接失败");
        }
        state_yunxing.setCompoundDrawables(null, null, null, null);
        state_sahngya.setCompoundDrawables(null, null, null, null);
        state_daiji.setCompoundDrawables(null, null, null, null);
        state_guanji.setCompoundDrawables(null, null, null, null);
    }

    private static final String kaiji_code_return = "AA 01 82 45 BB";
    private static final String guanji_code_return = "AA 02 82 45 BB";
    private static final String sahngya_code_return = "AA 03 82 45 BB"; //
    private static final String sahnya_tanqi_code_return = "AA 00 82 45 BB";


    private static final String kaiji_code = "AA 01 45 82 BB";
    private static final String guanji_code = "AA 02 45 82 BB";
    private static final String sahngya_code = "AA 03 45 82 BB"; //
    private static final String sahnya_tanqi_code = "AA 00 45 82 BB";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_kaiji: //发送一次
                sahngya.removeCallbacks(runnable);
                cancel = true;
                if (canSend()) {
                    sendMsg(HexConvert.hexStringToBytes(kaiji_code));
                }
                break;
            case R.id.tv_guanji: //发送一次
                sahngya.removeCallbacks(runnable);
                cancel = true;
                if (canSend()) {
                    sendMsg(HexConvert.hexStringToBytes(guanji_code));
                }
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sahngya != null) {
            sahngya.removeCallbacks(runnable);
        }
        cancel = true;
    }
}
