package com.zkunm.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class HttpUtils {

    private PoolingHttpClientConnectionManager cm;

    public HttpUtils() {
        this.cm = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        this.cm.setMaxTotal(100);
        // 设置每个主机的最大连接数
        this.cm.setDefaultMaxPerRoute(10);
    }

    public void doGetImage(String dirName, String fileName, String url) {
        //获取HttpClient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(this.cm).build();
        //创建httpGet请求对象，设置url地址
        HttpGet httpGet = new HttpGet(url);
        //设置请求信息
        httpGet.setConfig(this.getConfig());
        //获取图片的后缀
        String extName = url.substring(url.lastIndexOf("."));
        //创建图片名，重命名图片
        String picName = fileName + extName;
        String pwd = "images/" + dirName + "/" + picName;
        CloseableHttpResponse response = null;
        try {
            //使用HttpClient发起请求，获取响应
            response = httpClient.execute(httpGet);
            //解析响应，返回结果
            if (response.getStatusLine().getStatusCode() != 200) System.out.println(pwd + "下载失败！");
                //判断响应体Entity是否不为空
            else if (response.getEntity() != null)
                response.getEntity().writeTo(new FileOutputStream(new File(pwd)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭response
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(pwd + "下载成功！");
    }

    //设置请求信息
    private RequestConfig getConfig() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)           //创建连接的最长时间
                .setConnectionRequestTimeout(3000)  // 获取连接的最长时间
                .setSocketTimeout(10000)            //数据传输的最长时间
                .build();
        return config;
    }
}
