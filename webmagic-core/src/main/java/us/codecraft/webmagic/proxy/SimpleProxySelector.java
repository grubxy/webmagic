package us.codecraft.webmagic.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class SimpleProxySelector extends ProxySelector {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<Proxy> proxyList;

    SimpleProxySelector(List<Proxy> proxies) {
        this.proxyList = proxies;
    }

    @Override
    public List<Proxy> select(URI uri) {
        return proxyList;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        logger.warn("proxy connect fail..." + sa.toString());
    }
}
