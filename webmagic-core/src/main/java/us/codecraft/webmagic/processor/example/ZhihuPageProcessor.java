package us.codecraft.webmagic.processor.example;

import com.alibaba.fastjson.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.OkHttpDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.SimpleProxySelector;

import java.net.InetSocketAddress;
import java.net.Proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author code4crafter@gmail.com <br>
 * @since 0.6.0
 */
public class ZhihuPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {
        page.addTargetRequests(page.getHtml().links().regex("https://www\\.zhihu\\.com/question/\\d+/answer/\\d+.*").all());
        page.putField("title", page.getHtml().xpath("//h1[@class='QuestionHeader-title']/text()").toString());
        page.putField("question", page.getHtml().xpath("//div[@class='QuestionRichText']//tidyText()").toString());
        page.putField("answer", page.getHtml().xpath("//div[@class='QuestionAnswer-content']/tidyText()").toString());
        if (page.getResultItems().get("title")==null){
            //skip this page
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {

        // fiddler proxy
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8888");
        System.setProperty("https.proxyPort", "8888");


        // 获取proxy
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://proxylist.fatezero.org/proxy.list")
                .get().build();

        List<Proxy> proxyList = new ArrayList<>();
        try (Response response = client.newCall(request).execute()){
            if (response.isSuccessful()) {
                String line = null;
                while((line = response.body().source().readUtf8Line()) !=null) {
//                    Proxy proxy = new Proxy(
//                            JSON.parseObject(line).getString("host"),
//                            JSON.parseObject(line).getInteger("port")
//                    );
                    Proxy proxy = new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(JSON.parseObject(line).getString("host"),
                                    JSON.parseObject(line).getInteger("port"))
                    );
                    proxyList.add(proxy);
                }

            } else {
                System.out.println("下载proxy失败...");
            }
        } catch (IOException e) {
           System.out.println(e.getMessage());
        }

        OkHttpDownloader downloader = new OkHttpDownloader();
        downloader.setProxySelector(new SimpleProxySelector(proxyList));
        Spider.create(new ZhihuPageProcessor())
                .addUrl("https://www.zhihu.com/explore")
                .setDownloader(downloader)
                .run();
    }
}
