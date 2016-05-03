package com.example.coolweather1.util;

public interface HttpCallbackListener {

	void onFinish(String response);

	void onError(Exception e);

}
