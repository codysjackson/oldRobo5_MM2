package edu.gladstone.hts.parameters;

// For data from scheduler
public class ImageTemplateParser {
    private String data;
    private String delim = "[,][ ]";
    private String[] tokens;

    public ImageTemplateParser(String data) {
        this.data = data;
        this.tokens = getTokens();
    }

    public String[] getTokens() {
        String[] tokens = data.split(delim);
        return tokens;
    }

    public String getFilepath() {
        String filename = tokens[0].substring(6);
        return filename;
    }

    public int getTimepointNumber() {
        int timepointNumber = Integer.parseInt(tokens[2].split("[=][ ]")[1]);
        return timepointNumber;
    }

    public String getHour() {
        String hour = tokens[3].split("[=][ ]")[1];
        return hour;
    }

    public String getExperimentName() {
        String hour = tokens[4].split("[=][ ]")[1];
        return hour;
    }

}
