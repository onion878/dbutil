package onion.util.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 工具类
 *
 * @author
 */
public class Help {

    /**
     * 得到一个月的天数
     */
    public static int Getdayofmanth() {
        Calendar a = Calendar.getInstance(Locale.CHINA);
        int day = a.getActualMaximum(Calendar.DATE);
        return day;
    }

    /**
     * 得到当前年月份
     */
    public static String getmonth() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String date = formatter.format(currentTime);
        return date;
    }

    /**
     * String转Date
     *
     * @param date
     * @return Date
     */
    public static Date tostandarDate(String date) {
        try {
            SimpleDateFormat standarDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd");
            return standarDateFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(3, "日期转换出错,信息:" + e.getMessage());
        }
        return null;
    }

    public static Date tostandarDatehms(String date) {
        try {
            SimpleDateFormat standarDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return standarDateFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static class StandardDateformatter {
        private static SimpleDateFormat INSTANCE = null;

        public static SimpleDateFormat getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new SimpleDateFormat("yyyy-MM-dd");
            }
            return INSTANCE;
        }
    }

    /**
     * 得到当前年月日时分秒（String）
     *
     * @return
     */
    public static String getnow() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(currentTime);
        return date;
    }

    /**
     * 得到当前日期（Date）
     *
     * @return
     */
    public static Date getnowdate() {
        Date currentTime = new Date();
        return currentTime;
    }

    /**
     * 转换年月日（Date=》String）
     *
     * @param date
     * @return
     */
    public static String stringtodate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date1 = formatter.format(date);
        return date1;
    }

    /**
     * 获取两个日期中的所有日期
     *
     * @param startdate
     * @param enddate
     * @return String
     */
    public static List<String> findalldates(String startdate, String enddate) {
        List<String> list = new ArrayList<String>();
        list.add(startdate);
        Calendar calstart = Calendar.getInstance();
        calstart.setTime(tostandarDate(startdate));
        Calendar calend = Calendar.getInstance();
        calend.setTime(tostandarDate(enddate));
        while (tostandarDate(enddate).after(calstart.getTime())) {
            calstart.add(Calendar.DAY_OF_MONTH, 1);
            list.add(stringtodate(calstart.getTime()));

        }
        return list;
    }

    /**
     * 获得当前周的每一天
     *
     * @param mString
     * @return
     */
    @SuppressWarnings("deprecation")
    public static List<String> dateeveyweek(String mString) {
        Date mdate = tostandarDate(mString);
        int b = mdate.getDay();
        Date fDate;
        List<String> list = new ArrayList<String>();
        Long fTime = mdate.getTime() - b * 24 * 3600000;
        for (int a = 1; a < 8; a++) {
            fDate = new Date();
            fDate.setTime(fTime + (a * 24 * 3600000));
            list.add(stringtodate(fDate));
        }
        return list;
    }

    /**
     * 获取一周的开头和结尾
     *
     * @param mString
     * @return List<String>
     */
    @SuppressWarnings("deprecation")
    public static List<String> dateToweek(String mString) {
        Date mdate = tostandarDate(mString);
        int b = mdate.getDay();
        Date fDate;
        List<String> list = new ArrayList<String>();
        Long fTime = mdate.getTime() - b * 24 * 3600000;
        for (int a = 1; a < 8; a++) {
            fDate = new Date();
            fDate.setTime(fTime + (a * 24 * 3600000));
            if (a == 1) {
                list.add(stringtodate(fDate));
            }
            if (a == 7) {
                list.add(stringtodate(fDate));
            }

        }
        return list;
    }

    /**
     * 生成分页的sql语句，需配置db数据库类型
     *
     * @param key       数据库字段
     * @param value     对应的值
     * @param tableName 表名
     * @param order     排序（sqlserver必须）
     * @param page      第几页
     * @param size      显示多少条
     * @return sql分页的sql语句，size当前条件下的总记录数
     * @throws Exception
     */
    public static Map<String, Object> SqlSting(String[] key, String[] value,
                                               String tableName, String order, int page, int size)
            throws Exception {
        if (key != null && key.length != value.length)
            throw new Exception("参数值不对应");
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        String db = PropertyReader.get("db");
        String sqlString = "", sizeString = "";
        List<Object> obj = new ArrayList<Object>();
        if (db.equalsIgnoreCase("oracle")) {
            sqlString = "select*from ( select t.*,size_rownum from "
                    + tableName + " t ";
            sizeString = "select COUNT(*) as size from " + tableName + " ";
            if (key != null && key.length > 0 && value.equals(null)) {

                for (int i = 0; i < value.length; i++) {
                    try {
                        if (value[i].trim().equals("")
                                || value[i].trim().equals(null))
                            continue;
                    } catch (Exception e) {
                        continue;
                    }
                    if (i == 0) {
                        sqlString = sqlString + " where ";
                        sizeString = sizeString + " where ";
                    }
                    String val = value[i];
                    String keyString = key[i];
                    if (i < value.length - 1) {
                        sqlString = sqlString + keyString + " like '%" + val
                                + "%' and ";
                        sizeString = sizeString + keyString + " like '%" + val
                                + "%' and ";

                    } else {
                        sqlString = sqlString + keyString + " like '%" + val
                                + "%' ";
                        sizeString = sizeString + keyString + " like '%" + val
                                + "%' ";
                    }
                }
            }
            if (!order.trim().equals("") && !order.equals(null)) {
                sqlString = sqlString + " order by " + order;
            }
            sqlString = sqlString + " ) where size_rownum>(" + (page - 1)
                    * size + " and size_rownum<=(" + page + ")*" + size;
        } else if (db.equalsIgnoreCase("mysql")) {
            sqlString = "select*from " + tableName + "  ";
            sizeString = "select COUNT(*) as size from " + tableName + " ";
            if (key != null && key.length > 0) {
                for (int i = 0; i < value.length; i++) {
                    try {
                        if (value[i] == null || value[i].trim().equals(""))
                            continue;
                    } catch (Exception e) {
                        continue;
                    }
                    if (i == 0) {
                        sqlString = sqlString + " where ";
                        sizeString = sizeString + " where ";
                    }
                    String val = value[i];
                    String keyString = key[i];
                    if (i < value.length - 1) {
                        sqlString = sqlString + keyString + " like ? and ";
                        sizeString = sizeString + keyString + " like ? and ";
                        obj.add("%" + val + "%");
                    } else {
                        sqlString = sqlString + keyString + " like ? ";
                        sizeString = sizeString + keyString + " like ? ";
                        obj.add("%" + val + "%");
                    }
                }
            }
            if (!order.trim().equals("") && !order.equals(null)) {
                sqlString = sqlString + " order by " + order;
            }
            sqlString = sqlString + " limit " + (page - 1) * size + "," + size;
        } else if (db.equalsIgnoreCase("sqlserver")) {
            sqlString = "SELECT TOP " + size
                    + "*FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY " + order
                    + " ) COLINDEX ,* FROM " + tableName + " ";
            if (order.trim().equals("") && order.equals(null))
                throw new Exception("sqlserver分页语句必须设置排序");
            sqlString = sqlString + " ) v WHERE   COLINDEX >"
                    + (page - 1) * size + " ";
            sizeString = "select COUNT(*) as size from " + tableName + " ";
            int j = 0;
            if (key != null && key.length > 0) {
                for (int i = 0; i < value.length; i++) {
                    try {
                        if (value[i] == null || value[i].trim().equals(""))
                            continue;
                    } catch (Exception e) {
                        continue;
                    }
                    if (j == 0) {
                        sqlString = sqlString + " and ";
                        sizeString = sizeString + " where ";
                    }
                    String val = value[i];
                    String keyString = key[i];
                    obj.add(val);
                    sqlString = sqlString + keyString + " like '%'+?+'%' and ";
                    sizeString = sizeString + keyString + " like '%'+?+'%' and ";
                    j++;
                }
                if (sqlString.contains("and")) {
                    sqlString = sqlString.substring(0, sqlString.lastIndexOf("and"));
                    sizeString = sizeString.substring(0, sizeString.lastIndexOf("and"));
                }
            }
            sqlString = sqlString + " order by " + order;
        }
        sqlMap.put("sql", sqlString);
        sqlMap.put("size", sizeString);
        sqlMap.put("value", obj);
        return sqlMap;
    }

    public static String toEnd(String msg) {
        msg = msg.trim();
        return msg.substring(0, msg.length() - 1);
    }

    public static String toField(String field) {
        String[] s = field.split("_");
        String fielda = "";
        for (int i = 0; i < s.length; i++) {
            if (i == 0)
                fielda = fielda + s[i];
            else
                fielda = fielda + captureName(s[i]);
        }
        return fielda.trim();
    }

    public static String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }
}


