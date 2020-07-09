package com.ustc.location.drawtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.ustc.location.SyncActivity;
import com.ustc.location.location.Anchor;
import com.ustc.location.location.Position;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ustc.location.navitool.NaviTool;

import java.util.Vector;

/**
 *
 *
 *         画图实现类，支持画点、矩形、线、多边形、圆、手画线、手画多边形，可设置各种图形的symbol。
 */
public class DrawTool extends Subject {

	private MapView mapView;
	public GraphicsLayer tempLayer;
	public MarkerSymbol markerSymbol;
	private LineSymbol lineSymbol;
	private FillSymbol fillSymbol;
	private int drawType;
	private boolean active;
	public Point point;
	private Envelope envelope;
	private Polyline polyline;
	private Polygon polygon;
	private DrawTouchListener drawListener;
	private MapOnTouchListener defaultListener;
	public Graphic drawGraphic;
	private Point startPoint;
	private int graphicID;
	private AppCompatActivity ctx;

	public static final int POINT = 1;
	public static final int ENVELOPE = 2;
	public static final int POLYLINE = 3;
	public static final int POLYGON = 4;
	public static final int CIRCLE = 5;
	public static final int ELLIPSE = 6;
	public static final int FREEHAND_POLYGON = 7;
	public static final int FREEHAND_POLYLINE = 8;
    private	GraphicsLayer drawLayer;
    private NaviTool naviTool;
    private int beginIdx;
	private int endIdx;
	private Vector<Integer> route;
	public DrawTool(MapView mapView, AppCompatActivity ctx, GraphicsLayer layer) {
	    this.ctx=ctx;
		this.mapView = mapView;
		this.tempLayer = new GraphicsLayer();
		this.drawLayer=layer;
		this.mapView.addLayer(this.tempLayer);
		drawListener = new DrawTouchListener(this.mapView.getContext(),
				this.mapView);
		defaultListener = new MapOnTouchListener(this.mapView.getContext(),
				this.mapView);
		this.markerSymbol = new SimpleMarkerSymbol(Color.GRAY, 8,
				SimpleMarkerSymbol.STYLE.CIRCLE);
		this.lineSymbol = new SimpleLineSymbol(Color.BLUE, 2);
		this.fillSymbol = new SimpleFillSymbol(Color.BLUE);
		this.fillSymbol.setAlpha(90);
	}

	public void setNaviTool(NaviTool naviTool) {
		this.naviTool = naviTool;
	}

	public void activate(int drawType) {
		if (this.mapView == null)
			return;

		this.deactivate();

		this.mapView.setOnTouchListener(drawListener);
		this.drawType = drawType;
		this.active = true;
		switch (this.drawType) {
			case DrawTool.POINT:
				this.point = new Point();
				drawGraphic = new Graphic(this.point, this.markerSymbol);
				break;
			case DrawTool.ENVELOPE:
				this.envelope = new Envelope();
				drawGraphic = new Graphic(this.envelope, this.fillSymbol);
				break;
			case DrawTool.POLYGON:
			case DrawTool.CIRCLE:
			case DrawTool.FREEHAND_POLYGON:
				this.polygon = new Polygon();
				drawGraphic = new Graphic(this.polygon, this.fillSymbol);
				break;
			case DrawTool.POLYLINE:
			case DrawTool.FREEHAND_POLYLINE:
				this.polyline = new Polyline();
				drawGraphic = new Graphic(this.polyline, this.lineSymbol);
				break;
		}
		graphicID = this.tempLayer.addGraphic(drawGraphic);
	}

	public void deactivate() {
		this.mapView.setOnTouchListener(defaultListener);
		this.tempLayer.removeAll();
		this.active = false;
		this.drawType = -1;
		this.point = null;
		this.envelope = null;
		this.polygon = null;
		this.polyline = null;
		this.drawGraphic = null;
		this.startPoint=null;
	}

	public MarkerSymbol getMarkerSymbol() {
		return markerSymbol;
	}

	public void setMarkerSymbol(MarkerSymbol markerSymbol) {
		this.markerSymbol = markerSymbol;
	}

	public LineSymbol getLineSymbol() {
		return lineSymbol;
	}

	public void setLineSymbol(LineSymbol lineSymbol) {
		this.lineSymbol = lineSymbol;
	}

	public FillSymbol getFillSymbol() {
		return fillSymbol;
	}

	public void setFillSymbol(FillSymbol fillSymbol) {
		this.fillSymbol = fillSymbol;
	}

	public void setBeginIdx(int beginIdx) {
		this.beginIdx = beginIdx;
	}

	public void sendDrawEndEvent() {
		DrawEvent e = new DrawEvent(this, DrawEvent.DRAW_END,
				DrawTool.this.drawGraphic);
		DrawTool.this.notifyEvent(e);
		int type = this.drawType;
		this.deactivate();
		this.activate(type);
	}

	class DrawTouchListener extends MapOnTouchListener {

		public DrawTouchListener(Context context, MapView view) {
			super(context, view);
		}

//		@Override
//		public boolean onTouch(View view, MotionEvent event) {
//			if (active
//					&& (drawType == POINT || drawType == ENVELOPE
//					|| drawType == CIRCLE
//					|| drawType == FREEHAND_POLYLINE || drawType == POLYLINE)
//					&& event.getAction() == MotionEvent.ACTION_DOWN) {
//				final Point point = mapView.toMapPoint(event.getX(), event.getY());
//				System.out.println(point.getX()+","+point.getY());
//				switch (drawType) {
//					case DrawTool.POINT:{
//							DrawTool.this.point.setXY(point.getX(), point.getY());
//							sendDrawEndEvent();
//							try {
//								Thread.sleep(500);
//							} catch (InterruptedException ee) {
//								ee.printStackTrace();
//							}
//							final EditText inputServer = new EditText(ctx);
//							AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
//							builder.setTitle("输入mac地址：").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
//									.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog, int which) {
//											dialog.dismiss();
//										}
//									});
//							builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog, int which) {
//									String mac = inputServer.getText().toString();
//									//send [mac,(point.x,point.y)] to server
//									Intent intent = new Intent();
//									intent.setClass(ctx, SyncActivity.class);
//									Anchor anchor = new Anchor(mac, new Position(point.getX(), point.getY()));
//									intent.putExtra("anchor", anchor);
//									intent.putExtra("sync_kind", 1);
//									ctx.startActivityForResult(intent, 1);
//								}
//							});
//							builder.show();
//							break;
//					}
////					case DrawTool.ENVELOPE:
////						startPoint = point;
////						envelope.setCoords(point.getX(), point.getY(),
////								point.getX(), point.getY());
////						break;
////					case DrawTool.CIRCLE:
////						startPoint = point;
////						break;
////					case DrawTool.FREEHAND_POLYGON:
////						polygon.startPath(point);
////						break;
//					case DrawTool.POLYLINE: {
////						for (int i=0;i<20;i++){
////							for (int j=0;j<20;j++){
////								if (naviTool.lines[i][j]!=0.0){
////									polyline.startPath(new Point(naviTool.points.get(i).getPosX(),naviTool.points.get(i).getPosY()));
////									polyline.lineTo(new Point(naviTool.points.get(j).getPosX(),naviTool.points.get(j).getPosY()));
////									DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
////									sendDrawEndEvent();
////								}
////							}
////						}
//						drawLayer.removeAll();
//						endIdx = naviTool.getNearestPoint(new Position(point.getX(), point.getY()));
//						System.out.println(endIdx);
//						MarkerSymbol beginMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
//						MarkerSymbol endMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
//						Point beginPoint = new Point(naviTool.points.get(beginIdx).getPosX(), naviTool.points.get(beginIdx).getPosY());
//						Point endPoint=new Point(naviTool.points.get(endIdx).getPosX(), naviTool.points.get(endIdx).getPosY());
//						DrawTool.this.drawGraphic = new Graphic(beginPoint, beginMarkerSymbol);
//						DrawEvent e = new DrawEvent(DrawTool.this, DrawEvent.DRAW_END, DrawTool.this.drawGraphic);
//						DrawTool.this.notifyEvent(e);
//						DrawTool.this.drawGraphic = new Graphic(endPoint, endMarkerSymbol);
//						e = new DrawEvent(DrawTool.this, DrawEvent.DRAW_END, DrawTool.this.drawGraphic);
//						DrawTool.this.notifyEvent(e);
//
//						polyline = new Polyline();
//						drawGraphic = new Graphic(polyline, DrawTool.this.lineSymbol);
//						polyline.startPath(beginPoint);
//
//						route = naviTool.getRoute(beginIdx, endIdx);
//						for (int i = 0; i < route.size(); i++) {
//							Point ep = new Point(naviTool.points.get(route.get(i)).getPosX(), naviTool.points.get(route.get(i)).getPosY());
//							polyline.lineTo(ep);
//							DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
//							sendDrawEndEvent();
//							polyline.startPath(ep);
//						}
//						polyline.lineTo(endPoint);
//						DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
//						sendDrawEndEvent();
//						break;
//					}
//				}
//			}
//			return super.onTouch(view, event);
//		}

		@Override
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
//			if (active
//					&& (drawType == ENVELOPE || drawType == FREEHAND_POLYGON
//					|| drawType == FREEHAND_POLYLINE || drawType == CIRCLE)) {
//				Point point = mapView.toMapPoint(to.getX(), to.getY());
//				switch (drawType) {
//					case DrawTool.ENVELOPE:
//						envelope.setXMin(startPoint.getX() > point.getX() ? point
//								.getX() : startPoint.getX());
//						envelope.setYMin(startPoint.getY() > point.getY() ? point
//								.getY() : startPoint.getY());
//						envelope.setXMax(startPoint.getX() < point.getX() ? point
//								.getX() : startPoint.getX());
//						envelope.setYMax(startPoint.getY() < point.getY() ? point
//								.getY() : startPoint.getY());
//						DrawTool.this.tempLayer.updateGraphic(graphicID, envelope.copy());
//						break;
//					case DrawTool.FREEHAND_POLYGON:
//						polygon.lineTo(point);
//						DrawTool.this.tempLayer.updateGraphic(graphicID, polygon);
//						break;
//					case DrawTool.FREEHAND_POLYLINE:
//						polyline.lineTo(point);
//						DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
//						break;
//					case DrawTool.CIRCLE:
//						double radius = Math.sqrt(Math.pow(startPoint.getX()
//								- point.getX(), 2)
//								+ Math.pow(startPoint.getY() - point.getY(), 2));
//						getCircle(startPoint, radius, polygon);
//						DrawTool.this.tempLayer.updateGraphic(graphicID, polygon);
//						break;
//				}
//				return true;
//			}
			return super.onDragPointerMove(from, to);
		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
//			if (active && (drawType == ENVELOPE || drawType == FREEHAND_POLYGON
//					|| drawType == FREEHAND_POLYLINE || drawType == CIRCLE)) {
//				Point point = mapView.toMapPoint(to.getX(), to.getY());
//				switch (drawType) {
//					case DrawTool.ENVELOPE:
//						envelope.setXMin(startPoint.getX() > point.getX() ? point
//								.getX() : startPoint.getX());
//						envelope.setYMin(startPoint.getY() > point.getY() ? point
//								.getY() : startPoint.getY());
//						envelope.setXMax(startPoint.getX() < point.getX() ? point
//								.getX() : startPoint.getX());
//						envelope.setYMax(startPoint.getY() < point.getY() ? point
//								.getY() : startPoint.getY());
//						break;
//					case DrawTool.FREEHAND_POLYGON:
//						polygon.lineTo(point);
//						break;
//					case DrawTool.FREEHAND_POLYLINE:
//						polyline.lineTo(point);
//						break;
//					case DrawTool.CIRCLE:
//						double radius = Math.sqrt(Math.pow(startPoint.getX()
//								- point.getX(), 2)
//								+ Math.pow(startPoint.getY() - point.getY(), 2));
//						getCircle(startPoint, radius, polygon);
//						break;
//				}
//				sendDrawEndEvent();
//				startPoint = null;
//				return true;
//			}
			return super.onDragPointerUp(from, to);
		}

		public boolean onSingleTap(MotionEvent event) {
			if (active
					&& (drawType == POINT || drawType == ENVELOPE
					|| drawType == CIRCLE
					|| drawType == FREEHAND_POLYLINE || drawType == POLYLINE)
					&& event.getAction() == MotionEvent.ACTION_DOWN) {
				final Point point = mapView.toMapPoint(event.getX(), event.getY());
				System.out.println(point.getX()+","+point.getY());
				switch (drawType) {
					case DrawTool.POINT:{
						DrawTool.this.point.setXY(point.getX(), point.getY());
						sendDrawEndEvent();
						try {
							Thread.sleep(500);
						} catch (InterruptedException ee) {
							ee.printStackTrace();
						}
						final EditText inputServer = new EditText(ctx);
						AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
						builder.setTitle("输入mac地址：").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
								.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String mac = inputServer.getText().toString();
								//send [mac,(point.x,point.y)] to server
								Intent intent = new Intent();
								intent.setClass(ctx, SyncActivity.class);
								Anchor anchor = new Anchor(mac, new Position(point.getX(), point.getY()));
								intent.putExtra("anchor", anchor);
								intent.putExtra("sync_kind", 1);
								ctx.startActivityForResult(intent, 1);
							}
						});
						builder.show();
						break;
					}
//					case DrawTool.ENVELOPE:
//						startPoint = point;
//						envelope.setCoords(point.getX(), point.getY(),
//								point.getX(), point.getY());
//						break;
//					case DrawTool.CIRCLE:
//						startPoint = point;
//						break;
//					case DrawTool.FREEHAND_POLYGON:
//						polygon.startPath(point);
//						break;
					case DrawTool.POLYLINE: {
//						for (int i=0;i<20;i++){
//							for (int j=0;j<20;j++){
//								if (naviTool.lines[i][j]!=0.0){
//									polyline.startPath(new Point(naviTool.points.get(i).getPosX(),naviTool.points.get(i).getPosY()));
//									polyline.lineTo(new Point(naviTool.points.get(j).getPosX(),naviTool.points.get(j).getPosY()));
//									DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
//									sendDrawEndEvent();
//								}
//							}
//						}
						drawLayer.removeAll();
						endIdx = naviTool.getNearestPoint(new Position(point.getX(), point.getY()));
						System.out.println(endIdx);
						MarkerSymbol beginMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
						MarkerSymbol endMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 8, SimpleMarkerSymbol.STYLE.CIRCLE);
						Point beginPoint = new Point(naviTool.points.get(beginIdx).getPosX(), naviTool.points.get(beginIdx).getPosY());
						Point endPoint=new Point(naviTool.points.get(endIdx).getPosX(), naviTool.points.get(endIdx).getPosY());
						DrawTool.this.drawGraphic = new Graphic(beginPoint, beginMarkerSymbol);
						DrawEvent e = new DrawEvent(DrawTool.this, DrawEvent.DRAW_END, DrawTool.this.drawGraphic);
						DrawTool.this.notifyEvent(e);
						DrawTool.this.drawGraphic = new Graphic(endPoint, endMarkerSymbol);
						e = new DrawEvent(DrawTool.this, DrawEvent.DRAW_END, DrawTool.this.drawGraphic);
						DrawTool.this.notifyEvent(e);

						polyline = new Polyline();
						drawGraphic = new Graphic(polyline, DrawTool.this.lineSymbol);
						polyline.startPath(beginPoint);

						route = naviTool.getRoute(beginIdx, endIdx);
						for (int i = 0; i < route.size(); i++) {
							Point ep = new Point(naviTool.points.get(route.get(i)).getPosX(), naviTool.points.get(route.get(i)).getPosY());
							polyline.lineTo(ep);
							DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
							sendDrawEndEvent();
							polyline.startPath(ep);
						}
						polyline.lineTo(endPoint);
						DrawTool.this.tempLayer.updateGraphic(graphicID, polyline);
						sendDrawEndEvent();
						break;
					}
				}
			}
			return super.onSingleTap(event);
		}

		public boolean onDoubleTap(MotionEvent event) {
			if (active &&(drawType==POLYGON || drawType==POLYLINE)) {
				Point point = mapView.toMapPoint(event.getX(), event.getY());
				switch (drawType) {
					case DrawTool.POLYGON:
						polygon.lineTo(point);
						break;
					case DrawTool.POLYLINE:
						polyline.lineTo(point);
						break;
				}
				sendDrawEndEvent();
				startPoint = null;
				return true;
			}
			return super.onDoubleTap(event);
		}

		private void getCircle(Point center, double radius, Polygon circle) {
			circle.setEmpty();
			Point[] points = getPoints(center, radius);
			circle.startPath(points[0]);
			for (int i = 1; i < points.length; i++)
				circle.lineTo(points[i]);
		}

		private Point[] getPoints(Point center, double radius) {
			Point[] points = new Point[50];
			double sin;
			double cos;
			double x;
			double y;
			for (double i = 0; i < 50; i++) {
				sin = Math.sin(Math.PI * 2 * i / 50);
				cos = Math.cos(Math.PI * 2 * i / 50);
				x = center.getX() + radius * sin;
				y = center.getY() + radius * cos;
				points[(int) i] = new Point(x, y);
			}
			return points;
		}
	}
}
