package com.snatik.matches.ui

import android.view.View

/** This many density-independent pixels, in pixels of [view]'s display. */
fun Int.dp(view: View): Float = this * view.resources.displayMetrics.density
