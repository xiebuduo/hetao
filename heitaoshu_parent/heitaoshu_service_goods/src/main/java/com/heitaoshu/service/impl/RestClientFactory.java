package com.heitaoshu.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientFactory {
    public static RestHighLevelClient getRestHighLevelClient(String hostname,int port) {
        HttpHost httpHost = new HttpHost(hostname,port,"http");
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost).setMaxRetryTimeoutMillis(20*60*1000);
        return new RestHighLevelClient(restClientBuilder);
    }
}
