package us.codecraft.webmagic.downloader;

import okhttp3.*;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.utils.HttpConstant;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
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

    public Request.Builder getRequest(Request.Builder builder,us.codecraft.webmagic.Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            // default get
            return builder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            return builder.post(
                    RequestBody.create(MediaType.get(request.getRequestBody().getContentType()),
                            request.getRequestBody().getBody())
            );
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return builder.head();
        }else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
//            return new Request.Builder().put();
        }else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return builder.delete();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    public Request.Builder setRequestHeaders(Request.Builder builder, us.codecraft.webmagic.Request request) {
        Headers.Builder headBuilder = new Headers.Builder();
        return builder.headers(getHeader(headBuilder, request).build());
    }

    private Headers.Builder getHeader(Headers.Builder builder, us.codecraft.webmagic.Request request) {
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header:request.getHeaders().entrySet()) {
                builder.add(header.getKey(), header.getValue());
            }
        }
        return builder;
    }

}
