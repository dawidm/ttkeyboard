package com.example.taptimingkeyboard.data;

/**
 * For values stored in database that are displayed with different text in user interface (e.g. different language).
 * Stores String that should be displayed in user interface and corresponding String that is used in application's database.
 *
 */
public class LayoutStringDbString {
    
    private String layoutValue;
    private String dbValue;

    /**
     * Instantiates a new Layout string db string.
     *
     * @param layoutValue see {@link #getLayoutValue()}
     * @param dbValue     see {@link #getDbValue()}
     */
    public LayoutStringDbString(String layoutValue, String dbValue) {
        this.layoutValue = layoutValue;
        this.dbValue = dbValue;
    }

    /**
     * Gets layoutValue.
     *
     * @return The value that should be displayed in layout.
     */
    public String getLayoutValue() {
        return layoutValue;
    }

    /**
     * Gets dbValue.
     *
     * @return The value that is stored in application's database.
     */
    public String getDbValue() {
        return dbValue;
    }

    @Override
    public String toString() {
        return layoutValue;
    }
}
