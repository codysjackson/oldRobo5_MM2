package edu.gladstone.hts.parameters;

public class BaseName {
    private String date;
    private String timepoint;
    private String name;
    private int timepointNumber;
    private String hour;
    private String base;


    public BaseName(String date, String name, int timepointNumber, String hour) {
        this.date = date;
        this.name = name;
        this.timepointNumber = timepointNumber;
        this.hour = hour;
        timepoint = getTimepoint();
        base = createImageBaseFilename(date, name, timepoint, hour);
    }

    private static String createImageBaseFilename(String date, String name, String timepoint,
                                                  String hour) {
        String new_base = "PID" + date + "_" + name + "_" + timepoint + "_"
                + hour;
        return new_base;
    }

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getTimepoint() {
        String timepoint = "T" + Integer.toString(timepointNumber);
        return timepoint;
    }

    public int getTimpointNumber() {
        return timepointNumber;
    }

    public String getHour() {
        return hour;
    }

}
