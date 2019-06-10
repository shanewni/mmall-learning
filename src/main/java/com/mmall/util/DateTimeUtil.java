package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtil {
    private static String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DateTimeUtil.STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String dataToStr(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(DateTimeUtil.STANDARD_FORMAT);
    }
}
