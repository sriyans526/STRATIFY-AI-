package com.stratifyai;

public class AdvancedForecastModel extends BaseModel {
    @Override
    public int predict(int[] data) {
        int sum = 0;
        for (int value : data) {
            sum = sum + value;
        }
        int average = sum / data.length;
        return average + 10;
    }
}