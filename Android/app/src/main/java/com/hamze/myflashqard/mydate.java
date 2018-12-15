package com.hamze.myflashqard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class mydate {

    //date format. used in some date format conversions
    static final public SimpleDateFormat date_format;

    //A very old dummy constant time (represents time=-inf)
    static final public Calendar OLD_TIME;

    //initialization of static fields
    static{
        date_format = new SimpleDateFormat("d.M.yyyy");

        //just a very old dummy time (meaning time=-inf)
        OLD_TIME = Calendar.getInstance();
        OLD_TIME.set(Calendar.YEAR, 1000); //year=1000(very old)
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get today (+ day_offset)
    static Date getToday(int day_offset) {
        //Today
        Calendar cal = Calendar.getInstance();

        //Today + day_offset
        cal.add(Calendar.DATE, day_offset);

        //our precision is day.
        cal.clear(cal.MILLISECOND);
        cal.clear(cal.SECOND);
        cal.clear(cal.MINUTE);
        //cal.clear(cal.HOUR_OF_DAY);
        //cal.clear(cal.HOUR);
        cal.set(Calendar.HOUR_OF_DAY, 0); //two above did not work, because they consider GMT time

        Date today = cal.getTime();

        return today;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get today (plus day_offset), with my format
    static String getToday_formatted(int day_offset){
        Date today = getToday(day_offset);
        String today_formatted = date_format.format(today);
        return today_formatted;
    }


}
