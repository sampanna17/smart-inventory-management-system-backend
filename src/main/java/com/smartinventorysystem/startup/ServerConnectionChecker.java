package com.smartinventorysystem.startup;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerConnectionChecker implements CommandLineRunner {

    @Value("${server.port}")
    private String port;

    @Override
    public void run(String @NonNull ... args) {

        log.info("==================================");
        log.info(" Spring Project Started");
        log.info(" URL: http://localhost:{}", port);
        log.info("==================================");
    }
}