package com.example.tennisclub.initialization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "data-initialization")
public class DataInitializerProperties {
    private boolean initData;
}
