package com.example.hrm.system;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
              // ← add this for auto absent marking
public class 		HrmSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(HrmSystemApplication.class, args);
	}
}
