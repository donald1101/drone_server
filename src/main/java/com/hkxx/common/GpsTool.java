package com.hkxx.drone.common;


public class GpsTool {

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


}
