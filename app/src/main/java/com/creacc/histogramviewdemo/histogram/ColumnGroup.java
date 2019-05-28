package com.creacc.histogramviewdemo.histogram;

class ColumnGroup {

    private String mTitle;

    private float[] mColumnValues;

    public ColumnGroup(String title, float[] columnValues) {
        mTitle = title;
        mColumnValues = columnValues;
    }

    public String getTitle() {
        return mTitle;
    }

    public float[] getColumnValues() {
        return mColumnValues;
    }
}
