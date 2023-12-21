package com.github.wp.plugins.crawl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wp.plugins.crawl.vo.WPPluginInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * 爬取https://plugins.svn.wordpress.org 相关仓库信息，并将数据写到本地
 */
@Component
public class WpPluginsSvnRepositoryCrawl implements CommandLineRunner {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @Value("${wp-plugins-path}")
    private String filePath;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public WpPluginsSvnRepositoryCrawl(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.baseUrl("https://api.wordpress.org/plugins/info/1.2")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(12 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        //
        File file = new File(filePath);
        //设置请求相关参数保证能够获取到完整数据
        Document doc = Jsoup.newSession().header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .url("https://plugins.svn.wordpress.org/").header("Connection", "keep-alive")
                //由于body内容大于4M，需要设置body size和超时时间
                .maxBodySize(12 * 1024 * 1024).timeout(1000 * 1000)
                .get();
        Elements elements = doc.select("li > a[href]");
        List<String> list = new ArrayList<>();
        for (Element element : elements) {
            String plugin = element.text().replace("/", "");
            if (!"123ezbiz-ecommerce-store-connector-a".equals(plugin)) {
                threadPoolTaskExecutor.submit(new CrawlPluginTask(plugin.toString(), webClient, objectMapper));
            }
            list.add(plugin);
        }
        if (!list.isEmpty()) {
            FileUtils.writeLines(file, list);
        }
        System.out.println(elements.get(0).text());
        System.out.println("crawl end ....");
    }
}

@Slf4j
class CrawlPluginTask implements Callable {

    private final String pluginName;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public CrawlPluginTask(String pluginName, WebClient webClient, ObjectMapper objectMapper) {
        this.pluginName = pluginName;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object call() throws Exception {
        System.out.println(Thread.currentThread().getName());
        String result = "";
        Random rand = new Random();
        try {
            TimeUnit.MILLISECONDS.sleep(rand.nextInt(20, 300));
            //调用https://api.wordpress.org/plugins/info/1.2/?action=plugin_information&slug={slug} 获取完整插件信息
            Mono<String> body = this.webClient.get().uri("/?action=plugin_information&slug={slug}", pluginName).
                    exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.OK)) {
                            return response.bodyToMono(String.class);
                        } else {
                            log.info("调用wordpress:{} 无法获取对应的插件信息，插件信息可能不存在", pluginName);
                            return Mono.just("NoData");
                        }
                    });

            result = body.block();
            if (!"NoData".equals(result)) {
                WPPluginInfoVo pluginInfoVo = objectMapper.readValue(result, WPPluginInfoVo.class);
                FileUtils.writeStringToFile(new File("./wp-plugins/" + pluginName + ".txt"), result, "UTF-8");
            }
        } catch (Exception e) {
            log.error("调用wordpress api接口失败：", e);
        }
        return result;
    }
}