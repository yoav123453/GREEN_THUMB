package com.yoav_s.helper;

import android.annotation.SuppressLint;
import android.os.Build; // Required for Build.VERSION.SDK_INT

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle; // From your original class
import java.time.chrono.IsoChronology; // From your original class
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * A comprehensive utility class for handling date and time operations
 * using Java 8's java.time.* classes, with Android API level considerations.
 * <p>
 * This class provides static methods for:
 * <ul>
 * <li>Converting between String, Long (epoch milliseconds), and java.time objects (LocalDate, LocalDateTime, LocalTime).</li>
 * <li>Handling time zones, defaulting to the system's current zone or allowing a specific zone.</li>
 * <li>Formatting and parsing dates with predefined or custom patterns.</li>
 * <li>Calculating age, periods between dates, and comparing dates/times.</li>
 * <li>Common helper functions like isToday, isYesterday, getStartOfDay, etc.</li>
 * <li>Integration with Android's Material DatePicker constraints.</li>
 * </ul>
 * Note: Methods using 'java.time' APIs are guarded by Android API level checks (>= O / 26).
 * On older Android versions, these methods will return null or default values.
 * Consider using the 'ThreeTen Android Backport' library for full 'java.time' functionality on older Android versions.
 * This class is final and cannot be instantiated.
 */
public final class DateUtil {

    // --- CONSTANTS ---

    /**
     * Common Israeli/European date format: Day/Month/Full-Year (e.g., 23/06/2025).
     */
    public static final String FORMAT_DD_MM_YYYY = "dd/MM/yyyy";

    /**
     * Common Israeli/European date format: Day/Month/Short-Year (e.g., 23/06/25).
     */
    public static final String FORMAT_DD_MM_YY = "dd/MM/yy";

    /**
     * Common time format: Hour:Minute:Second (e.g., 17:58:30).
     */
    public static final String FORMAT_HH_MM_SS = "HH:mm:ss";

    /**
     * Common time format: Hour:Minute (e.g., 17:58).
     */
    public static final String FORMAT_HH_MM = "HH:mm";

    /**
     * ISO 8601 combined date and time format used often in APIs.
     */
    public static final String FORMAT_ISO_LOCAL_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DateUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // --- CORE CONVERSION METHODS (STRING <-> OBJECT) ---

    /**
     * Converts a string to a LocalDate using the default format "d/M/uuuu".
     *
     * @param dateString The string representation of the date (e.g., "23/6/2025").
     * @return The corresponding LocalDate object, or null if parsing fails or on older Android versions.
     */
    public static LocalDate stringToLocalDate(String dateString) {
        return stringToLocalDate(dateString, FORMAT_DD_MM_YYYY); // Using a more common default
    }

    /**
     * Converts a string to a LocalDate using a specified format.
     *
     * @param dateString The string representation of the date (e.g., "23/06/2025").
     * @param format     The format pattern (e.g., "dd/MM/yyyy").
     * @return The corresponding LocalDate object, or null if parsing fails or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static LocalDate stringToLocalDate(String dateString, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dateString == null || format == null) return null;
        try {
            // Added Chronology and ResolverStyle from your original class for strict parsing
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format)
                    .withChronology(IsoChronology.INSTANCE)
                    .withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date string '" + dateString + "' with format '" + format + "': " + e.getMessage());
            // In a real Android app, use Log.e("DateUtil", "Error message", e);
            return null;
        }
    }

    /**
     * Converts a LocalDate object to a formatted string using the default format "dd/MM/uuuu".
     *
     * @param date The LocalDate object to format.
     * @return The formatted date string, or null if the date is null or on older Android versions.
     */
    public static String localDateToString(LocalDate date) {
        return localDateToString(date, FORMAT_DD_MM_YYYY);
    }

    /**
     * Converts a LocalDate object to a formatted string.
     *
     * @param date   The LocalDate object to format.
     * @param format The desired format pattern (e.g., "dd/MM/yyyy").
     * @return The formatted date string, or null if the date is null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static String localDateToString(LocalDate date, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null || format == null) return null;
        try {
            // Added Chronology and ResolverStyle from your original class for strict formatting
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format)
                    .withChronology(IsoChronology.INSTANCE)
                    .withResolverStyle(ResolverStyle.STRICT);
            return date.format(formatter);
        } catch (Exception e) { // Catch broader exception for formatting, though DateTimeParseException is less likely here
            System.err.println("Error formatting LocalDate '" + date + "' with format '" + format + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a string to a LocalDateTime using a specified format.
     *
     * @param dateTimeString The string representation of the date and time (e.g., "23/06/2025 17:58:30").
     * @param format         The format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The corresponding LocalDateTime object, or null if parsing fails or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static LocalDateTime stringToLocalDateTime(String dateTimeString, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dateTimeString == null || format == null) return null;
        try {
            if (!dateTimeString.contains(" ")) {
                dateTimeString = dateTimeString + " 00:00";
                format = format + " HH:mm";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime date = LocalDateTime.parse(dateTimeString, formatter);
            return date;
            //return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date-time string '" + dateTimeString + "' with format '" + format + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a LocalDateTime object to a formatted string.
     *
     * @param dateTime The LocalDateTime object to format.
     * @param format   The desired format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The formatted date-time string, or null if the object is null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static String localDateTimeToString(LocalDateTime dateTime, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dateTime == null || format == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return dateTime.format(formatter);
    }

    // --- CORE CONVERSION METHODS (LONG <-> OBJECT) ---

    /**
     * Converts epoch milliseconds to a LocalDateTime using a specific time zone.
     *
     * @param epochMilli The number of milliseconds since the epoch (1970-01-01T00:00:00Z).
     * @param zoneId     The time zone to interpret the date and time in.
     * @return The corresponding LocalDateTime object, or null on older Android versions.
     */
    @SuppressLint("NewApi")
    public static LocalDateTime longToLocalDateTime(long epochMilli, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        return Instant.ofEpochMilli(epochMilli).atZone(Objects.requireNonNull(zoneId)).toLocalDateTime();
    }

    /**
     * Converts epoch milliseconds to a LocalDateTime using the system's default time zone.
     *
     * @param epochMilli The number of milliseconds since the epoch.
     * @return The corresponding LocalDateTime object in the system's default time zone, or null on older Android versions.
     */
    public static LocalDateTime longToLocalDateTime(long epochMilli) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        return longToLocalDateTime(epochMilli, ZoneId.systemDefault());
    }

    /**
     * Converts a LocalDateTime object to epoch milliseconds using a specific time zone.
     *
     * @param dateTime The LocalDateTime object.
     * @param zoneId   The time zone to apply for the conversion.
     * @return The number of milliseconds since the epoch. Returns -1 if dateTime is null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static long localDateTimeToLong(LocalDateTime dateTime, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dateTime == null) return -1;
        return dateTime.atZone(Objects.requireNonNull(zoneId)).toInstant().toEpochMilli();
    }

    /**
     * Converts a LocalDateTime object to epoch milliseconds using the system's default time zone.
     *
     * @param dateTime The LocalDateTime object.
     * @return The number of milliseconds since the epoch. Returns -1 if dateTime is null or on older Android versions.
     */
    public static long localDateTimeToLong(LocalDateTime dateTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return -1;
        return localDateTimeToLong(dateTime, ZoneId.systemDefault());
    }

    /**
     * Converts epoch milliseconds to a LocalDate. The time part is truncated.
     *
     * @param epochMilli The number of milliseconds since the epoch.
     * @param zoneId     The time zone to interpret the date in.
     * @return The corresponding LocalDate object, or null on older Android versions.
     */
    @SuppressLint("NewApi")
    public static LocalDate longToLocalDate(long epochMilli, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        return Instant.ofEpochMilli(epochMilli).atZone(Objects.requireNonNull(zoneId)).toLocalDate();
    }

    /**
     * Converts epoch milliseconds to a LocalDate using the system's default time zone.
     *
     * @param epochMilli The number of milliseconds since the epoch.
     * @return The corresponding LocalDate object, or null on older Android versions.
     */
    public static LocalDate longToLocalDate(long epochMilli) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        return longToLocalDate(epochMilli, ZoneId.systemDefault());
    }

    /**
     * Converts a LocalDate to epoch milliseconds, representing the start of that day (00:00:00).
     *
     * @param date   The LocalDate object.
     * @param zoneId The time zone to apply for the conversion.
     * @return The number of milliseconds since the epoch for the start of the given date. Returns -1 if date is null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static long localDateToLong(LocalDate date, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return -1;
        return date.atStartOfDay(Objects.requireNonNull(zoneId)).toInstant().toEpochMilli();
    }

    /**
     * Converts a LocalDate to epoch milliseconds using the system's default time zone (start of day).
     *
     * @param date The LocalDate object.
     * @return The number of milliseconds since the epoch for the start of the given date. Returns -1 if date is null or on older Android versions.
     */
    public static long localDateToLong(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return -1;
        return localDateToLong(date, ZoneId.systemDefault());
    }

    // --- DIRECT CONVERSION (STRING <-> LONG) ---

    /**
     * Converts a formatted date-time string directly to epoch milliseconds.
     * This is a convenience method combining stringToLocalDateTime and localDateTimeToLong.
     *
     * @param dateTimeString The string representation of the date and time.
     * @param format         The format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @param zoneId         The time zone to apply for the conversion.
     * @return The number of milliseconds since the epoch. Returns -1 if parsing fails, input is null, or on older Android versions.
     */
    public static long stringToLong(String dateTimeString, String format, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return -1;
        LocalDateTime localDateTime = stringToLocalDateTime(dateTimeString, format);
        if (localDateTime == null) {
            return -1;
        }
        return localDateTimeToLong(localDateTime, zoneId);
    }

    /**
     * Converts a formatted date-time string directly to epoch milliseconds using the system default time zone.
     *
     * @param dateTimeString The string representation of the date and time.
     * @param format         The format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The number of milliseconds since the epoch. Returns -1 if parsing fails, input is null, or on older Android versions.
     */
    public static long stringToLong(String dateTimeString, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return -1;
        return stringToLong(dateTimeString, format, ZoneId.systemDefault());
    }

    /**
     * Converts epoch milliseconds directly to a formatted date-time string.
     * This is a convenience method combining longToLocalDateTime and localDateTimeToString.
     *
     * @param epochMilli The number of milliseconds since the epoch.
     * @param format     The desired format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @param zoneId     The time zone to interpret the date and time in.
     * @return The formatted date-time string, or null if the format is null or on older Android versions.
     */
    public static String longToString(long epochMilli, String format, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        LocalDateTime localDateTime = longToLocalDateTime(epochMilli, zoneId);
        return localDateTimeToString(localDateTime, format);
    }

    /**
     * Converts epoch milliseconds directly to a formatted date-time string using the system default time zone.
     *
     * @param epochMilli The number of milliseconds since the epoch.
     * @param format     The desired format pattern (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The formatted date-time string, or null if the format is null or on older Android versions.
     */
    public static String longToString(long epochMilli, String format) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        return longToString(epochMilli, format, ZoneId.systemDefault());
    }

    // --- UTILITY FUNCTIONS ---

    public enum Periods {YEARS, MONTHS, DAYS, YEARS_MONTHS, YEAR_MONTHS_DAYS}

    /**
     * Calculates the age based on a birth date.
     *
     * @param birthDate The date of birth.
     * @return The age in whole years, or -1 if the birthDate is null, in the future, or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static int calculateAge(LocalDate birthDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || birthDate == null || birthDate.isAfter(LocalDate.now())) {
            return -1;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calculates the age based on a birth date, returning a formatted string based on specified periods.
     *
     * @param dateOfBirth The date of birth.
     * @param periods     The desired period format (e.g., YEARS, MONTHS).
     * @return A formatted string representing the age, or "-1" on older Android versions or if inputs are invalid.
     */
    public static String age(LocalDate dateOfBirth, Periods periods) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "-1";
        return DateUtil.age(dateOfBirth, LocalDate.now(), periods);
    }

    /**
     * Calculates the period between two dates, returning a formatted string based on specified periods.
     *
     * @param fromDate The start date.
     * @param toDate   The end date.
     * @param periods  The desired period format (e.g., YEARS, MONTHS, DAYS).
     * @return A formatted string representing the period, or an empty string on older Android versions or if inputs are invalid.
     */
    @SuppressLint("NewApi")
    public static String age(LocalDate fromDate, LocalDate toDate, Periods periods) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || fromDate == null || toDate == null)
            return "";

        Period period = Period.between(fromDate, toDate);

        switch (periods) {
            case YEARS: {
                return String.valueOf(period.getYears());
            }
            case MONTHS: {
                return String.valueOf(period.getMonths());
            }
            case DAYS: {
                return String.valueOf(period.getDays());
            }
            case YEAR_MONTHS_DAYS: {
                return String.valueOf(period.getYears()) + "|" +
                        String.valueOf(period.getMonths()) + "|" +
                        String.valueOf(period.getDays());
            }
            case YEARS_MONTHS: {
                return String.valueOf(period.getYears()) + "|" +
                        String.valueOf(period.getMonths());
            }
            default:
                return null;
        }
    }


    /**
     * Checks if a given date is today.
     *
     * @param date The date to check.
     * @return true if the date is today, false otherwise or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static boolean isToday(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return false;
        return date.equals(LocalDate.now());
    }

    /**
     * Checks if a given date was yesterday.
     *
     * @param date The date to check.
     * @return true if the date was yesterday, false otherwise or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static boolean isYesterday(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return false;
        return date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * Checks if a given date is in a weekend in Israel (Friday or Saturday).
     *
     * @param date The date to check.
     * @return true if the date is a Friday or Saturday, false otherwise or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static boolean isWeekendInIsrael(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return false;
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
    }

    /**
     * Calculates the number of days between two dates.
     *
     * @param startDate The start date (inclusive).
     * @param endDate   The end date.
     * @param inclusive If true, the end date is included in the count.
     * @return The total number of days between the two dates, or 0 on older Android versions or if inputs are null.
     */
    @SuppressLint("NewApi")
    public static long daysBetween(LocalDate startDate, LocalDate endDate, boolean inclusive) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || startDate == null || endDate == null)
            return 0;
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return inclusive ? days + 1 : days;
    }

    /**
     * Calculates the period between two dates and returns it as a human-readable string.
     * e.g., "1 year, 2 months, 10 days"
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return A formatted string representing the period. Returns an empty string if dates are null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static String periodBetweenToString(LocalDate startDate, LocalDate endDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || startDate == null || endDate == null)
            return "";

        Period period = Period.between(startDate, endDate);
        List<String> parts = new ArrayList<>();

        int years = period.getYears();
        if (years > 0) parts.add(years + (years == 1 ? " year" : " years"));

        int months = period.getMonths();
        if (months > 0) parts.add(months + (months == 1 ? " month" : " months"));

        int days = period.getDays();
        if (days > 0) parts.add(days + (days == 1 ? " day" : " days"));

        if (parts.isEmpty()) return "0 days";

        return String.join(", ", parts);
    }

    /**
     * Calculates the duration between two LocalDateTime objects.
     *
     * @param startDateTime The start date and time.
     * @param endDateTime   The end date and time.
     * @return A Duration object representing the time between the two points, or Duration.ZERO on older Android versions or if inputs are null.
     */
    @SuppressLint("NewApi")
    public static Duration durationBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || startDateTime == null || endDateTime == null)
            return Duration.ZERO;
        return Duration.between(startDateTime, endDateTime);
    }

    /**
     * Calculates the duration between two date-times and returns it as a human-readable string.
     * e.g., "5 days, 8 hours, 15 minutes, 30 seconds"
     *
     * @param startDateTime The start date and time.
     * @param endDateTime   The end date and time.
     * @return A formatted string representing the duration. Returns an empty string if dates are null or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static String durationBetweenToString(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || startDateTime == null || endDateTime == null)
            return "";

        Duration duration = Duration.between(startDateTime, endDateTime).abs();
        List<String> parts = new ArrayList<>();

        long days = duration.toDays();
        if (days > 0) parts.add(days + (days == 1 ? " day" : " days"));

        long hours = duration.toHours() % 24;
        if (hours > 0) parts.add(hours + (hours == 1 ? " hour" : " hours"));

        long minutes = duration.toMinutes() % 60;
        if (minutes > 0) parts.add(minutes + (minutes == 1 ? " minute" : " minutes"));

        long seconds = duration.getSeconds() % 60;
        if (seconds > 0) parts.add(seconds + (seconds == 1 ? " second" : " seconds"));

        if (parts.isEmpty()) return "0 seconds";

        return String.join(", ", parts);
    }

    /**
     * Gets the first day of the month for a given date.
     *
     * @param date The date.
     * @return A new LocalDate object representing the first day of the month, or null on older Android versions or if date is null.
     */
    @SuppressLint("NewApi")
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return null;
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Gets the last day of the month for a given date.
     *
     * @param date The date.
     * @return A new LocalDate object representing the last day of the month, or null on older Android versions or if date is null.
     */
    @SuppressLint("NewApi")
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return null;
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Gets the first day of the week for a given date.
     * For example, in Israel, the first day is Sunday. In the US/Europe, it's often Monday.
     *
     * @param date         The date.
     * @param firstDayOfWeek The DayOfWeek that should be considered the start of the week (e.g., DayOfWeek.SUNDAY).
     * @return A new LocalDate object representing the first day of the week, or null on older Android versions or if inputs are null.
     */
    @SuppressLint("NewApi")
    public static LocalDate getFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null || firstDayOfWeek == null)
            return null;
        return date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
    }

    /**
     * Gets the last day of the week for a given date.
     * This is calculated by adding 6 days to the first day of the week.
     *
     * @param date         The date.
     * @param firstDayOfWeek The DayOfWeek that should be considered the start of the week (e.g., DayOfWeek.SUNDAY).
     * @return A new LocalDate object representing the last day of the week, or null on older Android versions or if inputs are null.
     */
    @SuppressLint("NewApi")
    public static LocalDate getLastDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null || firstDayOfWeek == null)
            return null;
        // Find the first day of the week and add 6 days to get the last day.
        LocalDate firstDay = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
        return firstDay.plusDays(6);
    }

    /**
     * Gets the LocalDateTime for the start of a given day (00:00:00).
     *
     * @param date The date.
     * @return The LocalDateTime at the start of the day, or null on older Android versions or if date is null.
     */
    @SuppressLint("NewApi")
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return null;
        return date.atStartOfDay();
    }

    /**
     * Gets the LocalDateTime for the end of a given day (23:59:59.999999999).
     *
     * @param date The date.
     * @return The LocalDateTime at the end of the day, or null on older Android versions or if date is null.
     */
    @SuppressLint("NewApi")
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) return null;
        return date.atTime(LocalTime.MAX);
    }

    // --- COMPARISON METHODS ---

    /**
     * Checks if a date is within a specified range.
     *
     * @param dateToCheck  The date to check.
     * @param startRange   The start of the range (inclusive).
     * @param endRange     The end of the range (inclusive).
     * @param includeEdges If true, the start and end dates are included in the range.
     * @return true if the date is within the range, false otherwise or on older Android versions.
     */
    @SuppressLint("NewApi")
    public static boolean inRange(LocalDate dateToCheck, LocalDate startRange, LocalDate endRange, boolean includeEdges) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dateToCheck == null || startRange == null || endRange == null)
            return false; // Or handle as per your app's logic for older APIs

        if (includeEdges) {
            return (dateToCheck.isEqual(startRange) || dateToCheck.isAfter(startRange)) &&
                    (dateToCheck.isEqual(endRange) || dateToCheck.isBefore(endRange));
        } else {
            return dateToCheck.isAfter(startRange) && dateToCheck.isBefore(endRange);
        }
    }

    /**
     * Checks if a date is within a specified range, including the start and end dates.
     *
     * @param dateToCheck The date to check.
     * @param startRange  The start of the range (inclusive).
     * @param endRange    The end of the range (inclusive).
     * @return true if the date is within the range, false otherwise or on older Android versions.
     */
    public static boolean inRange(LocalDate dateToCheck, LocalDate startRange, LocalDate endRange) {
        return inRange(dateToCheck, startRange, endRange, true);
    }

    @SuppressLint("NewApi")
    public static boolean isDateAfter(LocalDate date1, LocalDate date2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date1 == null || date2 == null)
            return false;
        return date1.isAfter(date2);
    }

    @SuppressLint("NewApi")
    public static boolean isDateBefore(LocalDate date1, LocalDate date2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date1 == null || date2 == null)
            return false;
        return date1.isBefore(date2);
    }

    @SuppressLint("NewApi")
    public static boolean isDateEqual(LocalDate date1, LocalDate date2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date1 == null || date2 == null)
            return false;
        return date1.isEqual(date2);
    }

    @SuppressLint("NewApi")
    public static boolean isDateTimeAfter(LocalDateTime dt1, LocalDateTime dt2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dt1 == null || dt2 == null)
            return false;
        return dt1.isAfter(dt2);
    }

    @SuppressLint("NewApi")
    public static boolean isDateTimeBefore(LocalDateTime dt1, LocalDateTime dt2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dt1 == null || dt2 == null)
            return false;
        return dt1.isBefore(dt2);
    }

    @SuppressLint("NewApi")
    public static boolean isDateTimeEqual(LocalDateTime dt1, LocalDateTime dt2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || dt1 == null || dt2 == null)
            return false;
        return dt1.isEqual(dt2);
    }

    @SuppressLint("NewApi")
    public static boolean isTimeAfter(LocalTime t1, LocalTime t2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || t1 == null || t2 == null)
            return false;
        return t1.isAfter(t2);
    }

    @SuppressLint("NewApi")
    public static boolean isTimeBefore(LocalTime t1, LocalTime t2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || t1 == null || t2 == null)
            return false;
        return t1.isBefore(t2);
    }

    @SuppressLint("NewApi")
    public static boolean isTimeEqual(LocalTime t1, LocalTime t2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || t1 == null || t2 == null)
            return false;
        // LocalTime has no isEqual method, use compareTo
        return t1.compareTo(t2) == 0;
    }

    // --- Android UI Specific Functions ---

    /**
     * Builds CalendarConstraints for Android's Material DatePicker.
     *
     * @param startDate The minimum selectable date.
     * @param endDate   The maximum selectable date.
     * @return A CalendarConstraints object, or null on older Android versions.
     */
    public static CalendarConstraints buidCalendarConstrains(LocalDate startDate, LocalDate endDate) {
        return buidCalendarConstrains(startDate, endDate, null);
    }

    /**
     * Builds CalendarConstraints for Android's Material DatePicker.
     *
     * @param startDate  The minimum selectable date.
     * @param endDate    The maximum selectable date.
     * @param openAtDate The date to open the calendar view at. If null, it defaults to startDate.
     * @return A CalendarConstraints object, or null on older Android versions.
     */
    @SuppressLint("NewApi")
    public static CalendarConstraints buidCalendarConstrains(LocalDate startDate, LocalDate endDate, LocalDate openAtDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || startDate == null || endDate == null)
            return null;

        long dateStart = ZonedDateTime.ofLocal(startDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli();
        long dateEnd = ZonedDateTime.ofLocal(endDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli();
        long openAt = (openAtDate != null) ? ZonedDateTime.ofLocal(openAtDate.atStartOfDay(), ZoneId.systemDefault(), ZoneOffset.ofHours(0)).toInstant().toEpochMilli() : dateStart;

        // Ensure these Material components are in your build.gradle if you use this
        CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(dateStart);
        CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(dateEnd);

        ArrayList<CalendarConstraints.DateValidator> listValidators = new ArrayList<>();
        listValidators.add(dateValidatorMin);
        listValidators.add(dateValidatorMax);

        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);

        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(validators);
        constraintsBuilderRange.setStart(dateStart);
        constraintsBuilderRange.setEnd(dateEnd);
        constraintsBuilderRange.setOpenAt(openAt);

        return constraintsBuilderRange.build();
    }

    // --- Legacy java.util.Date conversions (kept for reference, generally avoid in new code) ---

    // @SuppressLint("NewApi")
    // public static Date localDateToDate(LocalDate localDate) {
    //     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || localDate == null) {
    //         return null;
    //     }
    //     return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    // }

    // @SuppressLint("NewApi")
    // public static LocalDate dateToLocalDate(Date date) {
    //     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || date == null) {
    //         return null;
    //     }
    //     return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    // }
}