package edu.gladstone.hts.parameters;

public class WellParser {
    private String name;
    private int row;
    private int column;

    public WellParser(String s) {
        s.toUpperCase();
        name = s;
        if (!s.equalsIgnoreCase("FIDUCIARY")){
            this.row = s.charAt(0) - 64; // Returns 1 for "A"
            String columnString = s.substring(1).trim();
            this.column = Integer.parseInt(columnString);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

}