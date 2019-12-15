package com.snatik.matches.common;

import android.media.MediaPlayer;

import com.snatik.matches.R;

import org.junit.Test;

import static org.junit.Assert.*;

public class MusicTest {

    @Test
    public void testPlayBackgroundMusic(){
        assertTrue(Music.isMusicOn());
    }

    @Test
    public void testStopBackgroundMusic() {
        Music.OFF = true;
        assertFalse(Music.isMusicOn());
    }

    @Test
    public void testMusicOn() {
        Music.OFF = true;
        assertFalse(Music.isMusicOn());

        Music.OFF = false;
        assertTrue(Music.isMusicOn());
    }
}