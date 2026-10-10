package com.stratifyai;

public class ForecastModel extends BaseModel {
    @Override
    public int predict(int[] data) {
        int lastValue = data[data.length - 1];
        return lastValue + 10;
    }
}