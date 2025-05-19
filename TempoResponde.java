package com.example.safetydrones.Tempo;

import java.util.List;

public class TempoResponde {
    public List<Day> days;
    public CurrentConditions currentConditions;

    public static class CurrentConditions {
        public double temp;
        public double windspeed;
        public String conditions;
    }


    public static class Day {
        public String datetime;
        public double temp;
        public String conditions;
        public double windspeed;
    }
}
