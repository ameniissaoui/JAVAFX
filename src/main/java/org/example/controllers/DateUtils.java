package org.example.controllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    /**
     * Converts a java.util.Date to java.time.LocalDate.
     *
     * @param date The java.util.Date to convert.
     * @return The corresponding LocalDate, or null if the input is null.
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            System.out.println("DEBUG: Null date encountered in toLocalDate");
            return null;
        }
        try {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to convert Date to LocalDate: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a java.time.LocalDate to java.util.Date.
     *
     * @param localDate The java.time.LocalDate to convert.
     * @return The corresponding Date, or null if the input is null.
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            System.out.println("DEBUG: DateUtils.toDate received a null LocalDate.");
            return null;
        }
        try {
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to convert LocalDate to Date: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}