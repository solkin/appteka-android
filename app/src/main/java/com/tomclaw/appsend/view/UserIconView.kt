package com.tomclaw.appsend.view

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon


interface UserIconView {

    fun setUserIcon(userIcon: UserIcon)

}

class UserIconViewImpl(view: View) : UserIconView {

    private val backView: View = view.findViewById(R.id.icon_back)
    private val svgView: SVGImageView = view.findViewById(R.id.icon_svg)

    override fun setUserIcon(userIcon: UserIcon) {
        var drawable: Drawable = backView.background
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable, Color.parseColor(userIcon.color))
        backView.background = drawable

        val svg = SVG.getFromString(userIcon.icon)
        svgView.setSVG(svg)
    }


}