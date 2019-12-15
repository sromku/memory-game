package com.snatik.matches.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoardConfigurationTest {

    BoardConfiguration boardConfiguration = new BoardConfiguration(5);

    @Test
    public void testNumTiles() {
        int numTiles = boardConfiguration.getNumTiles();
        assertEquals(numTiles, 32);
    }

    @Test
    public void testNumTilesInRow() {
        int numTilesInRow = boardConfiguration.getNumTilesInRow();
        assertEquals(numTilesInRow, 8);
    }

    @Test
    public void testNumRows() {
        int numRows = boardConfiguration.getNumRows();
        assertEquals(numRows, 4);
    }

    @Test
    public void testTime() {
        int time = boardConfiguration.getTime();
        assertEquals(time, 180);
    }
}