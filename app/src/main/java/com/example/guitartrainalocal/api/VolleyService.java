package com.example.guitartrainalocal.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyService {

    private final IResult resultCallback;
    private final Context context;
    private final String api = "http://192.168.1.90:7161";

    public VolleyService(IResult resultCallback, Context context) {
        this.resultCallback = resultCallback;
        this.context = context;
    }
    public void deleteStringDataVolley(String url) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest jsonObj = new StringRequest(Request.Method.DELETE, url, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("DELETE", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("DELETE", error);
            });
            queue.add(jsonObj);

        } catch (Exception ignored) {

        }
    }
    public void putDataVolley(String url, JSONObject sendObj){
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.PUT, url, null, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("PUT", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("PUT", error);
            });
            queue.add(jsonObj);

        } catch (Exception ignored) {

        }
    }
    public void putStringDataVolley(String url){
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest jsonObj = new StringRequest(Request.Method.PUT, url, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("PUT", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("PUT", error);
            });
            queue.add(jsonObj);
        } catch (Exception ex) {
            Log.d("Error en el put", ex.toString());
        }
    }
    public void postStringDataVolley(String url) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest jsonObj = new StringRequest(Request.Method.POST, url, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("POST", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("POST", error);
            });
            queue.add(jsonObj);

        } catch (Exception ignored) {

        }
    }
    public void postDataVolley(String url, JSONObject sendObj) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.POST, url, sendObj, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("POST", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("POST", error);
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json;charset=utf-8");
                    return headers;
                }
            };
            queue.add(jsonObj);

        } catch (Exception ignored) {

        }
    }
    public void getStringDataVolley(String url) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest jsonObj = new StringRequest(Request.Method.GET, url, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("GET", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("GET", error);
            });
            queue.add(jsonObj);
        } catch (Exception ex) {
            Log.d("Error en el get", ex.toString());
        }
    }

    public void getJsonDataVolley(String url) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("GET", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("GET", error);
            });
            queue.add(jsonObj);
        } catch (Exception ex) {
            Log.d("Error en el get", ex.toString());
        }
    }
    public void getJsonArrayDataVolley(String url) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonArrayRequest jsonObj = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess("GET", response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError("GET", error);
            });
            queue.add(jsonObj);
        } catch (Exception ex) {
            Log.d("Error en el get", ex.toString());
        }
    }
    public void getStringWithIdDataVolley(String url, String requestID) {
        try {
            url = this.api + url;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest jsonObj = new StringRequest(Request.Method.GET, url, response -> {
                if (resultCallback != null)
                    resultCallback.notifySuccess(requestID, response);
            }, error -> {
                if (resultCallback != null)
                    resultCallback.notifyError(requestID, error);
            });
            queue.add(jsonObj);
        } catch (Exception ex) {
            Log.d("Error en el get", ex.toString());
        }
    }
} 