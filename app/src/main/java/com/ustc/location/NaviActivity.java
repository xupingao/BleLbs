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
import com.ustc.location.location.Position;
import com.ustc.location.location.UserPos;
import com.ustc.location.navitool.NaviTool;
import com.ustc.location.view.MyMapView;

import java.util.List;
import java.util.Vector;

/**
 * 程序主界面
 */
public class NaviActivity extends AppCompatActivity  {
    private MyMapView mapView;
    private List<Anchor> anchors;
    private SimpleMarkerSymbol markerSymbol;
    private UserPos userPos;
    private int beginPoint;
    private NaviTool naviTool;
    private double[][] lines;
    private Vector<Position> points;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        mapView = (MyMapView) findViewById(R.id.myMapView);
        anchors=(List<Anchor>) getIntent().getSerializableExtra("anchors");
        userPos=(UserPos)getIntent().getSerializableExtra("userPos") ;
        System.out.println(anchors);
        String path =  "/sdcard/map/shape/room.shp";
        lines=new double[20][20];
        points=new  Vector<Position> ();
        naviTool=new NaviTool(lines,points);
        points.add(new Position(120.72371856577584,31.27808830346094));
        points.add(new Position(120.72379044929943,31.278086405882746));
        points.add(new Position(120.72385960837804,31.278063760206688));
        points.add(new Position(120.72379003015529,31.27803104981986));
        points.add(new Position(120.72378751527759,31.277974855039613));
        points.add(new Position( 120.72379044929943,31.277926628176733));
        points.add(new Position(120.72386044666952,31.277925789433755));
        points.add(new Position(120.72386086581047,31.277979468036314));
        points.add(new Position(120.72399038189897,31.27792704753541));
        points.add(new Position(120.72399247764211,31.27797820993466));
        points.add(new Position(120.72399163933461,31.278061244028994));
        points.add(new Position(120.72414295103395,31.27805663100667));
        points.add(new Position(120.72411528739867,31.27797485501399));
        points.add(new Position( 120.72411151509176,31.27792956368748));
        points.add(new Position( 120.72417019551548,31.277992468309055));
        points.add(new Position( 120.7242431268978,31.27785659435514));
        points.add(new Position( 120.72416055515852,31.278119116243953));
        points.add(new Position( 120.72423474400209,31.27814302002167));
        points.add(new Position( 120.72420791864869,31.278000436226424));
        points.add(new Position( 120.72419576342044,31.278061663362053));
        addLine(0,1);
        addLine(1,2);
        addLine(1,3);
        addLine(2,3);
        addLine(3,4);
        addLine(4,5);
        addLine(5,6);
        addLine(6,7);
        addLine(7,2);
        addLine(7,9);
        addLine(6,8);
        addLine(2,10);
        addLine(10,11);
        addLine(9,12);
        addLine(8,13);
        addLine(8,9);
        addLine(9,10);
        addLine(12,13);
        addLine(12,14);
        addLine(11,14);
        addLine(14,16);
        addLine(11,16);
        addLine(14,18);
        addLine(16,17);
        addLine(18,15);
        addLine(18,19);

        beginPoint=naviTool.getNearestPoint(userPos.getPos());
        try{
            loadShpFile(path);
            mapView.initPaintTool(this);
            mapView.drawTool.setNaviTool(naviTool);
            mapView.drawTool.setBeginIdx(beginPoint);
            mapView.showNativeInfo();
            markerSymbol = new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
            mapView.drawTool.deactivate();
            mapView.drawTool.point = new Point();
            mapView.drawTool.drawGraphic = new Graphic(mapView.drawTool.point, markerSymbol);
            mapView.drawTool.point.setXY(points.get(beginPoint).getPosX(),points.get(beginPoint).getPosY());
            DrawEvent e = new DrawEvent(mapView.drawTool, DrawEvent.DRAW_END,
                    mapView.drawTool.drawGraphic);
//            for(int i=0;i<points.size();i++){
//                mapView.drawTool.point.setXY(points.get(i).getPosX(), points.get(i).getPosY());
//                //mapView.drawTool.sendDrawEndEvent();
//                DrawEvent e = new DrawEvent(mapView.drawTool, DrawEvent.DRAW_END,
//                        mapView.drawTool.drawGraphic);
//                mapView.drawTool.notifyEvent(e);
//            }
            mapView.drawTool.notifyEvent(e);
            mapView.drawTool.activate(DrawTool.POLYLINE);
        }catch (Exception e){
            Toast.makeText(this, "定位系统初始化失败！", Toast.LENGTH_SHORT).show();
        }
    }
    private void addLine(int a,int b){
        lines[a][b]=NaviTool.calDis(points.get(a),points.get(b));
        lines[b][a]=lines[a][b];
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

            case R.id.menu_navi: {
                Intent i = new Intent();
                i.setClass(this, NaviActivity.class);
                i.putExtra("userPos",userPos);
                startActivity(i);
                finish();
                break;
            }
        }
        return true;
    }
    /**
     * 加载本地shp文件
     */
    private void loadShpFile(String path){
        mapView.loadLocalMap(path,2);
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
