package com.snatik.matches.ui

import android.content.Context
import com.snatik.matches.R

/** "mm:ss" as shown on the in-game clock and the "won" popup. */
fun Context.formatClock(totalSeconds: Int): String =
    getString(R.string.clock_format, totalSeconds / 60, totalSeconds % 60)
