package com.tomclaw.appsend.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Applies top window insets as padding to this view.
 * Useful for AppBarLayout and Toolbar to avoid status bar overlap.
 */
fun View.applyTopInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(top = insets.top)
        windowInsets
    }
}

/**
 * Applies bottom window insets as padding to this view.
 * Useful for BottomNavigationView to avoid navigation bar overlap.
 */
fun View.applyBottomInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(bottom = insets.bottom)
        windowInsets
    }
}

/**
 * Applies bottom insets as padding, counting the keyboard as well as
 * the system bars and taking whichever is taller.
 *
 * For a view pinned to the bottom that the keyboard opens over — a
 * message box, say. Padding for the navigation bar alone would leave a
 * gap of exactly that height between the view and the raised keyboard.
 */
fun View.applyBottomInsetsWithIme() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        view.updatePadding(bottom = insets.bottom)
        windowInsets
    }
}

/**
 * Applies the bottom inset as padding while the keyboard is down and as
 * margin while it is up, shrinking the view the way `adjustResize` did.
 *
 * For a scrolling container holding text fields. An edge-to-edge window is
 * never resized by the keyboard, so nothing brings the focused field back
 * into sight on its own — and both RecyclerView and NestedScrollView do
 * that off their own height change, which padding alone does not give them.
 * The padding goes away for the same reason: a ScrollView measures scrolling
 * against its full height, so anything left below stays under the keyboard.
 */
fun View.applyBottomInsetsWithImeAsMargin() {
    val initialMargin = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(bottom = if (ime > 0) 0 else bars)
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = initialMargin + ime
        }
        windowInsets
    }
}

/**
 * Applies bottom window insets as margin to this view.
 * Useful for FAB buttons to avoid navigation bar overlap.
 */
fun View.applyBottomInsetsAsMargin() {
    val initialMargin = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = initialMargin + insets.bottom
        }
        windowInsets
    }
}

/**
 * Applies both top and bottom window insets as padding to this view.
 * Useful for scrollable content areas.
 */
fun View.applyVerticalInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(top = insets.top, bottom = insets.bottom)
        windowInsets
    }
}

/**
 * Applies all system bar insets as padding to this view.
 */
fun View.applySystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = insets.left,
            top = insets.top,
            right = insets.right,
            bottom = insets.bottom
        )
        windowInsets
    }
}

/**
 * Sets bottom margin of this view to match the height of another view.
 * The anchor view should already have bottom insets applied.
 * Useful for content areas that need to stay above BottomNavigationView.
 */
fun View.applyBottomMarginForView(anchorView: View) {
    // Wait for anchor view to be laid out with its insets applied
    anchorView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        if (bottom != oldBottom) {
            this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = anchorView.height
            }
        }
    }
    // Initial setup
    anchorView.post {
        this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = anchorView.height
        }
    }
}

