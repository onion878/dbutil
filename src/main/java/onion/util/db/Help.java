package onion.util.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


