package com.snatik.matches.model;

public class Board {

	public static final int _6 = 6;
	public static final int _12 = 12;
	public static final int _20 = 20;
	public static final int _30 = 30;
	public static final int _40 = 40;
	public static final int _54 = 54;

	public final int difficulty;
	public final int numTiles;
	public final int numTilesInRow;
	public final int numRows;

	public Board(int numTiles) {
		this.numTiles = numTiles;
		switch (numTiles) {
		case _6:
			difficulty = 1;
			numTilesInRow = 3;
			numRows = 2;
			break;
		case _12:
			difficulty = 2;
			numTilesInRow = 4;
			numRows = 3;
			break;
		case _20:
			difficulty = 3;
			numTilesInRow = 5;
			numRows = 4;
			break;
		case _30:
			difficulty = 4;
			numTilesInRow = 6;
			numRows = 5;
			break;
		case _40:
			difficulty = 5;
			numTilesInRow = 8;
			numRows = 5;
			break;
		case _54:
			difficulty = 6;
			numTilesInRow = 9;
			numRows = 6;
			break;	
		default:
			throw new IllegalArgumentException("Select one of predefined sizes");
		}
	}

}
