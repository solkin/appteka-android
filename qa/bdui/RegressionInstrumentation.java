package store.appteka.qa.runner;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RegressionInstrumentation extends Instrumentation {
    private Bundle args;
    public void onCreate(Bundle args) { this.args = args; start(); }

    public void onStart() {
        Bundle result = new Bundle();
        try {
            Intent intent = new Intent();
            intent.setClassName("com.tomclaw.appsend", "com.tomclaw.appsend.screen.bdui.BduiScreenActivity");
            intent.putExtra("url", args.getString("url"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Activity activity = startActivitySync(intent);
            ViewGroup root = activity.findViewById(activity.getResources().getIdentifier("bdui_view", "id", "com.tomclaw.appsend"));
            CountDownLatch rendered = new CountDownLatch(1);
            onMain(() -> {
                if (root.getChildCount() > 0) { rendered.countDown(); return; }
                root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        if (root.getChildCount() > 0) {
                            root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            rendered.countDown();
                        }
                    }
                });
            });
            if (!rendered.await(30, TimeUnit.SECONDS)) throw new AssertionError("BDUI did not render");
            if ("menu".equals(args.getString("mode"))) {
                onMain(() -> checkMenu(root));
                waitForIdleSync();
                onMain(() -> {
                    try {
                        Object clicked = root.getClass().getMethod("getHiddenValue", String.class).invoke(root, "qa_clicked");
                        if (!Boolean.TRUE.equals(clicked)) throw new AssertionError("Menu action did not execute");
                    } catch (ReflectiveOperationException failure) { throw new RuntimeException(failure); }
                });
            } else {
                checkFlipper(root);
            }
            result.putString("result", "verified");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            while (failure.getCause() != null) failure = failure.getCause();
            result.putString("failure", failure.toString());
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private void checkMenu(Object root) {
        try {
            Object toolbar = find(root, "qa_root");
            Menu menu = (Menu) toolbar.getClass().getMethod("getMenu").invoke(toolbar);
            expect(4, menu.size());
            expect(Color.parseColor("#009688"), tint(menu.getItem(0)).getDefaultColor());
            expect(Color.parseColor("#AD1457"), tint(menu.getItem(1)).getDefaultColor());
            if (tint(menu.getItem(2)) != null || tint(menu.getItem(3)) != null) {
                throw new AssertionError("Absent or invalid tint must keep the default icon");
            }
            for (int index = 0; index < menu.size(); index++) {
                if (menu.getItem(index).getIcon() == null) throw new AssertionError("Missing icon");
            }
            if (!menu.performIdentifierAction(menu.getItem(0).getItemId(), 0)) {
                throw new AssertionError("Menu action was not handled");
            }
        } catch (ReflectiveOperationException failure) { throw new RuntimeException(failure); }
    }

    private ColorStateList tint(MenuItem item) throws ReflectiveOperationException {
        return (ColorStateList) Class.forName("androidx.core.view.MenuItemCompat")
            .getMethod("getIconTintList", MenuItem.class).invoke(null, item);
    }

    private void checkFlipper(ViewGroup root) throws Exception {
        CountDownLatch flipped = new CountDownLatch(1);
        AtomicReference<ViewFlipper> ref = new AtomicReference<>();
        onMain(() -> {
            expect(3000, value(root, "default_flipper"));
            expect(700, value(root, "configured_flipper"));
            transform(root, "default_flipper", 1234);
            expect(1234, value(root, "default_flipper"));
            expect(700, value(root, "configured_flipper"));
            ViewFlipper flipper = (ViewFlipper) find(root, "default_flipper");
            flipper.setFlipInterval(100);
            expect(100, value(root, "default_flipper"));
            if (Build.VERSION.SDK_INT >= 29) expect(100, flipper.getFlipInterval());
            ref.set(flipper);
            final int initial = flipper.getDisplayedChild();
            flipper.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (flipper.getDisplayedChild() != initial) {
                        flipper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        flipped.countDown();
                    }
                }
            });
            flipper.startFlipping();
        });
        try {
            if (!flipped.await(5, TimeUnit.SECONDS)) throw new AssertionError("Flipper did not advance");
        } finally {
            onMain(() -> { if (ref.get() != null) ref.get().stopFlipping(); });
        }
    }

    private Object find(Object root, String id) {
        try { return root.getClass().getMethod("findBduiViewById", String.class).invoke(root, id); }
        catch (ReflectiveOperationException failure) { throw new RuntimeException(failure); }
    }
    private int value(Object root, String id) {
        try {
            Object value = root.getClass().getMethod("getPropertyValue", String.class, String.class).invoke(root, id, "flipInterval");
            if (!(value instanceof Number)) throw new AssertionError("Missing interval for " + id);
            return ((Number) value).intValue();
        } catch (ReflectiveOperationException failure) { throw new RuntimeException(failure); }
    }
    private void transform(Object root, String id, int value) {
        try { root.getClass().getMethod("transform", String.class, String.class, Object.class).invoke(root, id, "flipInterval", value); }
        catch (ReflectiveOperationException failure) { throw new RuntimeException(failure); }
    }
    private void expect(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    private void onMain(Runnable action) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { action.run(); } catch (Throwable error) { failure.set(error); } });
        if (failure.get() != null) throw new RuntimeException(failure.get());
    }
}
