//
// DO NOT EDIT THIS FILE.
// Generated using AndroidAnnotations 4.8.0.
// 
// You can create a larger work that contains this file and distribute that work under terms of your choice.
//

package com.tomclaw.appsend;

public final class Appteka_
    extends Appteka
{
    private static Appteka INSTANCE_;

    public static Appteka getInstance() {
        return INSTANCE_;
    }

    /**
     * Visible for testing purposes
     */
    public static void setForTesting(Appteka application) {
        INSTANCE_ = application;
    }

    @Override
    public void onCreate() {
        INSTANCE_ = this;
        init_();
        super.onCreate();
    }

    private void init_() {
    }
}
