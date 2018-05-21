package com.anyhowhow.howhowweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.anyhowhow.howhowweather.gson.Forecast;
import com.anyhowhow.howhowweather.gson.Weather;
import com.anyhowhow.howhowweather.util.HttpUtil;
import com.anyhowhow.howhowweather.util.Utility;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private long exitTime = 0;
    private String mWeatherId;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView binPicImg;
    private ImageView weatherNow;
    public SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout forecastLayout;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //若Android在5.0以上，则启动全屏模式
        if (Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        binPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        weatherNow = (ImageView)findViewById(R.id.now_image);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.Swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        Button button = (Button)findViewById(R.id.change_city);
        //读取数据库中weather的信息
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        //若已有天气缓存，则直接显示，否则调用API查询天气
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if(weather != null){
                mWeatherId = weather.basic.weatherId;
                showWeatherInfo(weather);
            }

        }else {
             mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        //读取数据库中的必应图片，若读取成功则直接显示，否则重新加载
        String bingPic = preferences.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(binPicImg);
        } else {
            loadBingPic();
        }
        //下拉刷新之后，重新查询天气
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        //打开侧滑导航栏
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GaussianBlur();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //导航窗格中的菜单点击监听事件
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_change:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                editor.remove("weather");
                                editor.apply();
                                Intent intent = new Intent(WeatherActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        }).start();
                        break;
                    case R.id.nav_location:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Intent locationIntent = new Intent(WeatherActivity.this,LocationActivity.class);
                                startActivity(locationIntent);
                            }
                        }).start();
                        break;
                    case R.id.nav_author:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Intent locationIntent = new Intent(WeatherActivity.this,AuthorActivity.class);
                                startActivity(locationIntent);
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawers();
                finish();
                return true;
            }
        });

    }
    //再按一次退出程序的实现
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast toast = Toast.makeText(getApplicationContext(), "再按一次退出浩浩天气", Toast.LENGTH_SHORT);
                toast.show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //高斯模糊处理结果输出
    public void GaussianBlur(){
        View navHeader;
        ImageView navBackground;
        CircleImageView circleImageView;
        Bitmap userImg;
        Bitmap navBackImg;
        navHeader = navigationView.getHeaderView(0);
        navBackground = (ImageView)navHeader.findViewById(R.id.nav_background);
        circleImageView = (CircleImageView)navHeader.findViewById(R.id.icon_image);
        //新方案
        Drawable originDrawable = circleImageView.getDrawable();
        BitmapDrawable temImg = (BitmapDrawable)originDrawable;
        Bitmap bingBitmap = temImg.getBitmap();
        Bitmap GaussianBlurImg = blur(bingBitmap);
        navBackground.setImageBitmap(GaussianBlurImg);
        //旧方案
//        userImg = BitmapFactory.decodeResource(getResources(), R.drawable.nav_anyhow);
//        navBackImg = blur(userImg);
//        //Glide.with(WeatherActivity.this).load(navBackImg).into(navBackground);
//        navBackground.setImageBitmap(navBackImg);
    }
    //通过RenderScript实现高斯模糊
    public Bitmap blur(Bitmap image) {
        float BITMAP_SCALE = 0.2f;
        float BLUR_RADIUS = 25f;

        // 计算图片缩小后的长宽
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);
        // 将缩小后的图片做为预渲染的图片。
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片。
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(this);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(BLUR_RADIUS);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=c6fe695914d2428397569c25a7cf4b1c";
        //需要注意sendOkHttpRequest的用法，它是一个封装过的方法
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //将查询到的天气存储到数据库中，键值为weather
                        if (weather != null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//刷新事件结束
                    }

                });
            }
        });
        loadBingPic();
    }
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String countyName = weather.basic.countyName;
        String updateName = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(countyName);
        titleUpdateTime.setText(updateName);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        switch (weatherInfo){
            case "晴":
                weatherNow.setImageResource(R.drawable.sunny);
                break;
            case "多云":
                weatherNow.setImageResource(R.drawable.cloudy);
                break;
            case "晴间多云":
                weatherNow.setImageResource(R.drawable.cloudy);
                break;
            case "阴":
                weatherNow.setImageResource(R.drawable.overcast);
                break;
            case "小雨":
                weatherNow.setImageResource(R.drawable.rainy);
                break;
            case "中雨":
                weatherNow.setImageResource(R.drawable.rainy);
                break;
            case "阵雨":
                weatherNow.setImageResource(R.drawable.rainy);
                break;
            case "雷阵雨":
                weatherNow.setImageResource(R.drawable.thundershower);
                break;
            default:
                Toast.makeText(WeatherActivity.this,"未找到对应天气图标",Toast.LENGTH_SHORT).show();
                break;
        }
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            ImageView weatherForecast = (ImageView)view.findViewById(R.id.forecast_image);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView wavyLine = (TextView)view.findViewById(R.id.wavy_line);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            String forecastInfo = forecast.more.info;
            String maxTemperature = forecast.temperature.max +"℃";
            String minTemperature = forecast.temperature.min +"℃";
            infoText.setText(forecastInfo);
            switch (forecastInfo){
                case "晴":
                    weatherForecast.setImageResource(R.drawable.sunny_s);
                    break;
                case "多云":
                    weatherForecast.setImageResource(R.drawable.cloudy_s);
                    break;
                case "晴间多云":
                    weatherForecast.setImageResource(R.drawable.cloudy_s);
                    break;
                case "阴":
                    weatherForecast.setImageResource(R.drawable.overcast_s);
                    break;
                case "小雨":
                    weatherForecast.setImageResource(R.drawable.rainy_s);
                    break;
                case "中雨":
                    weatherForecast.setImageResource(R.drawable.rainy_s);
                    break;
                case "阵雨":
                    weatherForecast.setImageResource(R.drawable.rainy_s);
                    break;
                case "雷阵雨":
                    weatherForecast.setImageResource(R.drawable.thundershower_s);
                    break;
                default:
                    Toast.makeText(WeatherActivity.this,"未找到对应天气图标",Toast.LENGTH_SHORT).show();
                    break;
            }
            maxText.setText(maxTemperature);
            minText.setText(minTemperature);
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(binPicImg);
                    }
                });
            }
        });
    }
}
