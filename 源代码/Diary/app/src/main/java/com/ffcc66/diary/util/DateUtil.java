package com.ffcc66.diary.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    Calendar calendar = Calendar.getInstance();
    String yearStr,monthStr,dayStr,hourStr,minuteStr,secondStr,mescStr;
    int year,month,day,hour,minute,second,mesc;

    public DateUtil(Long date) {
        calendar.setTime(new Date(date));
        year = calendar.get(Calendar.YEAR);
        yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
        month = calendar.get(Calendar.MONTH) + 1;//获取月份
        monthStr = month < 10 ? "0" + month : month + "";
        day = calendar.get(Calendar.DATE);//获取日
        dayStr = day < 10 ? "0" + day : day + "";
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        hourStr = hour < 10 ? "0" + hour : hour + "";
        minute = calendar.get(Calendar.MINUTE);
        minuteStr = minute < 10 ? "0" + minute : minute + "";
        second = calendar.get(Calendar.SECOND);
        secondStr = calendar.get(Calendar.SECOND) + "";
        mesc = calendar.get(Calendar.MILLISECOND);
        mescStr = calendar.get(Calendar.MILLISECOND) + "";
    }

    /**
     * 获取当前时间
     * @return
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 获取时间字符串
     * 格式：2019/02/03
     * @return
     */
    public String getDateString() {
        return yearStr+"/"+monthStr+"/"+dayStr;
    }

    public String getDateTimeStringOne() {
        return yearStr+monthStr+dayStr+hourStr+minuteStr+secondStr+mescStr;
    }

    public String getDateTimeStringTwo() {
        return getDateString()+"  "+getTimeString();
    }

    /**
     * 获取小时字符串
     * 格式：20:30
     * @return
     */
    public String getTimeString() {
        return hourStr+":"+minuteStr;
    }




    /**
     * 获取对应时间是周几
     * @return
     */
    public String getWeek() {
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String weekStr = "";

        /*星期日:Calendar.SUNDAY=1
         *星期一:Calendar.MONDAY=2
         *星期二:Calendar.TUESDAY=3
         *星期三:Calendar.WEDNESDAY=4
         *星期四:Calendar.THURSDAY=5
         *星期五:Calendar.FRIDAY=6
         *星期六:Calendar.SATURDAY=7 */
        switch (week) {
            case 1:
                weekStr = "周日";
                break;
            case 2:
                weekStr = "周一";
                break;
            case 3:
                weekStr = "周二";
                break;
            case 4:
                weekStr = "周三";
                break;
            case 5:
                weekStr = "周四";
                break;
            case 6:
                weekStr = "周五";
                break;
            case 7:
                weekStr = "周六";
                break;
            default:
                break;
        }

        return weekStr;
    }

    public String getYearStr() {
        return yearStr;
    }

    public String getMonthStr() {
        return monthStr;
    }

    public String getDayStr() {
        return dayStr;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public int getMesc() {
        return mesc;
    }

    public static int differentDays(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2)   //同一年
        {
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++)
            {
                if(i%4==0 && i%100!=0 || i%400==0)    //闰年
                {
                    timeDistance += 366;
                }
                else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2-day1) ;
        }
        else    //不同年
        {
            System.out.println("判断day2 - day1 : " + (day2-day1));
            return day2-day1;
        }
    }
}
