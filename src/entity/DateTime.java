package entity;

public class DateTime {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minutes;

    /**
     * Constructor
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minutes
     */
    public DateTime(int year, int month, int day, int hour, int minutes) {
        if(year < 0 || month < 0 || day < 0 || hour < 0 || minutes < 0 || month > 12 || day > 31 || hour > 23 || minutes > 59) {
            throw new IllegalArgumentException("Invalid date/time");
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minutes = minutes;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * @param hour the hour to set
     */
    public void setHour(int hour) {
        this.hour = hour;
    }

    /**
     * @return the minute
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @param minute the minute to set
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return String.format("%d-%d-%d %d:%d", day, month, year, hour, minutes);
    }

}
