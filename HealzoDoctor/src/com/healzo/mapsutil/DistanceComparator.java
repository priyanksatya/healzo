package com.healzo.mapsutil;

import java.util.Comparator;

public class DistanceComparator implements Comparator<StepsDistance> {
    @Override
    public int compare(StepsDistance step1, StepsDistance step2) {
    	return step1.getDistance() - step2.getDistance();
    }
}