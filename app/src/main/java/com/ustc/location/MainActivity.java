package com.ustc.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ustc.location.bluetooth.BleTool;
import com.ustc.location.bluetooth.DeviceAdapter;
import com.ustc.location.database.DataBase;
import com.ustc.location.location.Anchor;
import com.ustc.location.location.LbsUtils;
import com.ustc.location.location.Position;
import com.ustc.location.location.UserPos;
import com.ustc.location.network.JSONParser;
import com.ustc.location.utils.FileUtils;
import com.ustc.location.utils.Uri2PathUtil;
import com.ustc.location.view.MyMapView;
import com.clj.fastble.BleManager;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity  {
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final double BASE_POINT_X = 120.72366575339005;
    public static final double BASE_POINT_Y = 31.277829136467126;
    private static String uploadURL = "http://192.168.79.1:80/upload.php";
    public static final int REFRESH_POS_SIGNAL= 0;
    private MyMapView mapView;
    private DeviceAdapter mDeviceAdapter;
    private BleTool bletool;
    private UserPos userPos;
    private LbsThread  lbsThread;
    private SyncThread  syncThread;
    private SimpleMarkerSymbol markerSymbol;
    private List<Anchor> syncResult;
    private Vector<Anchor> anchors;
    private Map<String, Position> beaconPositions;
    private DataBase db ;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
        String userName=getIntent().getStringExtra("userName");
        System.out.println(userName);
        userPos=new UserPos(userName,new Position(BASE_POINT_X ,BASE_POINT_Y));
        mapView = (MyMapView) findViewById(R.id.myMapView);
        db = new DataBase(getApplicationContext());
        String path =  "/sdcard/map/shape/room.shp";
        if (!FileUtils.fileIsExists(path)){
            FileUtils.getInstance(this).copyAssetsToSD("shape","map/shape");
        }
        try{
            loadShpFile(path);
            mapView.initPaintTool(this);
            Intent intent = new Intent();
            intent.setClass(this, SyncActivity.class);
            intent.putExtra("sync_kind",2);
            this.startActivityForResult(intent,2);
            refreshPostion();
        }catch (Exception e){
            Toast.makeText(this, "定位系统初始化失败！", Toast.LENGTH_SHORT).show();
        }

    }
    private void initBle(){
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        mDeviceAdapter = new DeviceAdapter(this);
        bletool=new BleTool(this,mDeviceAdapter);
        bletool.setBeaconPositions(beaconPositions);
    }
    private void startLbs(){
        lbsThread=new LbsThread();
        lbsThread.start();
        syncThread=new SyncThread();
        syncThread.start();
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    public void setPosition(Position p){
        this.userPos.setPos(p);
    }
    public static void verifyStoragePermissions(Activity activity) {

        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH_POS_SIGNAL:{
                    userPos.setPos((Position) msg.obj);
                    //System.out.println(userPos);
                    refreshPostion();
                }
            }
        }
    };
    public void refreshPostion(){
        mapView.showNativeInfo();
        mapView.drawLayer.removeAll();
        mapView.drawTool.deactivate();
        mapView.drawTool.point = new Point();
        mapView.drawTool.drawGraphic = new Graphic(mapView.drawTool.point, markerSymbol);
        mapView.drawTool.point.setXY(userPos.getPos().getPosX(), userPos.getPos().getPosY());
        mapView.drawTool.sendDrawEndEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 点击菜单的事件响应
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_select:
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,10000);
                break;
            case R.id.menu_lbs:
                Intent mi = new Intent();
                mi.setClass(this, MainActivity.class);
                startActivity(mi);
                finish();
                break;
            case R.id.menu_navi: {
                Intent i = new Intent();
                i.setClass(this, NaviActivity.class);
                i.putExtra("userPos",userPos);
                startActivity(i);
                finish();
                break;
            }
            case R.id.menu_anchor:
                Intent i = new Intent();
                i.setClass(this, AnchorActivity.class);
                i.putExtra("anchors",anchors);
                startActivity(i);
                finish();
                break;
        }
        return true;
    }
    /**
     * 加载本地shp文件
     */
    private void loadShpFile(String path){
        mapView.loadLocalMap(path,1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 10000) {
            Uri uri = data.getData();
            String path =  Uri2PathUtil.getRealPathFromUri(this,uri);
            System.out.println(path);
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
            if(path != null && (path.endsWith(".shp") || path.endsWith(".SHP") )){
                loadShpFile(path);
                return;
            }
            Toast.makeText(this, "文件格式不对，请重新选择！", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                bletool.setScanRule();
                bletool.startScan();
            }
        }
        if (requestCode == 2 ) {
            if (resultCode == 0) {
                Toast.makeText(this, "同步锚点坐标成功", Toast.LENGTH_SHORT).show();
                syncResult=(List<Anchor>)data.getSerializableExtra("syncResult") ;
                anchors=new Vector<Anchor>();
                for (int i=0;i<syncResult.size();i++){
                    anchors.add(syncResult.get(i));
                }
                saveLocalAnchors(db);
                getLocalAnchors(db);
            }else{
                Toast.makeText(this, "同步锚点失败，使用本地数据", Toast.LENGTH_SHORT).show();
                getLocalAnchors(db);
            }
           beaconPositions= LbsUtils.buildMapping(syncResult);

            initBle();
            startLbs();
        }
    }
    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }
    private void checkPermissionsAndStartScan() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    bletool.setScanRule();
                    bletool.startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        lbsThread.interrupt();
    }
    class LbsThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true&&(!Thread.currentThread().isInterrupted())){
                try {
                    Position p = new Position(BASE_POINT_X + 0.0000001*(1+Math.random()*(6371-1+1)),BASE_POINT_Y + 0.0000001*(1+Math.random()*(3354-1+1)));
                    Message m = Message.obtain();
                    m.obj=p;
                    m.what=REFRESH_POS_SIGNAL;
                    handler.sendMessage(m);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("lbs over");
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    class SyncThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true&&(!Thread.currentThread().isInterrupted())){
                try {
                    //send userpos to server
                    Thread.sleep(10000);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("device_name", userPos.getUserid()));
                    params.add(new BasicNameValuePair("posX", String.valueOf(userPos.getPos().getPosX())));
                    params.add(new BasicNameValuePair("posY", String.valueOf(userPos.getPos().getPosY())));
                    JSONParser.makeHttpRequest(uploadURL, "POST",	params);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("sync over");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    private void getLocalAnchors(DataBase db) {
        Cursor cursor = db.queryAnchors();
        syncResult=new ArrayList<Anchor>();
        if(cursor!=null&&cursor.moveToFirst()){
            do{
                String mac=cursor.getString(cursor.getColumnIndex("mac"));
                double x=cursor.getDouble(cursor.getColumnIndex("posX"));
                double y=cursor.getDouble(cursor.getColumnIndex("posY"));
                syncResult.add(new Anchor(mac,new Position(x,y)));
            }while(cursor.moveToNext());

        }
    }

    private void saveLocalAnchors(DataBase db) {
        for (int i = 0; i < anchors.size(); i++) {
            Anchor anchor;
            anchor = anchors.get(i);
            ContentValues values = new ContentValues();
            values.put("mac", anchor.getMac());
            values.put("posX", anchor.getPos().getPosX());
            values.put("posY", anchor.getPos().getPosY());
            db.updateAnchor(anchor.getMac());
            db.insertAnchors(values);
        }
    }
}


