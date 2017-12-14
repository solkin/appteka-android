package com.tomclaw.appsend.main.permissions;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.PermissionHelper;

import java.util.List;

import static com.tomclaw.appsend.util.ColorHelper.getAttributedColor;
import static com.tomclaw.appsend.util.PermissionHelper.getPermissionSmallInfo;

/**
 * Created by ivsolkin on 27.01.17.
 */

public class PermissionsAdapter extends RecyclerView.Adapter<PermissionsAdapter.PermissionItemHolder> {

    private Context context;
    private LayoutInflater inflater;

    private List<String> permissions;

    public PermissionsAdapter(Context context, List<String> permissions) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.permissions = permissions;
    }

    @Override
    public PermissionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.permission_large_view, parent, false);
        PermissionItemHolder holder = new PermissionItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PermissionItemHolder holder, int position) {
        String permission = permissions.get(position);
        holder.bind(context, permission);
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    public class PermissionItemHolder extends RecyclerView.ViewHolder {

        private TextView permissionDescription;
        private TextView permissionName;

        public PermissionItemHolder(View itemView) {
            super(itemView);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                int margin = itemView.getResources().getDimensionPixelSize(R.dimen.app_item_margin);
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                p.setMargins(margin, margin / 3, margin, 0); // get rid of margins since shadow area is now the margin
                itemView.setLayoutParams(p);
            }

            permissionDescription = (TextView) itemView.findViewById(R.id.permission_description);
            permissionName = (TextView) itemView.findViewById(R.id.permission_name);
        }

        public void bind(Context context, final String permission) {
            PermissionHelper.PermissionSmallInfo info = getPermissionSmallInfo(context, permission);
            permissionDescription.setText(info.getDescription());
            @ColorInt int descriptionColor = info.isDangerous() ?
                    context.getResources().getColor(R.color.dangerous_permission_color) :
                    getAttributedColor(context, R.attr.text_primary_color);
            permissionDescription.setTextColor(descriptionColor);
            permissionName.setText(permission);
        }
    }
}
