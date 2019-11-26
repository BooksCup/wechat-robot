package com.bc.wechat.robot.configuration;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * @author zhou
 */
@Configuration
public class ElasticSearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Value("${es.clusterName}")
    String esClusterName;

    @Value("${es.host}")
    String esHost;

    @Bean
    public TransportClient client() {
        logger.info("===> clusterName:" + esClusterName + ", host:" + esHost);
        TransportClient client = null;
        try {
            Settings settings = Settings.builder().put("cluster.name",
                    esClusterName).build();
            client = new PreBuiltTransportClient(settings).addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(esHost), 9300));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
}
