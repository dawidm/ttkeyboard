package com.example.taptimingkeyboard.keyboard;

import com.example.taptimingkeyboard.data.FlightTimeCharacteristics;

public class WaitingFlightTimeCharacteristics {

    private FlightTimeCharacteristics flightTimeCharacteristics;
    private long firstClickId;

    public WaitingFlightTimeCharacteristics(FlightTimeCharacteristics flightTimeCharacteristics, long firstClickId) {
        this.flightTimeCharacteristics = flightTimeCharacteristics;
        this.firstClickId = firstClickId;
    }

    public FlightTimeCharacteristics getFlightTimeCharacteristics() {
        return flightTimeCharacteristics;
    }

    public long getFirstClickId() {
        return firstClickId;
    }
}
