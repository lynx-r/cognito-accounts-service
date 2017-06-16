package com.workingbit.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AccountServiceTestUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceTestUiApplication.class, args);
	}
}
