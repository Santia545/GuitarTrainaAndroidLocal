package com.example.guitartraina.api;

import com.android.volley.VolleyError;

public interface IResult {
    void notifySuccess(String requestType, Object response);
    void notifyError(String requestType, VolleyError error);
}