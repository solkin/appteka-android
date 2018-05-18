package com.tomclaw.appsend.util;

import com.tomclaw.appsend.R;

/**
 * Created by solkin on 18/03/2018.
 */
public class RoleHelper {

    public static final int ROLE_OWNER = 300;
    public static final int ROLE_ADMIN = 200;
    public static final int ROLE_MODERATOR = 100;
    public static final int ROLE_DEFAULT = 0;

    public static int getRoleName(int role) {
        switch (role) {
            case ROLE_OWNER:
                return R.string.role_owner;
            case ROLE_ADMIN:
                return R.string.role_admin;
            case ROLE_MODERATOR:
                return R.string.role_moderator;
            default:
                return R.string.role_default;
        }
    }

}
