package com.weather.project;

import jdk.nashorn.internal.parser.JSONParser;
import netscape.javascript.JSObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.alibaba.fastjson.*;


/**
 * Created by kk on 2022/7/22
 */
public class Weather {
    //初始时间
    private static long startTime = System.currentTimeMillis();
    //初始计数值
    private static final AtomicInteger ZERO = new AtomicInteger(0);
    //时间窗口
    private static final long interval = 1000;
    //限制请求数
    private static int limit = 100;
    //请求计数
    private static AtomicInteger requestCount = ZERO;
    //限流方法
    public  boolean tryAcquire() {
        //获取当前时间
        long now = System.currentTimeMillis();
        //在时间窗口内
        if (now < startTime + interval) {
            //判断是否超过最大请求
            if (requestCount.get() < limit) {
                requestCount.incrementAndGet();
                return true;
            }
            //
            return false;
        }else {
            //否则重置时间窗口
            startTime = now;
            requestCount = ZERO;
            return true;
        }
    }
    //获取温度
    public Optional<BigDecimal> getTemperature(String province, String city, String country) {
        //如果异常则返回-1
        Optional<BigDecimal> ans = Optional.of(BigDecimal.valueOf(-1));
        //检查每秒访问次数
        if (!tryAcquire()) {
            System.out.println(ans);
            return ans;
        }
        //重试次数
        final int MAX_RETRY = 10;
        //判输入合法
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNumProvince = pattern.matcher(province);
        Matcher isNumCity = pattern.matcher(city);
        Matcher isNumCountry = pattern.matcher(country);
        if ( !isNumProvince.matches() || !isNumCity.matches() || !isNumCountry.matches()) return ans;
        if ("".equals(province) || "".equals(city) || "".equals(country) || province.length() != 5 || city.length() != 2 || country.length() != 2)
            return ans;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://www.weather.com.cn/data/sk/"+province+city+country+".html");
        //循环计数
        int times = 0;
        //如果循环次数小于等于最大重试次数则循环重试
        while (times <= MAX_RETRY) {
            try {
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    //获取返回值并转换为String
                    String res = EntityUtils.toString(response.getEntity());
                    //若返回值长度过长则异常，返回-1
                    if (res.length() > 400) {
                        System.out.println(ans);
                        return ans;
                    } else {
                        //否则处理返回值
                        String result = new String(res.getBytes("ISO-8859-1"));
                        System.out.println(result);
                        //转换为JSON
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        String weatherinfo = jsonObject.getString("weatherinfo");
                        JSONObject infoObject = JSONObject.parseObject(weatherinfo);
                        //获取温度值并返回
                        BigDecimal temp = new BigDecimal(infoObject.getString("temp"));
                        ans = Optional.of(temp);
                        System.out.println(ans);
                        return ans;
                    }

                }
            } catch (Exception e) {
                //出现异常时将循环次数+1
                times++;
                //如果循环次数+1后大于最大重试次数则返回异常值
                if (times > MAX_RETRY){
                    e.printStackTrace();
                    System.out.println(ans);
                    return ans;
                }
            }
        }
        return ans;
    }

//    public static void main(String[] args) {
//        getTemperature("10119","04","01");
//    }
}
