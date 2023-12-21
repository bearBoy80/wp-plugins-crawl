package com.github.wp.plugins.crawl.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.wp.plugins.crawl.Deserializer.CustomArrayToMapDeserializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WPPluginInfoVo {
    private String name;
    private String slug;
    private String version;
    private String author;
    @JsonAlias("author_profile")
    private String authorProfile;
    /**
     * 最低wp版本
     */
    private String requires;
    /**
     * test wp版本
     */
    private String tested;
    /**
     * PHP版本要求
     */
    @JsonAlias("requires_php")
    private String requiresPhp;
    /**
     * 依赖插件
     */
    @JsonAlias("requires_plugins")
    private List<String> requiresPlugins;
    /**
     * 兼容
     */
    private List<String> compatibility;
    /**
     * 插件评分
     */
    private int rating;
    /**
     * 1-5🌟对应的数量
     */
    private Rating ratings;
    /**
     * 评分数量
     */
    @JsonAlias("num_ratings")
    private int numRatings;
    /**
     * Issues resolved
     */
    @JsonAlias("support_threads")
    private int supportThreads;

    @JsonAlias("support_threads_resolved")
    private int supportThreadsResolved;
    /**
     * 下载量
     */
    private long downloaded;

    @JsonAlias("active_installs")
    private long activeInstalls;
    /**
     * 最新更新
     */
    @JsonAlias("last_updated")
    private String lastUpdated;
    private String added;
    private String homepage;
    @JsonAlias("sections")
    private Section section;

    @JsonAlias("download_link")
    private String downloadLink;
    @JsonDeserialize(using = CustomArrayToMapDeserializer.class)
    private Map<String, Screenshot> screenshots;
    @JsonDeserialize(using = CustomArrayToMapDeserializer.class)
    private Map<String, String> tags;
    @JsonDeserialize(using = CustomArrayToMapDeserializer.class)
    private Map<String, String> versions;
    @JsonAlias("donate_link")
    private String donateLink;
    private Map<String, Contributor> contributors;
    @JsonDeserialize(using = CustomArrayToMapDeserializer.class)
    private Map<String,String> banners;
}

@Data
class Screenshot {
    private String src;
    private String caption;
}
@Data
class Rating {
    @JsonAlias("1")
    private int oneStar;
    @JsonAlias("2")
    private int twoStar;
    @JsonAlias("3")
    private int threeStar;
    @JsonAlias("4")
    private int fourStar;
    @JsonAlias("5")
    private int fiveStar;

}

@Data
class Section {
    private String description;
    private String installation;
    private String faq;
    private String changelog;
    private String reviews;
    private String screenshots;
}
@Data
class Contributor{
    private String profile;
    private String avatar;
    @JsonAlias("display_name")
    private String displayName;
}