package com.hkxx.common;


import java.util.ArrayList;
import java.util.List;

public class GpsUtil {

    public static double pi = 3.1415926535897932384626;
    public static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;
    private static double EARTH_RADIUS = 6378137;// 单位米，地球半径

    public static class LatLng {
        public double lat;
        public double lng;
    }

    public static class Point {
        public double x;
        public double y;
    }

    public static class Size {
        public int w;
        public int h;
    }

    public static class Rect {
        public double x;
        public double y;
        public double w;
        public double h;
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static double[] transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new double[]{mgLat, mgLon};
    }

    /**
     * 判断是否在中国
     *
     * @param lat
     * @param lon
     * @return
     */
    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    /**
     * 84 ==》 高德
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_Gcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new double[]{mgLat, mgLon};
    }

    /**
     * 高德 ==》 84
     *
     * @param lon * @param lat * @return
     */
    public static double[] gcj02_To_Gps84(double lat, double lon) {
        double[] gps = transform(lat, lon);
        double lontitude = lon * 2 - gps[1];
        double latitude = lat * 2 - gps[0];
        return new double[]{latitude, lontitude};
    }

    /**
     * 高德 == 》 百度
     *
     * @param lat
     * @param lon
     */
    public static double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    /**
     * 百度 == 》 高德
     *
     * @param lat
     * @param lon
     */
    public static double[] bd09_To_Gcj02(double lat, double lon) {
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta);
        double tempLat = z * Math.sin(theta);
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    /**
     * 84 == 》 百度
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_bd09(double lat, double lon) {
        double[] gcj02 = gps84_To_Gcj02(lat, lon);
        double[] bd09 = gcj02_To_Bd09(gcj02[0], gcj02[1]);
        return bd09;
    }

    /**
     * 百度 == 》 84
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] bd09_To_gps84(double lat, double lon) {
        double[] gcj02 = bd09_To_Gcj02(lat, lon);
        double[] gps84 = gcj02_To_Gps84(gcj02[0], gcj02[1]);
        //保留小数点后六位
        gps84[0] = retain6(gps84[0]);
        gps84[1] = retain6(gps84[1]);
        return gps84;
    }

    /*
     * 保留小数点后六位
     * @param num
     * @return
     */
    private static double retain6(double num) {
        String result = String.format("%.6f", num);
        return Double.valueOf(result);
    }

    /**
     * 角度弧度计算公式 rad:(). <br/>
     * <p>
     * 360度=2π π=Math.PI
     * <p>
     * x度 = x*π/180 弧度
     *
     * @param degree
     * @return
     * @author chiwei
     * @since JDK 1.6
     */
    public static double getRadian(double degree) {
        return degree * Math.PI / 180.0;
    }

    /**
     * 将弧度换算为角度
     *
     * @param radian
     * @return
     */
    public static double getDegree(double radian) {
        return radian * 180.0 / Math.PI;
    }

    /**
     * 根据经纬度计算两点之间的距离 GetDistance:(). <br/>
     *
     * @param lat1 1点的纬度
     * @param lng1 1点的经度
     * @param lat2 2点的纬度
     * @param lng2 2点的经度
     * @return 距离 单位 米
     * @author chiwei
     * @since JDK 1.6
     */
    public static double getDistanceByGPS(double lat1, double lng1,
                                          double lat2, double lng2) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;// 两点纬度差
        double b = getRadian(lng1) - getRadian(lng2);// 两点的经度差
//        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
//                + Math.cos(radLat1) * Math.cos(radLat2)
//                * Math.pow(Math.sin(b / 2), 2)));
        //三面角公式：cosC=sinA*sinB+cosA*cosB*cosAB；AB为二面角
        double s = Math.acos(Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(b));
        s = s * EARTH_RADIUS;
        return s;
    }

    public static double getDistanceByGPS(double lat1, double lng1,
                                          double lat2, double lng2, double height) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;// 两点纬度差
        double b = getRadian(lng1) - getRadian(lng2);// 两点的经度差
//        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
//                + Math.cos(radLat1) * Math.cos(radLat2)
//                * Math.pow(Math.sin(b / 2), 2)));
        //三面角公式：cosC=sinA*sinB+cosA*cosB*cosAB；AB为二面角
        double s = Math.acos(Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(b));
        s = s * (EARTH_RADIUS + height);
        return s;
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    /**
     * 根据两个点的经纬度，返回航向角，范围为[0,2π)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getAngleByGPS(double lat1, double lng1,
                                       double lat2, double lng2) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = Math.abs(radLat1 - radLat2);// 两点纬度差
        double b = Math.abs(getRadian(lng1) - getRadian(lng2));// 两点的经度差
        double angle = 0;

        if (lng1 < lng2 && lat1 < lat2) {
            angle = Math.atan(a / (Math.cos(radLat1) * b));
        } else if (lng1 < lng2 && lat1 > lat2) {
            angle = 2 * Math.PI - Math.atan(a / (Math.cos(radLat1) * b));
        } else if (lng1 < lng2 && lat1 == lat2) {
            angle = 0;
        } else if (lng1 > lng2 && lat1 < lat2) {
            angle = Math.PI - Math.atan(a / (Math.cos(radLat1) * b));
        } else if (lng1 > lng2 && lat1 > lat2) {
            angle = Math.atan(a / (Math.cos(radLat1) * b)) + Math.PI;
        } else if (lng1 > lng2 && lat1 == lat2) {
            angle = Math.PI;
        } else if (lng1 == lng2 && lat1 < lat2) {
            angle = 0.5 * Math.PI;
        } else if (lng1 == lng2 && lat1 > lat2) {
            angle = 1.5 * Math.PI;
        } else if (lng1 == lng2 && lat1 == lat2) {
            angle = 0;
        }
        return angle;
    }

    /**
     * @param height 无人机高度，单位米(m)
     * @param frame  画幅，单位mm
     * @param focal  焦距，单位mm
     * @param ratio  重叠率，范围10%到90%
     */
    public static double getDistanceByOverlap(double height, double frame, double focal, double ratio) {
        double d = 0;
        try {
            //todo 如果focal焦距为0的话，则使用默认值值24毫米
            focal = focal == 0 ? 24 : focal;
            //单位换成米
            focal = focal / 1000;
            frame = frame / 1000;
            //设呈现的真实距离为x
            double x;
            //拍摄到的距离
            x = frame * height / focal;
            //设重叠距离
            //重叠部分的距离
            d = ratio * x;
            //非重叠部分的距离 （单位米）
            d = x - d;
        } catch (Exception e) {
            e.printStackTrace();
            d = 0;
        }
        return d;
    }

    /**
     * 根据两点和等距的步长生成坐标点列表
     *
     * @param p1   第一个点坐标
     * @param p2   第二个点坐标
     * @param step 步长
     * @return
     */
    public static List<Point> genPointsByStep(Point p1, Point p2, double step) {
        List<Point> rt = new ArrayList<>();
        try {
            rt.add(p1);
            double totalLength = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
            int count = (int) (totalLength / step);
            for (int i = 0; i < count; i++) {
                Point point = new Point();
                point.x = p1.x + (i + 1) * (step / totalLength) * (p2.x - p1.x);    //向量计算
                point.y = p1.y + (i + 1) * (step / totalLength) * (p2.y - p1.y);    //向量计算
                rt.add(point);
            }
            rt.add(p2);
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }

    /**
     * 根据两点和等距的步长生成坐标点列表
     *
     * @param p1      第一个点坐标
     * @param p2      第二个点坐标
     * @param step    步长
     * @param addSelf 是否添加两个端点
     * @return
     */
    public static List<Point> genPointsByStep(Point p1, Point p2, double step, boolean addSelf) {
        List<Point> rt = new ArrayList<>();
        try {
            if (addSelf) {
                rt.add(p1);
            }
            double totalLength = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
            int count = (int) (totalLength / step);
            for (int i = 0; i < count; i++) {
                Point point = new Point();
                point.x = p1.x + (i + 1) * (step / totalLength) * (p2.x - p1.x);    //向量计算
                point.y = p1.y + (i + 1) * (step / totalLength) * (p2.y - p1.y);    //向量计算
                rt.add(point);
            }
            if (addSelf) {
                rt.add(p2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }

    /**
     * 以p为原点，东西方向为水平x轴，南北方向为Y轴建立坐标系，将dest点的经纬度转换为该坐标系的相对坐标
     *
     * @param p      原点的经纬度
     * @param dest   目标点的经纬度
     * @param height 高度
     * @return 返回相对于p点的相对坐标，单位米
     */
    public static Point latLngToPoint(LatLng p, LatLng dest, double height) {
        Point rt = new Point();
        try {
            double distance = getDistanceByGPS(p.lat, p.lng, dest.lat, dest.lng, height);
            double angle = getAngleByGPS(p.lat, p.lng, dest.lat, dest.lng);
            double lng1 = p.lng;
            double lat1 = p.lat;
            double lng2 = dest.lng;
            double lat2 = dest.lat;
            if (lng1 < lng2 && lat1 < lat2) {
                rt.x = distance * Math.cos(angle);
                rt.y = distance * Math.sin(angle);
            } else if (lng1 < lng2 && lat1 > lat2) {
                rt.x = distance * Math.cos(angle);
                rt.y = distance * Math.sin(angle);
            } else if (lng1 < lng2 && lat1 == lat2) {
                rt.x = distance;
                rt.y = 0;
            } else if (lng1 > lng2 && lat1 < lat2) {
                rt.x = distance * Math.cos(angle);
                rt.y = distance * Math.sin(angle);
            } else if (lng1 > lng2 && lat1 > lat2) {
                rt.x = distance * Math.cos(angle);
                rt.y = distance * Math.sin(angle);
            } else if (lng1 > lng2 && lat1 == lat2) {
                rt.x = -distance;
                rt.y = 0;
            } else if (lng1 == lng2 && lat1 < lat2) {
                rt.x = 0;
                rt.y = distance;
            } else if (lng1 == lng2 && lat1 > lat2) {
                rt.x = 0;
                rt.y = -distance;
            } else if (lng1 == lng2 && lat1 == lat2) {
                rt.x = 0;
                rt.y = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 以p为原点，至西向东方向为水平x轴，由南至北为Y轴建立坐标系，将dest点的相对坐标转换为原始的地球经纬度
     *
     * @param p    原点的经纬度
     * @param dest 相对于原点的坐标，单位米
     * @return 返回原始的经纬度
     */
    public static LatLng pointToLatLng(LatLng p, Point dest) {
        LatLng rt = new LatLng();
        try {
            double radLat = getRadian(p.lat);
            double radLng = getRadian(p.lng);
            rt.lat = getDegree(dest.y / EARTH_RADIUS + radLat);
            rt.lng = getDegree(dest.x / (EARTH_RADIUS * Math.cos(radLat)) + radLng);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 根据两条直线方程，求出交点坐标，直线方程为y=kx+b；方程1为y=k1x+b1，方程2为y=k2x+b2
     *
     * @param k1
     * @param b1
     * @param k2
     * @param b2
     * @return
     */
    public static Point getPointByTwoLine(double k1, double b1, double k2, double b2) {
        Point rt = new Point();
        try {
            if (k1 == Double.POSITIVE_INFINITY && k2 != Double.POSITIVE_INFINITY) {
                rt.x = b1;
                rt.y = k2 * rt.x + b2;
            } else if (k1 == Double.POSITIVE_INFINITY && k2 == Double.POSITIVE_INFINITY) {
                //没有交点
                rt = null;
            } else if (k1 != Double.POSITIVE_INFINITY && k2 != Double.POSITIVE_INFINITY) {
                if (k1 == k2) {
                    //没有交点
                    rt = null;
                } else {
                    rt.x = (b2 - b1) / (k1 - k2);
                    rt.y = k1 * rt.x + b1;
                }
            } else if (k1 != Double.POSITIVE_INFINITY && k2 == Double.POSITIVE_INFINITY) {
                rt.x = b2;
                rt.y = k1 * rt.x + b1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = null;
        }
        return rt;
    }

    /**
     * 求一个点到一条直线的距离，直线方程为y=kx+b
     *
     * @param p 该点的坐标
     * @param k 直线的斜率
     * @param b 直线的b值
     * @return
     */
    public static double getDistancePointToLine(Point p, double k, double b) {
        double rt = 0;
        if (k == Double.POSITIVE_INFINITY) {
            //该直线方程为x=b
            rt = Math.abs(p.x - b);
        } else {
            //计算p点平行于Y轴与直线交点的距离
            rt = Math.abs(k * p.x + b - p.y);
            //计算点到直线的距离为D=rt*cosA
            rt = rt * Math.sqrt(1 / (1 + k * k));
        }
        return rt;
    }

    /**
     * 根据一条直线y=kx+b，计算该直线正向平移distance距离后新直线方程y=kx+B，返回B值
     *
     * @param k
     * @param b
     * @param distance 正向平移的间隔
     * @return
     */
    public static double getBWithPositiveDistance(double k, double b, double distance) {
        double rt = 0;
        try {
            if (k == Double.POSITIVE_INFINITY) {
                rt = b + distance;
            } else {
                //计算原直线的斜率角度余弦值
                double cosA = Math.sqrt(1 / (1 + k * k));
                rt = b - distance / cosA;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = 0;
        }
        return rt;
    }

    /**
     * 根据两个点坐标，计算直线方程y=kx+b，返回k和b值，第一个值为k，第二个值为b
     *
     * @param p1 该点的坐标
     * @param p2 该点的坐标
     * @return 返回2个元素的数组，第一个值为k，第二个值为b
     */
    public static double[] getKBFromTwoPoint(Point p1, Point p2) {
        double[] rt = new double[2];
        try {
            double k, b = 0;
            if (p1.x == p2.x) {
                k = Double.POSITIVE_INFINITY;
                b = p1.x; //该直线方程为x=b
            } else {
                k = (p2.y - p1.y) / (p2.x - p1.x);
                b = p1.y - k * p1.x;
            }
            rt[0] = k;
            rt[1] = b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 根据航向重叠率（对应垂直步长）和旁向重叠率（对应水平步长），求一个四边形区域的分割点列表
     *
     * @param point1 四边形顶点1的经纬度
     * @param point2 四边形顶点2的经纬度
     * @param point3 四边形顶点3的经纬度
     * @param point4 四边形顶点4的经纬度
     * @param vStep  垂直间隔，即航向分割的步长，单位米
     * @param hStep  水平间隔，旁向分割的步长，单位米
     * @param height 飞行高度，单位米
     * @return
     */
    public static List<LatLng> getPointListOfArea(LatLng point1, LatLng point2, LatLng point3, LatLng point4, double vStep, double hStep, double height) {
        List<LatLng> list = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        try {
            //以p1为原点，建立坐标系，计算其余点的相对坐标
            Point p1 = latLngToPoint(point1, point1, height);
            Point p2 = latLngToPoint(point1, point2, height);
            Point p3 = latLngToPoint(point1, point3, height);
            Point p4 = latLngToPoint(point1, point4, height);
            //计算四个边的直线方程
            double[] kb = getKBFromTwoPoint(p1, p2);
            double k_p1p2 = kb[0];
            double b_p1p2 = kb[1];
            kb = getKBFromTwoPoint(p2, p3);
            double k_p2p3 = kb[0];
            double b_p2p3 = kb[1];
            kb = getKBFromTwoPoint(p1, p4);
            double k_p1p4 = kb[0];
            double b_p1p4 = kb[1];
            kb = getKBFromTwoPoint(p3, p4);
            double k_p3p4 = kb[0];
            double b_p3p4 = kb[1];
            //计算p3到p1p2的距离
            double d3 = getDistancePointToLine(p3, k_p1p2, b_p1p2);
            double d4 = getDistancePointToLine(p4, k_p1p2, b_p1p2);
            int totalCount = (int) (Math.max(d3, d4) / hStep) + 1;
            int preCount = (int) (Math.min(d3, d4) / hStep) + 1; //扫描的前半段数量
            //不考虑不规则四边形

            double k = k_p1p2;
            double b = b_p1p2;
            Point pointA = p1;
            Point pointB = p2;
            int factor = 1; //直线p1p2平移的方向
            if (k_p1p2 == Double.POSITIVE_INFINITY) {
                if (b_p1p2 <= p3.x) {
                    factor = 1;
                } else {
                    factor = -1;
                }
            } else {
                double temp = k_p1p2 * p3.x + b_p1p2;
                if (temp >= p3.y) {
                    factor = 1;
                } else {
                    factor = -1;
                }
            }
            for (int i = 0; i < totalCount; i++) {
                //计算两个点之间分割后的顶点列表
                if (i % 2 == 0) {
                    //偶数行
                    pointList.addAll(genPointsByStep(pointA, pointB, vStep));
                } else {
                    //奇数行要反过来
                    pointList.addAll(genPointsByStep(pointB, pointA, vStep));
                }
                //获取正向平移后的直线方程
                b = getBWithPositiveDistance(k, b_p1p2, factor * hStep * (i + 1));
                if (i < preCount) {
                    //计算平移后的直线与两个边的交点
                    pointA = getPointByTwoLine(k, b, k_p1p4, b_p1p4);
                    pointB = getPointByTwoLine(k, b, k_p2p3, b_p2p3);
                } else {
                    if (d3 < d4) {
                        //下半段扫描p1p4和p3p4线段
                        pointA = getPointByTwoLine(k, b, k_p1p4, b_p1p4);
                        pointB = getPointByTwoLine(k, b, k_p3p4, b_p3p4);
                    } else {
                        //下半段扫描p2p3和p3p4线段
                        pointA = getPointByTwoLine(k, b, k_p3p4, b_p3p4);
                        pointB = getPointByTwoLine(k, b, k_p2p3, b_p2p3);
                    }
                }
            }
            //将point转换为实际的经纬度坐标
            for (int i = 0; i < pointList.size(); i++) {
                list.add(pointToLatLng(point1, pointList.get(i)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    /**
     * 根据旁向重叠率（对应水平步长），求一个四边形区域的分割点列表，只包含拐点信息
     *
     * @param point1 四边形顶点1的经纬度
     * @param point2 四边形顶点2的经纬度
     * @param point3 四边形顶点3的经纬度
     * @param point4 四边形顶点4的经纬度
     * @param hStep  水平间隔，旁向分割的步长，单位米
     * @param height 飞行高度，单位米
     * @return
     */
    public static List<LatLng> getPointListOfAreaBySidelapRate(LatLng point1, LatLng point2, LatLng point3, LatLng point4, double hStep, double height) {
        List<LatLng> list = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        try {
            //以p1为原点，建立坐标系，计算其余点的相对坐标
            Point p1 = latLngToPoint(point1, point1, height);
            Point p2 = latLngToPoint(point1, point2, height);
            Point p3 = latLngToPoint(point1, point3, height);
            Point p4 = latLngToPoint(point1, point4, height);
            //计算四个边的直线方程
            double[] kb = getKBFromTwoPoint(p1, p2);
            double k_p1p2 = kb[0];
            double b_p1p2 = kb[1];
            kb = getKBFromTwoPoint(p2, p3);
            double k_p2p3 = kb[0];
            double b_p2p3 = kb[1];
            kb = getKBFromTwoPoint(p1, p4);
            double k_p1p4 = kb[0];
            double b_p1p4 = kb[1];
            kb = getKBFromTwoPoint(p3, p4);
            double k_p3p4 = kb[0];
            double b_p3p4 = kb[1];
            //计算p3到p1p2的距离
            double d3 = getDistancePointToLine(p3, k_p1p2, b_p1p2);
            double d4 = getDistancePointToLine(p4, k_p1p2, b_p1p2);
            int totalCount = (int) (Math.max(d3, d4) / hStep) + 1;
            int preCount = (int) (Math.min(d3, d4) / hStep) + 1; //扫描的前半段数量
            //不考虑不规则四边形

            double k = k_p1p2;
            double b = b_p1p2;
            Point pointA = p1;
            Point pointB = p2;
            int factor = 1; //直线p1p2平移的方向
            if (k_p1p2 == Double.POSITIVE_INFINITY) {
                if (b_p1p2 <= p3.x) {
                    factor = 1;
                } else {
                    factor = -1;
                }
            } else {
                double temp = k_p1p2 * p3.x + b_p1p2;
                if (temp >= p3.y) {
                    factor = 1;
                } else {
                    factor = -1;
                }
            }
            for (int i = 0; i < totalCount; i++) {
                //计算两个点之间分割后的顶点列表
                if (i % 2 == 0) {
                    //偶数行
                    //pointList.addAll(genPointsByStep(pointA, pointB, vStep));
                    pointList.add(pointA);
                    pointList.add(pointB);
                } else {
                    //奇数行要反过来
                    //pointList.addAll(genPointsByStep(pointB, pointA, vStep));
                    pointList.add(pointB);
                    pointList.add(pointA);
                }
                //获取正向平移后的直线方程
                b = getBWithPositiveDistance(k, b_p1p2, factor * hStep * (i + 1));
                if (i < preCount) {
                    //计算平移后的直线与两个边的交点
                    pointA = getPointByTwoLine(k, b, k_p1p4, b_p1p4);
                    pointB = getPointByTwoLine(k, b, k_p2p3, b_p2p3);
                } else {
                    if (d3 < d4) {
                        //下半段扫描p1p4和p3p4线段
                        pointA = getPointByTwoLine(k, b, k_p1p4, b_p1p4);
                        pointB = getPointByTwoLine(k, b, k_p3p4, b_p3p4);
                    } else {
                        //下半段扫描p2p3和p3p4线段
                        pointA = getPointByTwoLine(k, b, k_p3p4, b_p3p4);
                        pointB = getPointByTwoLine(k, b, k_p2p3, b_p2p3);
                    }
                }
            }
            //将point转换为实际的经纬度坐标
            for (int i = 0; i < pointList.size(); i++) {
                list.add(pointToLatLng(point1, pointList.get(i)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    /**
     * 检测一个点与直线的方位关系，0表示点在直线上，1表示点在直线大于0的一侧，-1表示点在直线小于0的一侧，-2表示计算错误
     *
     * @param p
     * @param k
     * @param b
     * @return
     */
    public static int checkPointOnLine(Point p, double k, double b) {
        int rt = 0;
        try {
            if (k == Double.POSITIVE_INFINITY) {
                if (p.x == b) {
                    rt = 0;
                } else if (p.x > b) {
                    rt = 1;
                } else {
                    rt = -1;
                }
            } else if (k >= 0) {
                double temp = k * p.x + b - p.y;
                if (temp == 0) {
                    rt = 0;
                } else if (temp > 0) {
                    rt = 1;
                } else {
                    rt = -1;
                }
            } else if (k < 0) {
                double temp = k * p.x + b - p.y;
                if (temp == 0) {
                    rt = 0;
                } else if (temp > 0) {
                    rt = -1;
                } else {
                    rt = 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = -2;
        }
        return rt;
    }

    /**
     * 判断一个四边形是否规则，必须保证两个顶点在对角线两侧才为合法
     *
     * @param pa     其中一个顶点
     * @param pb     对角线的一个顶点，经纬度
     * @param pc     对角线的另一个顶点，经纬度
     * @param pCheck 待测试的顶点，经纬度
     * @return
     */
    public static boolean isNormalPolygon(LatLng pa, LatLng pb, LatLng pc, LatLng pCheck) {
        boolean rt = true;
        try {
            //以pa为原点，建立坐标系，计算其他顶点的相对坐标
            Point p1 = latLngToPoint(pa, pa, 0);
            Point p2 = latLngToPoint(pa, pb, 0);
            Point p3 = latLngToPoint(pa, pc, 0);
            Point p4 = latLngToPoint(pa, pCheck, 0);
            //计算对角线方程
            double[] kb = getKBFromTwoPoint(p2, p3);
            double k = kb[0];
            double b = kb[1];
            //计算顶点pa与对角线的方位关系
            int relativeP1 = checkPointOnLine(p1, k, b);
            int relativeP4 = checkPointOnLine(p4, k, b);
            //不允许在对角线上，且不能与另外一个顶点在对角线同一侧
            if (relativeP4 == 0 || relativeP4 == relativeP1) {
                rt = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    /**
     * 将角度转换到-180到180度之间
     *
     * @param angle
     * @return
     */
    public static float changeAngle(float angle) {
        float rt = angle;
        int temp = 0;
        if (angle > 180) {
            temp = ((int) angle) / 180;
            //angle=a+2k*180 或者angle=a+2(k+1)*180
            if (temp % 2 == 0) {
                temp /= 2;
            } else {
                temp = temp / 2 + 1;
            }
            rt = angle - 360 * temp;
        } else if (angle < -180) {
            temp = ((int) angle) / 180;
            //angle=a+2k*180 或者angle=a+2(k+1)*180
            if (temp % 2 == 0) {
                temp /= 2;
            } else {
                temp = temp / 2 + 1;
            }
            rt = angle + 360 * temp;
        }
        return rt;
    }

    /**
     * 将一个经纬度列表，根据指定步长进行分割，生成分割后的经纬度列表
     *
     * @param srcPointList 源经纬度列表
     * @param step         步长，单位米
     * @param height       高度，单位米
     * @return 分割后的经纬度列表
     */
    public static List<LatLng> getPointListByStep(List<LatLng> srcPointList, double step, double height) {
        List<LatLng> list = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        try {
            if (srcPointList != null && srcPointList.size() > 1) {
                //以第一个点为原点，建立坐标系，计算其余点的相对坐标
                for (int i = 0; i < srcPointList.size() - 1; i++) {
                    Point p1 = latLngToPoint(srcPointList.get(0), srcPointList.get(i), height);
                    Point p2 = latLngToPoint(srcPointList.get(0), srcPointList.get(i + 1), height);
                    pointList.add(p1);
                    pointList.addAll(genPointsByStep(p1, p2, step, false));
                    if (i == srcPointList.size() - 2) {
                        pointList.add(p2);
                    }
                }
                //将point转换为实际的经纬度坐标
                for (int i = 0; i < pointList.size(); i++) {
                    list.add(pointToLatLng(srcPointList.get(0), pointList.get(i)));
                }

            } else {
                list = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }

        return list;
    }

    /**
     * 将图片的像素坐标转换为传感器画幅的坐标，像素坐标原点为图片矩形的左上角，从左到右为x轴，从上到下为y轴，
     * 画幅传感器的坐标系为以中心点为原点，画幅宽度从左至右为x轴，画幅高度从下至上为y轴
     *
     * @param pix          图片像素点坐标值，单位像素
     * @param imageWidth   图片的像素宽度，单位像素
     * @param imageHeight  图片的像素高度，单位像素
     * @param sensorWidth  传感器宽度，单位mm
     * @param sensorHeight 传感器高度，单位mm
     * @return 像素点对应的画幅坐标，单位mm
     */
    public static Point getSensorPointFromPixel(Point pix, int imageWidth, int imageHeight, float sensorWidth, float sensorHeight) {
        Point rt = new Point();
        try {
            rt.x = ((pix.x - imageWidth / 2) / imageWidth) * sensorWidth;
            rt.y = ((imageHeight / 2 - pix.y) / imageHeight) * sensorHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 将图片的像素坐标转换为经纬度坐标。
     * 图片像素坐标系的原点为图片矩形的左上角，从左到右为x轴，从上到下为y轴；
     * 画幅传感器的坐标系为以传感器的中心点为原点，画幅宽度从左至右为x轴，画幅高度从下至上为y轴。
     * 世界坐标系的原点为画幅传感器中心点的正射投影点，至西向东为x轴，由南向北为y轴，由地面至天空为z轴，默认无人机朝向为正北，偏航角为0，云台俯仰角为-90，垂直于地面；
     * 设用户选取的图片目标像素点为P，传感器的焦距视点为F（以传感器中心为原点，做法线垂直于传感器，往上距离为焦距的那个点），
     * 实际问题可转为：在世界坐标系的三维空间中，求解直线FP在传感器坐标系中经过偏航角旋转、云台俯仰角旋转后，与世界坐标系xy平面的交点。
     * 转换方法分为以下几步：
     * 1.将图片像素坐标转换为画幅传感器的坐标，即计算目标点位在传感器坐标系的坐标；
     * 2.将传感器坐标系转换到世界坐标系：对传感器坐标系依次进行云台俯仰角旋转、偏航角旋转，使得传感器坐标系xy轴与世界
     * 坐标系xy轴对齐；计算出2个旋转矩阵，得到变换矩阵R；
     * 3.将传感器坐标系沿z轴平移至世界坐标系，完成传感器坐标系变换至世界坐标系，计算平移矩阵T ；
     * 4.根据前面的变换矩阵R和平移矩阵T，计算目标像素点P，传感器焦距视点F在世界坐标系的坐标P2,F2，即为P2=P*R+T，F2=F*R+T
     * 即目标点位为原坐标经过2次旋转和1次平移得到世界坐标系的坐标；
     * 绕x轴旋转矩阵为：
     * [1 0 0]
     * [0 cosG -sinG]
     * [0 sinG cosG]
     * G为云台俯仰角；
     * 绕z轴旋转矩阵为：
     * [cosY -sinY 0]
     * [sinY cosY 0]
     * [0 0 1]
     * Y为无人机偏航角；
     * 沿着z轴的平移矩阵为=[0 0 S]，S=H-F，H为无人机高度，F为相机焦距；
     * 5.根据PF得到直线PF的方程，求解直线PF与xy平面的交点。
     * P的坐标为(xp,yp,zp)，F的坐标为(xf,yf,zf)；PF直线的方程为 (x-xp)/(xp-xf)=(y-yp)/(yp-yf)=(z-zp)/(zp-zf)；xy平面方程为z=0。
     * 6.世界坐标系原点的经纬度即为无人机的经纬度；根据交点的坐标，可求得该交点的经纬度。
     * 本算法暂时忽略无人机的翻滚角，俯仰角，只考虑无人机的偏航角和云台俯仰角。
     *
     * @param pix          图片像素点坐标值，单位像素
     * @param imageWidth   图片的像素宽度，单位像素
     * @param imageHeight  图片的像素高度，单位像素
     * @param sensorWidth  传感器宽度，单位mm
     * @param sensorHeight 传感器高度，单位mm
     * @param height       无人机高度，单位米
     * @param focal        相机焦距，单位mm
     * @param yaw          无人机偏航角，单位度，范围-180至180
     * @param gimbalPitch  云台俯仰角，单位度，范围-90至0
     * @param srcLatLng    无人机当前经纬度，即传感器中心点正射投影的经纬度
     * @return
     */
    public static LatLng getLatLngFromPixel(Point pix, int imageWidth, int imageHeight, float sensorWidth, float sensorHeight, float height, float focal, float yaw, float gimbalPitch, LatLng srcLatLng) {
        LatLng rt = new LatLng();
        try {
            //将图片像素坐标转换为画幅传感器的坐标
            Point pt = getSensorPointFromPixel(pix, imageWidth, imageHeight, sensorWidth, sensorHeight);
            //将传感器坐标系变换至世界坐标系，计算目标点P的坐标和焦距视点F的坐标
            //矫正偏航角和云台俯仰角，坐标系变换是个相反的过程，所以需要乘以-1
            //yaw = yaw * -1;   //偏航角是需要传感器坐标系逆时针旋转，正好抵消
            gimbalPitch = (gimbalPitch + 90) * -1;  //云台是需要传感器坐标系顺时针旋转，所以需要乘以-1
            //统一单位为米
            pt.x = pt.x / 1000;
            pt.y = pt.y / 1000;
            focal = focal / 1000;
            //转换为弧度值
            double radYaw = getRadian(yaw);
            double radGimbalPitch = getRadian(gimbalPitch);
            //计算P点的坐标，P在传感器坐标系里的坐标为[x y 0]
            double xp = pt.x * Math.cos(radYaw) + pt.y * Math.cos(radGimbalPitch) * Math.sin(radYaw);
            double yp = pt.x * (-1) * Math.sin(radYaw) + pt.y * Math.cos(radGimbalPitch) * Math.cos(radYaw);
            double zp = pt.y * (-1) * Math.sin(radGimbalPitch) + height - focal;
            //计算F点的坐标，F在传感器坐标系里的坐标为[0 0 F]
            double xf = focal * Math.sin(radGimbalPitch) * Math.sin(radYaw);
            double yf = focal * Math.sin(radGimbalPitch) * Math.cos(radYaw);
            double zf = focal * Math.cos(radGimbalPitch) + height - focal;
            //根据P点和F点两点式确定直线PF的方程，求与xy平面的交点G=[gx gy 0]
            double gx = xp - zp * (xp - xf) / (zp - zf);
            double gy = yp - zp * (yp - yf) / (zp - zf);
            //将G点的坐标，根据无人机的经纬度为参考点，计算出相应的经纬度
            Point pointG = new Point();
            pointG.x = gx;
            pointG.y = gy;
            rt = pointToLatLng(srcLatLng, pointG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 根据图片像素上面的AB两点，计算AB两点对应的地面距离，相机角度为正射垂直向下，返回值单位米
     *
     * @param pixA
     * @param pixB
     * @param imageWidth   图片的像素宽度，单位像素
     * @param imageHeight  图片的像素高度，单位像素
     * @param sensorWidth  传感器宽度，单位mm
     * @param sensorHeight 传感器高度，单位mm
     * @param height       无人机高度，单位米
     * @param focal        相机焦距，单位mm
     * @return 返回AB两点的地面距离，单位米
     */
    public static double getDistanceFromPixel(Point pixA, Point pixB, int imageWidth, int imageHeight, float sensorWidth, float sensorHeight, float height, float focal) {
        double rt = 0;
        try {
            //将图片像素坐标转换为画幅传感器的坐标
            Point ptA = getSensorPointFromPixel(pixA, imageWidth, imageHeight, sensorWidth, sensorHeight);
            Point ptB = getSensorPointFromPixel(pixB, imageWidth, imageHeight, sensorWidth, sensorHeight);
            //计算AB两点的画幅距离
            double sensorAB = Math.sqrt(Math.pow(ptA.x - ptB.x, 2) + Math.pow(ptA.y - ptB.y, 2));
            //根据画幅距离和相机焦距，计算地面距离，公式为 sensorAB/groundAB=focal/height
            rt = sensorAB * height / focal;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * 判断一个点是否在多边形中
     *
     * @param vertexList 多边形顶点列表
     * @param pCheck     待测试的点
     * @return true表示在，false表示不在
     */
    public static boolean isInPolygon(List<LatLng> vertexList, LatLng pCheck) {
        boolean rt = false;
        try {
            if (vertexList.size() < 3) {
                rt = false;
            } else {
                List<Point> pointList = new ArrayList<>();
                for (int i = 0; i < vertexList.size(); i++) {
                    //以pa为原点，建立坐标系，计算其他顶点的相对坐标
                    Point pt = latLngToPoint(vertexList.get(0), vertexList.get(i), 0);
                    pointList.add(pt);
                }
                Point pointCheck = latLngToPoint(vertexList.get(0), pCheck, 0);
                rt = isInPolygon(pointList, pointCheck);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }

    /**
     * 判断一个点是否在多边形中
     *
     * @param vertexList 多边形顶点列表
     * @param pCheck     待测试的点
     * @return true表示在，false表示不在
     */
    public static boolean isInPolygon(List<Point> vertexList, Point pCheck) {
        boolean rt = false;
        try {
            if (vertexList.size() < 3) {
                rt = false;
            } else {
                //扫描多边形顶点列表，找到x和y的最大值和最小值，若测试点在包围区域之外，直接返回false
                Point pt = vertexList.get(0);
                double minX = pt.x;
                double maxX = pt.x;
                double minY = pt.y;
                double maxY = pt.y;
                for (int i = 1; i < vertexList.size(); i++) {
                    Point p = vertexList.get(i);
                    if (p.x < minX) {
                        minX = p.x;
                    }
                    if (p.x > maxX) {
                        maxX = p.x;
                    }
                    if (p.y < minY) {
                        minY = p.y;
                    }
                    if (p.y > maxY) {
                        maxY = p.y;
                    }
                }
                if (pCheck.x < minX || pCheck.x > maxX || pCheck.y < minY || pCheck.y > maxY) {
                    //若测试点在包围区域之外，直接返回false
                    rt = false;
                } else {
                    //采用射线法，以测试点为起点，向x正轴方向作一条射线，计算射线与多边形边的交点，若交点数量为奇数
                    //则该点在多边形内部；若交点数量为偶数，则该点在多边形外部
                    int nCross = 0;
                    for (int i = 0; i < vertexList.size(); ++i) {
                        Point p1 = vertexList.get(i);
                        Point p2 = vertexList.get((i + 1) % vertexList.size());
                        // 求解 y=p.y 与 p1 p2 的交点
                        if (p1.y == p2.y) {   // p1p2 与 y=p0.y平行，y=p的斜率为0
                            continue;
                        }
                        if (pCheck.y < Math.min(p1.y, p2.y)) { // 交点在p1p2延长线上
                            continue;
                        }
                        if (pCheck.y >= Math.max(p1.y, p2.y)) { // 交点在p1p2延长线上
                            continue;
                        }
                        // 求交点的 X 坐标
                        double x = (pCheck.y - p1.y) * (p2.x - p1.x)
                                / (p2.y - p1.y) + p1.x;
                        if (x > pCheck.x) { // 只统计单边交点
                            nCross++;
                        }
                    }
                    // 单边交点为偶数，点在多边形之外
                    rt = (nCross % 2 == 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rt = false;
        }
        return rt;
    }


    /**
     * 在一个矩形中，定义坐标系，矩形左上角为原点，从左至右为x轴，从上至下为y轴
     * 目标矩形rect的中心点与点（x，y）构成一条线段，计算该线段中点的坐标
     *
     * @param x    起始点的x坐标
     * @param y    起始点的y坐标
     * @param rect 目标矩形
     * @return 返回目标矩形中心点与点（x，y）构成线段的中心点坐标
     */
    public static Point getHalfPointByRect(double x, double y, Rect rect) {
        Point rt = new Point();
        //计算目标矩形rect中心点的坐标
        Point pRect = new Point();
        pRect.x = rect.x + rect.w / 2;
        pRect.y = rect.y + rect.h / 2;
        //计算起始点（x，y）到rect中心点的向量，除以2之后，得到起始点到中心点的向量
        Point pHalfPoint = new Point();
        pHalfPoint.x = (pRect.x - x) / 2;
        pHalfPoint.y = (pRect.y - y) / 2;
        //原点到起始点的向量加上起始点到中心点的向量，即得到原点到中心点的向量
        rt.x = x + pHalfPoint.x;
        rt.y = y + pHalfPoint.y;

        return rt;
    }


}
