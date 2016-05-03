package com.example.coolweather1.activity;

import com.example.coolweather1.R;
import com.example.coolweather1.receiver.AutoUpdateReceiver;
import com.example.coolweather1.util.HttpCallbackListener;
import com.example.coolweather1.util.HttpUtil;
import com.example.coolweather1.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishTimeText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button swithCity;
	private Button refreshWeather;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		//初始化各种控件
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info);
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishTimeText = (TextView)findViewById(R.id.textview_publish_info);
		weatherDespText = (TextView)findViewById(R.id.textview_weather_desp);
		temp1Text = (TextView)findViewById(R.id.textview_temp1);
		temp2Text = (TextView)findViewById(R.id.textview_temp2);
		currentDateText = (TextView)findViewById(R.id.textview_current_time);
		swithCity = (Button)findViewById(R.id.button_home);
		refreshWeather = (Button)findViewById(R.id.button_refresh);
		
		swithCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			//
			publishTimeText.setText("同步中。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
			Log.d("WeatherActivity", "123");
		}else {
			showWeather();
			Log.d("WeatherActivity", "1234");
		}
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_home:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
			
		case R.id.button_refresh:
			publishTimeText.setText("同步中。。。");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = preferences.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	* 查询县级代号所对应的天气代号。
	*/
	private void queryWeatherCode(String countyCode){
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	* 查询天气代号所对应的天气。
	*/
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	/**
	* 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	*/
	private void queryFromServer(final String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if(!TextUtils.isEmpty(response)){
						//从服务器中返回的数据解析出天气代号
						String[] array = response.split("\\|");
						if(array != null && array.length == 2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}{

				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishTimeText.setText("同步失败");
					}
				});
			}
		});
	}
	
	private void showWeather() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(preferences.getString("city_name", ""));
		temp1Text.setText(preferences.getString("temp1", ""));
		temp2Text.setText(preferences.getString("temp2", ""));
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		publishTimeText.setText("今天" + preferences.getString("publish_time", "") + "发布");
		currentDateText.setText(preferences.getString("current_data", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateReceiver.class);
		startService(intent);
	}	
}
