package com.example.hack2020;

public class HealthdataHelper {

    private String Temperature;
    private String HeartRate;


    public HealthdataHelper(String temperature, String heartRate) {
        this.Temperature = temperature;
        this.HeartRate = heartRate;
    }

    public HealthdataHelper(){

    }



    public String getTemperature() { return Temperature; }

    public void setTemperature(String temperature) {
        this.Temperature = temperature;
    }

    public String getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(String heartRate) {
        this.HeartRate = heartRate;
    }
}
