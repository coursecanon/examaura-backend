package com.coursecanon.examaura;

import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "com.coursecanon.examaura")
@EnableJpaAuditing
public class ExmauraApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExmauraApplication.class, args);
	}

}
