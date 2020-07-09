package com.ustc.location.navitool;

import android.util.Pair;

import com.ustc.location.MainActivity;
import com.ustc.location.location.Position;

import java.util.Vector;

public class NaviTool {
    public double [][]lines;
    public Vector<Position> points;
    private Vector<Vector<Integer>>routes;
    private boolean processFlag =false;
    public NaviTool(double[][] lines, Vector<Position> points) {
        this.lines = lines;
        this.points = points;
    }
    public Vector<Integer> getRoute(int begin, int end){
        if (!processFlag){
            processRoute(begin);
            processFlag=true;
        }
        return routes.get(end);
    }
    public int getNearestPoint(Position p){
        int idx=-1;
        double minDis=0;
        for (int i=0;i<points.size();i++){
            double dis=(points.get(i).getPosX()-p.getPosX())*(points.get(i).getPosX()-p.getPosX())+(points.get(i).getPosY()-p.getPosY())*(points.get(i).getPosY()-p.getPosY());
            if (idx==-1){
                idx=i;
                minDis=dis;
            }else{
                if (dis<minDis){
                    minDis=dis;
                    idx=i;
                }
            }
        }
        return idx;
    }
    public static double calDis(Position p1,Position p2){
        double x1=(p1.getPosX()- MainActivity.BASE_POINT_X)*100000;
        double x2=(p2.getPosX()- MainActivity.BASE_POINT_X)*100000;
        double y1=(p1.getPosY()- MainActivity.BASE_POINT_Y)*100000;
        double y2=(p2.getPosY()- MainActivity.BASE_POINT_Y)*100000;
        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    private void processRoute(int begin){
        System.out.println(lines[0][14]);
        routes=new Vector<Vector<Integer>>();
        Vector<Integer> unDecided=new Vector<Integer>();
        for (int i=0;i<20;i++){
            routes.add(new Vector<Integer>());
            if (i!=begin){
                unDecided.add(i);
            }
        }
        while (unDecided.size()!=0){
            double mindis=Double.MAX_VALUE;
            int idx=-1;
            for (int i=0;i<unDecided.size();i++){
                if (lines[begin][unDecided.get(i)]!=0.0&&lines[begin][unDecided.get(i)]<mindis){
                    mindis=lines[begin][unDecided.get(i)];
                    idx=i;
                }
            }
            int selected=unDecided.get(idx);
            System.out.println(selected);
            unDecided.remove(idx);
            for (int i=0;i<unDecided.size();i++){
                if (lines[selected][unDecided.get(i)]!=0.0&&(lines[selected][unDecided.get(i)]+lines[selected][begin]<lines[begin][unDecided.get(i)]||lines[begin][unDecided.get(i)]==0.0)){
                    lines[begin][unDecided.get(i)]=lines[selected][unDecided.get(i)]+lines[selected][begin];
                    lines[unDecided.get(i)][begin]=lines[selected][unDecided.get(i)]+lines[selected][begin];
                    routes.get(unDecided.get(i)).clear();
                    for (int j=0;j<routes.get(selected).size();j++){
                        routes.get(unDecided.get(i)).add(routes.get(selected).get(j));
                    }
                    routes.get(unDecided.get(i)).add(selected);
                }
            }
        }
        System.out.println(routes);
    }
}
