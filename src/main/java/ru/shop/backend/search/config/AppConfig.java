package ru.shop.backend.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import  org.springframework.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableElasticsearchRepositories
@EnableScheduling
public class AppConfig implements WebMvcConfigurer {
    private String elasticUrl = "127.0.0.1:9200";
    @Bean
    public ClientConfiguration clientConfiguration(){
        return ClientConfiguration.builder().connectedTo(elasticUrl)
                .build();
    }
    @Bean
    @Autowired
    public RestHighLevelClient restHighLevelClient(ClientConfiguration client){
        return RestClients.create(client).rest();
    }

    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String version) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info()
                .title("Проект электронных очередей")
                .version(version));
        return openAPI;
    }


}
