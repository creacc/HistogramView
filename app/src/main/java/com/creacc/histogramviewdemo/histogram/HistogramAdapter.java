package com.creacc.histogramviewdemo.histogram;

public interface HistogramAdapter {

    int getColumnGroupCount();

    String getColumnGroupTitle(int position);

    int getColumnItemCount();

    int getColumnColor(int itemPosition);

    float getColumnValue(int groupPosition, int itemPosition);

    int getRowCount();

    String getRowTitle(int position);
}
