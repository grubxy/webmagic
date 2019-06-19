package us.codecraft.webmagic.downloader;

import okhttp3.OkHttpClient;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class OkHttpDownloader extends AbstractDownloader {

    private final Map<String, OkHttpClient> clients = new HashMap<String, OkHttpClient>();

    private OkHttpGenerator okHttpGenerator = new OkHttpGenerator();

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

    }

    @Override
    public void setThread(int thread) {

    }
}
