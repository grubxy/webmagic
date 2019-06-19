package us.codecraft.webmagic.downloader;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.utils.HttpConstant;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class OkHttpGenerator {

    private ConnectionPool connectionPool;

    public OkHttpGenerator() {

    }

    public OkHttpGenerator setPoolSize(int poolSize) {
        connectionPool = new ConnectionPool(poolSize,30, TimeUnit.SECONDS);
        return this;
    }

    private X509TrustManager getX509TrustManage() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());

        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        return trustManager;
    }

    private SSLSocketFactory getSSLSocketFactory () throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { getX509TrustManage() }, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return sslSocketFactory;
    }

    public OkHttpClient getClient(Site site) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        return new OkHttpClient.Builder()
                .sslSocketFactory(getSSLSocketFactory(), getX509TrustManage())
                .followRedirects(false) // 禁用重定向
                .callTimeout(site.getTimeOut(),TimeUnit.MILLISECONDS)   // 超时
                .cookieJar(new CookieJarImpl()) // 设置cookie
                .build();
    }

    public Request.Builder selectRequestMethod(us.codecraft.webmagic.Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            // default get
            return new Request.Builder().get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            return addFormParams(new Request.Builder().post(addFormParams(request)));
        }
    }

    private RequestBody addFormParams(us.codecraft.webmagic.Request request) {

    }
}
