/*
 * Copyright (C) 1999-2018 Alibaba Group Holding Limited
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.tuna.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Comment of DateUtil
 *
 *
 */
public final class DateUtil {
    public static final String DEFAULT_DATE_FORMAT_STR = "yyyyMMddHHmmssSSSZ";
    private static SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STR);
    
    public static String format(Date d) {
        return format(d, null);
    }
    
    public static String format(Date d, String pattern) {
        return format(d, pattern, null);
    }
    
    public static String format(Date d, String pattern, TimeZone timeZone) {
        if (d == null){
            return null;
        }
        final SimpleDateFormat format;
        if(pattern != null){
            format = new SimpleDateFormat(pattern);
        }else{
            format = (SimpleDateFormat)DEFAULT_FORMAT.clone();
        }
        if(timeZone != null){
            format.setTimeZone(timeZone);
        }
        return format.format(d);
    }

    public static Date parse(String str) throws ParseException{
        return parse(str, null);
    }
    
    public static Date parse(String source, String pattern) throws ParseException{
        return parse(source, pattern, null);
    }
    
    public static Date parse(String source, String pattern, TimeZone timeZone) throws ParseException{
        if (source == null){
            return null;
        }
        final SimpleDateFormat format;
        if(pattern != null){
            format = new SimpleDateFormat(pattern);
        }else{
            format = (SimpleDateFormat)DEFAULT_FORMAT.clone();
        }
        if(timeZone != null){
            format.setTimeZone(timeZone);
        }
        return format.parse(source);
    }
    
    private DateUtil(){};
}
