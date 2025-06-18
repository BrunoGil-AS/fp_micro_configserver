package com.aspiresys.fp_micro_configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class FpMicroConfigserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(FpMicroConfigserverApplication.class, args);
	}

}
