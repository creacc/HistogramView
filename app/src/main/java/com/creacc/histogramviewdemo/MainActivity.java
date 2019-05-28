package com.creacc.histogramviewdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.creacc.histogramviewdemo.histogram.HistogramAdapter;
import com.creacc.histogramviewdemo.histogram.HistogramView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HistogramView histogramView = findViewById(R.id.histogram_view);
        // for test
        histogramView.setAdapter(new HistogramAdapter() {
            @Override
            public int getColumnGroupCount() {
                return 6;
            }

            @Override
            public String getColumnGroupTitle(int position) {
                return "123";
            }

            @Override
            public int getColumnItemCount() {
                return 3;
            }

            @Override
            public int getColumnColor(int itemPosition) {
                return Color.parseColor("#338899");
            }

            @Override
            public float getColumnValue(int groupPosition, int itemPosition) {
                return 0.6f;
            }

            @Override
            public int getRowCount() {
                return 6;
            }

            @Override
            public String getRowTitle(int position) {
                return "123";
            }
        });
    }
}
