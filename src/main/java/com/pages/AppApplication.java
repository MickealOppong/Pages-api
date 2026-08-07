package com.pages;

import com.pages.impl.MediaUtilImpl;
import com.pages.util.RsaKeyProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
@AllArgsConstructor
public class AppApplication {

	private MediaUtilImpl mediaUtilImpl;


	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(){
		return args -> {
			mediaUtilImpl.init();
		};
	}
}
