package com.tomclaw.appsend.core;

public class Response<Result> {

    private int status;
    private Result result;
    private String description;

    public int getStatus() {
        return status;
    }

    public Result getResult() {
        return result;
    }

    public String getDescription() {
        return description;
    }
}
