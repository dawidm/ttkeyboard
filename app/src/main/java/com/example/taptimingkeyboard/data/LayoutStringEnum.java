package com.example.taptimingkeyboard.data;

/**
 * For storing enum value with String value that should be displayed in a UI for this enum.
 */
public class LayoutStringEnum {
    
    private final String layoutValue;
    private final Enum enumValue;

    /**
     *
     * @param layoutValue see {@link #getLayoutValue()}
     * @param enumValue     see {@link #getEnumValue()}
     */
    public LayoutStringEnum(String layoutValue, Enum enumValue) {
        this.layoutValue = layoutValue;
        this.enumValue = enumValue;
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
     * @return The value of enum.
     */
    public Enum getEnumValue() {
        return enumValue;
    }

    @Override
    public String toString() {
        return layoutValue;
    }
}
