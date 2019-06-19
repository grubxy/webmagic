package us.codecraft.webmagic.downloader;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.List;

public class CookieJarImpl implements CookieJar {

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

    }

    @Override
    public  List<Cookie> loadForRequest(HttpUrl url) {
        return null;
    }
}
