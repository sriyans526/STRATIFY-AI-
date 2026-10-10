package com.stratifyai;

public class PatternRecognizer {
    public String recognizePattern(int[] data) {
        int first = data[0];
        int last = data[data.length - 1];

        if (last > first) {
            return "Increasing Trend";
        } else if (last < first) {
            return "Decreasing Trend";
        } else {
            return "Stable Trend";
        }
    }
}