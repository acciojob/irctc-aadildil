package com.driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
public class Irctc {

	public static void main(String[] args)throws java.lang.Exception {
		SpringApplication.run(Irctc.class, args);
	}

}
