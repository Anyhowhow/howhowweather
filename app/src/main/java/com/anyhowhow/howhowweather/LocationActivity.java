package com.anyhowhow.howhowweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private TextView position_text;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isfirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location_2);
        position_text = (TextView) findViewById(R.id.position);
        mapView = (MapView) findViewById(R.id.baidumap);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionlist = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionlist.isEmpty()) {
            String[] permissions = permissionlist.toArray(new String[permissionlist.size()]);
            ActivityCompat.requestPermissions(LocationActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
        Button locateBack_button;
        locateBack_button = (Button)findViewById(R.id.locateBack_button);
        locateBack_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocationActivity.this,WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
        private void requestLocation () {
            initLocation();
            mLocationClient.start();
        }
        private void initLocation () {
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(5000);
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);
        }
        @Override
        protected void onResume () {
            super.onResume();
            mapView.onResume();
        }
        @Override
        protected void onPause () {
            super.onPause();
            mapView.onPause();
        }
        @Override
        protected void onDestroy () {
            super.onDestroy();
            mLocationClient.stop();
            mapView.onDestroy();
            baiduMap.setMyLocationEnabled(false);
        }
        @Override
        public void onRequestPermissionsResult ( int requestCode, String[] permissions,
        int[] grantResults){
            switch (requestCode) {
                case 1:
                    if (grantResults.length > 0) {
                        for (int result : grantResults) {
                            if (result != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                        requestLocation();
                    } else {
                        Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                default:
            }
        }
        private void navigateTo (BDLocation location){
            StringBuilder currentPosition = new StringBuilder();
            //展开地图，地图中心点位置为当前定位位置
            if (isfirstLocate){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus mMapStatus = new MapStatus.Builder().target(ll).zoom(16).build();
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            baiduMap.setMapStatus(mMapStatusUpdate);
            isfirstLocate = false;
            }
            //在地图上显示我的位置
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(location.getLatitude());
            locationBuilder.longitude(location.getLongitude());
            MyLocationData locationData = locationBuilder.build();
            baiduMap.setMyLocationData(locationData);
            //显示当前位置
            currentPosition.append(location.getProvince());
            currentPosition.append(location.getCity());
            currentPosition.append(location.getDistrict());
            currentPosition.append(location.getStreet());
            position_text.setText(currentPosition);
        }
        public class MyLocationListener implements BDLocationListener {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    navigateTo(location);
                }
            }
        }
    }