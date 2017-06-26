package com.tomclaw.appsend.main.view;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.util.ChatLayoutManager;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;

/**
 * Created by ivsolkin on 23.06.17.
 */
public class DiscussView extends MainView {

    private ViewFlipper viewFlipper;
    private TextView errorText;
    private ChatAdapter adapter;
    private ChatLayoutManager chatLayoutManager;

    public DiscussView(AppCompatActivity context) {
        super(context);

        viewFlipper = (ViewFlipper) findViewById(R.id.discuss_view_switcher);

        errorText = (TextView) findViewById(R.id.error_text);

        findViewById(R.id.button_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        findViewById(R.id.get_started_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.discuss_view);

        chatLayoutManager = new ChatLayoutManager(getContext());
        recyclerView.setLayoutManager(chatLayoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        chatLayoutManager.setDataChangedListener(new ChatLayoutManager.DataChangedListener() {
            @Override
            public void onDataChanged() {
//                resetUnreadMessages();
            }
        });

        final int toolbarColor = ColorHelper.getAttributedColor(context, R.attr.toolbar_background);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                EdgeChanger.setEdgeGlowColor(recyclerView, toolbarColor);
            }
        });

        adapter = new ChatAdapter(context, context.getSupportLoaderManager());
        recyclerView.setAdapter(adapter);

        viewFlipper.setDisplayedChild(2);
    }

    @Override
    protected int getLayout() {
        return R.layout.discuss_view;
    }

    @Override
    void activate() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean isFilterable() {
        return false;
    }
}
