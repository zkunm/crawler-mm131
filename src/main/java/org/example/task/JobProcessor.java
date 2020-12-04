package org.example.task;

import org.example.util.HttpUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.util.List;

@Component
public class JobProcessor implements PageProcessor {

    private String url = "http://www.mm131.vip/xinggan/index.html";

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        // 解析页面
        List<Selectable> list = html.css("dl.list-left > dd").nodes();
        if (list.size() == 0) {
            // 解析详情页面
            String baseUrl = page.getUrl().toString();
            // 文件夹名称
            String dirName = html.css("div.content > h5", "text").toString();
            if (!baseUrl.matches("_")) {
                // 首图
                // 获取总数
                List<String> num = html.css("div.content-page > a[class=page-en]", "text").all();
                int total = Integer.parseInt(num.get(num.size() - 1));
                // 拼接其他图的url
                String url = baseUrl.replace(".html", "_2.html");
                page.addTargetRequest(url);
                for (int i = 3; i < total + 1; i++) {
                    url = url.replace("_" + (i - 1) + ".html", "_" + i + ".html");
                    page.addTargetRequest(url);
                }
                // 创建文件夹
                new File("images/" + dirName).mkdirs();
            }
            String fileName = html.css("div.content-pic > a > img", "alt").get();
            String fileUrl = html.css("div.content-pic > a > img", "src").get();
            new HttpUtils().doGetImage(dirName, fileName, fileUrl);
        } else {
            for (Selectable selectable : list) {
                String link = selectable.css("a").links().toString();
                page.addTargetRequest(link);
            }
        }
    }

    private Site site = Site.me()
            .setCharset("utf-8")        //设置编码
            .setTimeOut(10 * 1000)      //设置超时时间
            .setRetrySleepTime(3000)    //设置重试的间隔时间
            .setRetryTimes(3);          //设置重试的次数

    @Override
    public Site getSite() {
        return site;
    }

    /**
     * initialDelay当任务启动后，等等多久执行方法
     * fixedDelay每个多久执行方法
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 60 * 60 * 1000)
    public void process() {
        Spider.create(new JobProcessor())
                .addUrl(url)
                .thread(10)
                .run();
    }
}
