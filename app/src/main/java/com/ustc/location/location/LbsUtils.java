package com.ustc.location.location;

import android.content.Context;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LbsUtils {
    private static final double TRY_DISTANCE_STEP = 0.01;
    private static final double START_X = 0.0;
    private static final double START_Y = 0.0;
    public static Position calculatePosition(Context ctx, List<BleDevice> bleDevices, Map<String, Position> beaconPositions) {
        Position defPos=new Position(0,0);
        if (bleDevices.isEmpty()) {
            Toast.makeText(ctx, "找不到目标设备", Toast.LENGTH_SHORT).show();
            return defPos;
        }

        if (bleDevices.size() < 3) {
            Toast.makeText(ctx, "目标设备数少于三个", Toast.LENGTH_SHORT).show();
            return defPos;
        }
        // 按信号强度排序
        Collections.sort(bleDevices);

        BleDevice bleDevice1 = bleDevices.get(0);
        BleDevice bleDevice2 = bleDevices.get(1);
        BleDevice bleDevice3 = bleDevices.get(2);

        MathTool.Point device1Point = new MathTool.Point(beaconPositions.get(bleDevice1.getMac()).getPosX(), beaconPositions.get(bleDevice1.getMac()).getPosY());
        MathTool.Point device2Point = new MathTool.Point(beaconPositions.get(bleDevice2.getMac()).getPosX(), beaconPositions.get(bleDevice2.getMac()).getPosY());
        MathTool.Point device3Point = new MathTool.Point(beaconPositions.get(bleDevice3.getMac()).getPosX(), beaconPositions.get(bleDevice3.getMac()).getPosY());

        double [] distances = new double[3];
        distances[0] = MathTool.rssiToDistance(bleDevice1.getRssi()) * Math.cos(Math.toRadians(45));
        distances[1] = MathTool.rssiToDistance(bleDevice2.getRssi()) * Math.cos(Math.toRadians(45));
        distances[2] = MathTool.rssiToDistance(bleDevice3.getRssi()) * Math.cos(Math.toRadians(45)); // 45？

        // 抽象圆
        MathTool.Circle circle1 = new MathTool.Circle(
                new MathTool.Point(device1Point.x, device1Point.y),
                distances[0]);
        MathTool.Circle circle2 = new MathTool.Circle(
                new MathTool.Point(device2Point.x, device2Point.y),
                distances[1]
        );
        MathTool.Circle circle3 = new MathTool.Circle(
                new MathTool.Point(device3Point.x, device3Point.y),
                distances[2]
        );
        while (true) {
            if (!MathTool.isTwoCircleIntersect(circle1, circle2)) {
                if (circle1.r > circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                } else {
                    circle2.r += TRY_DISTANCE_STEP;
                }
                continue;
            }
            if (!MathTool.isTwoCircleIntersect(circle1, circle3)) {
                if (circle3.r < circle1.r && circle3.r < circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                    circle2.r += TRY_DISTANCE_STEP;
                } else {
                    circle3.r += TRY_DISTANCE_STEP;
                }
                continue;
            }
            if (!MathTool.isTwoCircleIntersect(circle2, circle3)) {
                if (circle3.r < circle1.r && circle3.r < circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                    circle2.r += TRY_DISTANCE_STEP;
                } else {
                    circle3.r += TRY_DISTANCE_STEP;
                }
                continue;
            }
            break;
        }

        // 等尝试到三个圆都有交点的时候，求出各自两个圆之间的交点
        MathTool.PointVector2 temp1 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle1, circle2);
        MathTool.PointVector2 temp2 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle2, circle3);
        MathTool.PointVector2 temp3 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle3, circle1);
        // 1、2两圆的交点取y > 0 的那个点
        MathTool.Point resultPoint1 = temp1.p1.y > 0 ?
                new MathTool.Point(temp1.p1.x, temp1.p1.y) :
                new MathTool.Point(temp1.p2.x, temp1.p2.y);
        // 2、3两圆的交点取两者的均值
        MathTool.Point resultPoint2 = new MathTool.Point(
                (temp2.p1.x + temp2.p2.x) / 2,
                (temp2.p1.y + temp2.p2.y) / 2
        );
        // 3、1两圆的交点取x > 0的那个点
        MathTool.Point resultPoint3 = temp3.p1.x > 0 ?
                new MathTool.Point(temp3.p1.x, temp3.p1.y) :
                new MathTool.Point(temp3.p2.x, temp3.p2.y);

        // 求出三个点的中心点
        MathTool.Point resultPoint = MathTool.getCenterOfThreePoint(
                resultPoint1,
                resultPoint2,
                resultPoint3
        );

        return new Position(resultPoint.x, resultPoint.y);
    }
    public static Map<String, Position> buildMapping(List<Anchor> l) {
        Map<String, Position> ret = new HashMap<>();
        for (int i = 0; i < l.size(); i++) {
            ret.put(l.get(i).getMac(), l.get(i).getPos());
        }
        return ret;
    }
}
