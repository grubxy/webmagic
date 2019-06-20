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

        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task):null;

        Page page = Page.fail();
        try {
            OkHttpClient client = getOkHttpClient(task.getSite());
            okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
            okhttp3.Request okRequest = okHttpGenerator.getRequest(requestBuilder.url(request.getUrl()), request).build();

            Response response = client.newCall(okRequest).execute();
            logger.info(response.toString());


        }  catch (Exception e) {
            logger.info("Exception..."+e.getMessage());
        }
        return null;
    }

    @Override
    public void setThread(int thread) {

    }
}
