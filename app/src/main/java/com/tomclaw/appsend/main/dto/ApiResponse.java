package com.tomclaw.appsend.main.dto;

public class ApiResponse<A> {

    private final int status;
    private final A result;

    public ApiResponse(int status, A result) {
        this.status = status;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public A getResult() {
        return result;
    }
}
