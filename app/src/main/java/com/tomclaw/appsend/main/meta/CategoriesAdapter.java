package com.tomclaw.appsend.main.meta;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.LocaleHelper;

import java.util.List;

/**
 * Created by solkin on 23.09.17.
 */
public class CategoriesAdapter extends BaseAdapter {

    private final
    @NonNull
    List<Category> categories;

    private LayoutInflater inflater;

    public CategoriesAdapter(@NonNull Context context,
                             @NonNull List<Category> categories) {
        this.categories = categories;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflater.inflate(R.layout.category_item, parent, false);
        }
        SVGImageView icon = view.findViewById(R.id.icon);
        TextView names = view.findViewById(R.id.name);
        Category category = getItem(position);
        if (category.getId() == 0) {
            Drawable drawable = view.getResources().getDrawable(R.drawable.close);
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(drawable);
            names.setText(R.string.category_not_defined);
        } else {
            try {
                SVG svg = SVG.getFromString(category.getIcon());
                icon.setSVG(svg);
            } catch (SVGParseException ignored) {
            }
            names.setText(LocaleHelper.getLocalizedName(category));
        }
        return view;
    }
}
