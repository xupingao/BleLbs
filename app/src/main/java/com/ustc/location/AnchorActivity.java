package com.ustc.location;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ustc.location.bluetooth.BleTool;
import com.ustc.location.bluetooth.DeviceAdapter;
import com.ustc.location.drawtool.DrawEvent;
import com.ustc.location.drawtool.DrawTool;
import com.ustc.location.location.Anchor;
import com.ustc.location.view.MyMapView;

import java.util.List;
import java.util.Vector;

/**
 * 程序主界面
 */
public class AnchorActivity extends AppCompatActivity  {
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private MyMapView mapView;
    private View peopleTag;
    private View areaTag;
    private DeviceAdapter mDeviceAdapter;
    private BleTool bletool;
    private List<Anchor> anchors;
    private SimpleMarkerSymbol markerSymbol;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchor);
        mapView = (MyMapView) findViewById(R.id.myMapView);
        peopleTag = findViewById(R.id.people_tag);
        areaTag = findViewById(R.id.area_tag);
        anchors=(List<Anchor>) getIntent().getSerializableExtra("anchors");
        System.out.println(anchors);
        String path =  "/sdcard/map/shape/room.shp";
        try{
            loadShpFile(path);
            mapView.initPaintTool(this);
            mapView.showNativeInfo();
            markerSymbol = new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
            mapView.drawTool.deactivate();
            mapView.drawTool.point = new Point();
            mapView.drawTool.drawGraphic = new Graphic(mapView.drawTool.point, markerSymbol);
            for(int i=0;i<anchors.size();i++){
                mapView.drawTool.point.setXY(anchors.get(i).getPos().getPosX(), anchors.get(i).getPos().getPosY());
                //mapView.drawTool.sendDrawEndEvent();
                DrawEvent e = new DrawEvent(mapView.drawTool, DrawEvent.DRAW_END,
                        mapView.drawTool.drawGraphic);
                mapView.drawTool.notifyEvent(e);
            }
            mapView.drawTool.activate(DrawTool.POINT);


        }catch (Exception e){
            Toast.makeText(this, "定位系统初始化失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置右上角的菜单选项
     */
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
                //选择图层，调用系统文件夹进行选择
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
            case R.id.menu_native:
                //原始地图
                mapView.showNativeInfo();
                mapView.drawLayer.removeAll();
                mapView.drawTool.deactivate();
                peopleTag.setVisibility(View.GONE);
                areaTag.setVisibility(View.GONE);
                break;
            case R.id.menu_anchor:
                //添加锚点界面
                Intent i = new Intent();
                i.setClass(this, AnchorActivity.class);
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
        mapView.loadLocalMap(path,0);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 ) {
            String result = data.getStringExtra("result");
            System.out.println(result);
            Toast.makeText(this,result ,Toast.LENGTH_SHORT).show();
        }
    }

}
