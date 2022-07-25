import com.weather.project.Weather;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.Optional;

/**
 * Created by kk on 2022/7/24
 */
public class testWeather {

    //正常情况测试
    @Test
    public void testWeatherNormal() {
        Optional<BigDecimal> a = Optional.of(BigDecimal.valueOf(23.9));
        Weather weather = new Weather();
        Assert.assertEquals(a, weather.getTemperature("10119", "04", "01"));
    }
    //边界测试
    @Test
    public void testWeatherBorder() {
        Optional<BigDecimal> a = Optional.of(BigDecimal.valueOf(-1));
        Weather weather = new Weather();
        Assert.assertEquals(a, weather.getTemperature("1011a","04", "01"));
        Assert.assertEquals(a, weather.getTemperature("10119", "04", "10"));
        Assert.assertEquals(a, weather.getTemperature("101", "04", "01"));
        Assert.assertEquals(a, weather.getTemperature("10119", "14", "01"));
    }
    //异常测试，不太会
    @Test(expected = UnknownHostException.class)
    public void testWeatherException() {
        Weather weather = new Weather();
        Optional<BigDecimal> a = weather.getTemperature("10119", "04", "01");
    }
}
