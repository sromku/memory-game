package com.snatik.matches.model;

public class BoardConfiguration {

	public static final int _6 = 6;
	public static final int _12 = 12;
	public static final int _18 = 18;
	public static final int _28 = 28;
	public static final int _32 = 32;
	public static final int _50 = 50;

	public final int difficulty;
	public final int numTiles;
	public final int numTilesInRow;
	public final int numRows;
	public final int time;

	public BoardConfiguration(int numTiles) {
		this.numTiles = numTiles;
		switch (numTiles) {
		case _6:
			difficulty = 1;
			numTilesInRow = 3;
			numRows = 2;
			time = 60;
			break;
		case _12:
			difficulty = 2;
			numTilesInRow = 4;
			numRows = 3;
			time = 90;
			break;
		case _18:
			difficulty = 3;
			numTilesInRow = 6;
			numRows = 3;
			time = 120;
			break;
		case _28:
			difficulty = 4;
			numTilesInRow = 7;
			numRows = 4;
			time = 150;
			break;
		case _32:
			difficulty = 5;
			numTilesInRow = 8;
			numRows = 4;
			time = 180;
			break;
		case _50:
			difficulty = 6;
			numTilesInRow = 10;
			numRows = 5;
			time = 210;
			break;	
		default:
			throw new IllegalArgumentException("Select one of predefined sizes");
		}
	}

}
