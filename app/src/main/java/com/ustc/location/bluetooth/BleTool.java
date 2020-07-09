package com.ustc.location.bluetooth;

import android.os.Message;
import android.widget.Toast;

import com.ustc.location.MainActivity;
import com.ustc.location.location.LbsUtils;
import com.ustc.location.location.Position;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BleTool {
    private DeviceAdapter mDeviceAdapter;
    private Map<String, Position> beaconPositions;
    private  Position userPos;
    private MainActivity ctx;
    // 设置扫描规则：

    public BleTool(MainActivity context,DeviceAdapter adapter){
        this.mDeviceAdapter=adapter;
        ctx=context;
    }
    public void setBeaconPositions(Map<String, Position> pos){
        beaconPositions=pos;
    }
    public void setScanRule() {
        String[] uuids;
        uuids = null;
        UUID[] serviceUuids = null;
        String[] names;
        names = null;

        String mac = "";

        boolean isAutoConnect = false;

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if (beaconPositions.containsKey(bleDevice.getMac())) {
                    mDeviceAdapter.addDevice(bleDevice);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                List<BleDevice> filterScanResultList = new ArrayList<>();
                for (BleDevice bleDevice: scanResultList) {
                    if (beaconPositions.containsKey(bleDevice.getMac())) {
                        filterScanResultList.add(bleDevice);
                    }
                }
                userPos = LbsUtils.calculatePosition(ctx,filterScanResultList,beaconPositions);
                Message m = Message.obtain();
                m.obj=userPos;
                m.what=ctx.REFRESH_POS_SIGNAL;
                ctx.handler.sendMessage(m);
                Toast.makeText(ctx, userPos.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }







}
