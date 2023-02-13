package entity;

import java.util.Date;
import java.time.LocalTime;

public class DateTime {
    private Date date;
    private LocalTime time;

    /**
     * Constructor
     * @param date - new Date(int year, int month, int date)
     * @param time - new LocalTime(int hour, int minute)
     */
    public DateTime(Date date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }   

    /**
     * @return the time
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    /**
     * @return the date and time
     */
    public String getDateTime() {
        return date.toString() + " " + time.toString();
    }
}
