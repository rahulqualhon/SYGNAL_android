package com.android.utils;

import org.json.JSONObject;

public interface ResponseHandle {
    void onSuccess(JSONObject object, String api);



    void onError(String api, JSONObject obj);

}
