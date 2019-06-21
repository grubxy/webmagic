package us.codecraft.webmagic.downloader;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;

import java.io.IOException;
import java.net.ProxySelector;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class OkHttpDownloader extends AbstractDownloader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, OkHttpClient> clients = new HashMap<String, OkHttpClient>();

    private OkHttpGenerator okHttpGenerator = new OkHttpGenerator();

    public ProxyProvider proxyProvider;

    public ProxySelector proxySelector;

    private boolean responseHeader = true;

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    public void setProxySelector(ProxySelector proxySelector) {
        this.proxySelector = proxySelector;
    }

    private OkHttpClient getOkHttpClient(Site site) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (site == null) {
            return okHttpGenerator.getClient(site);
        }
        String domain = site.getDomain();
        OkHttpClient client = clients.get(domain);
        if (client == null) {
            synchronized (this) {
                client = clients.get(domain);
                if (client == null) {
                    client = okHttpGenerator.getClient(site);
                    clients.put(domain, client);
                }
            }
        }
        return client;
    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        long start = System.currentTimeMillis();
        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task):null;

        Page page = Page.fail();
        try {
            OkHttpClient client = getOkHttpClient(task.getSite());
            okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
            okhttp3.Request okRequest = okHttpGenerator.getRequest(requestBuilder.url(request.getUrl()), request).build();

            Response response = client.newCall(okRequest).execute();

            page = handleResponse(request, request.getCharset() != null ?request.getCharset():task.getSite().getCharset(), response);
            return page;
        }  catch (Exception e) {
            logger.warn("download page {} error", request.getUrl(), e);
            return page;
        } finally {
            long end = System.currentTimeMillis();
            logger.info("cost time:"+ String.valueOf(end-start) +"url"+request.getUrl());
        }
    }

    @Override
    public void setThread(int thread) {

    }

    protected Page handleResponse(Request request, String charset, Response response) throws IOException {
        byte[] bytes = response.body().bytes();
        String contentType = response.body().contentType() == null?"":response.body().contentType().type();
        Page page = new Page();
        page.setBytes(bytes);
        if (!request.isBinaryContent()) {
            if (charset == null) {
                charset = getHtmlCharset(contentType, bytes);
            }
            page.setCharset(charset);
            page.setRawText(new String(bytes, charset));
         }
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(response.code());
        page.setDownloadSuccess(true);
        if (responseHeader) {
            page.setHeaders(response.headers().toMultimap());
        }
        return page;
    }

    private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }
}
