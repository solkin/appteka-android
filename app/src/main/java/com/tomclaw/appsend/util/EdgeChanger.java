package com.tomclaw.appsend.util;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.ScrollView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by solkin on 25/04/16.
 */
public class EdgeChanger {

    private static final Class<?> CLASS_SCROLL_VIEW = ScrollView.class;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Class<?> CLASS_LIST_VIEW = AbsListView.class;
    private static Field LIST_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM;

    private static final Class<?> CLASS_NESTED_SCROLL_VIEW = NestedScrollView.class;
    private static Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Method NESTED_SCROLL_VIEW_METHOD_ENSURE_GLOWS;

    private static final Class<?> CLASS_RECYCLER_VIEW = RecyclerView.class;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Method RECYCLER_VIEW_METHOD_ENSURE_GLOW_TOP;
    private static Method RECYCLER_VIEW_METHOD_ENSURE_GLOW_BOTTOM;

    private static final Class<?> CLASS_VIEW_PAGER = ViewPager.class;
    private static Field VIEW_PAGER_FIELD_EDGE_GLOW_LEFT;
    private static Field VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT;

    static {

        Field edgeGlowTop = null, edgeGlowBottom = null;
        Method ensureGlowTop = null, ensureGlowBottom = null;

        for (Field f : CLASS_SCROLL_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }
        SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;

        for (Field f : CLASS_LIST_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }
        LIST_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;

        for (Field f : CLASS_NESTED_SCROLL_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }
        for (Method m : CLASS_NESTED_SCROLL_VIEW.getDeclaredMethods()) {
            switch (m.getName()) {
                case "ensureGlows":
                    m.setAccessible(true);
                    ensureGlowTop = m;
                    break;
            }
        }
        NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;
        NESTED_SCROLL_VIEW_METHOD_ENSURE_GLOWS = ensureGlowTop;

        for (Field f : CLASS_RECYCLER_VIEW.getDeclaredFields()) {
            switch (f.getName()) {
                case "mTopGlow":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mBottomGlow":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }
        for (Method m : CLASS_RECYCLER_VIEW.getDeclaredMethods()) {
            switch (m.getName()) {
                case "ensureTopGlow":
                    m.setAccessible(true);
                    ensureGlowTop = m;
                    break;
                case "ensureBottomGlow":
                    m.setAccessible(true);
                    ensureGlowBottom = m;
                    break;
            }
        }
        RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP = edgeGlowTop;
        RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM = edgeGlowBottom;
        RECYCLER_VIEW_METHOD_ENSURE_GLOW_TOP = ensureGlowTop;
        RECYCLER_VIEW_METHOD_ENSURE_GLOW_BOTTOM = ensureGlowBottom;

        for (Field f : CLASS_VIEW_PAGER.getDeclaredFields()) {
            switch (f.getName()) {
                case "mLeftEdge":
                    f.setAccessible(true);
                    edgeGlowTop = f;
                    break;
                case "mRightEdge":
                    f.setAccessible(true);
                    edgeGlowBottom = f;
                    break;
            }
        }
        VIEW_PAGER_FIELD_EDGE_GLOW_LEFT = edgeGlowTop;
        VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT = edgeGlowBottom;

    }

    public static void setEdgeGlowColor(AbsListView listView, int color) {

        try {
            setEdgeEffectColor(LIST_VIEW_FIELD_EDGE_GLOW_TOP.get(listView), color);
            setEdgeEffectColor(LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(listView), color);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setEdgeGlowColor(ScrollView scrollView, int color) {

        try {
            setEdgeEffectColor(SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView), color);
            setEdgeEffectColor(SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView), color);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setEdgeGlowColor(final ViewPager viewPager, final int color) {

        try {
            setEdgeEffectColor(VIEW_PAGER_FIELD_EDGE_GLOW_LEFT.get(viewPager), color);
            setEdgeEffectColor(VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT.get(viewPager), color);
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    public static void setEdgeGlowColor(NestedScrollView scrollView, int color) {

        try {
            NESTED_SCROLL_VIEW_METHOD_ENSURE_GLOWS.invoke(scrollView);
            setEdgeEffectColor(NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView), color);
            setEdgeEffectColor(NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView), color);
        } catch (final Exception | NoClassDefFoundError e) {
            e.printStackTrace();
        }

    }

    public static void setEdgeGlowColor(final RecyclerView recyclerView, final int color) {

        try {
            RECYCLER_VIEW_METHOD_ENSURE_GLOW_TOP.invoke(recyclerView);
            RECYCLER_VIEW_METHOD_ENSURE_GLOW_BOTTOM.invoke(recyclerView);
            setEdgeEffectColor(RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP.get(recyclerView), color);
            setEdgeEffectColor(RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(recyclerView), color);
        } catch (final Exception | NoClassDefFoundError e) {
            e.printStackTrace();
        }

    }

    private static void setEdgeEffectColor(Object edgeEffectCompat, int color) {

        try {
            final Field fEdgeEffect = edgeEffectCompat.getClass().getDeclaredField("mEdgeEffect");
            fEdgeEffect.setAccessible(true);
            final EdgeEffect edgeEffect = (EdgeEffect) fEdgeEffect.get(edgeEffectCompat);

            if (Build.VERSION.SDK_INT >= 21) {
                edgeEffect.setColor(color);
                return;
            }

            for (String name : new String[]{"mEdge", "mGlow"}) {
                final Field field = EdgeEffect.class.getDeclaredField(name);
                field.setAccessible(true);
                final Drawable drawable = (Drawable) field.get(edgeEffect);
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                drawable.setCallback(null);
            }
        } catch (final Exception | NoClassDefFoundError e) {
            e.printStackTrace();
        }

    }
}
