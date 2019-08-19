package com.example.taptimingkeyboard.data;

public class LayoutStringDbString {
    private String layoutValue;
    private String dbValue;

    public LayoutStringDbString(String layoutValue, String dbValue) {
        this.layoutValue = layoutValue;
        this.dbValue = dbValue;
    }

    public String getLayoutValue() {
        return layoutValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @Override
    public String toString() {
        return layoutValue;
    }
}
