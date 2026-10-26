package com.kilonzo;

import java.util.Objects;

public class Location {
    private int row;
    private int col;

    public Location(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return "[" + row + ", " + col + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return row == location.row && col == location.col;
    }

}

