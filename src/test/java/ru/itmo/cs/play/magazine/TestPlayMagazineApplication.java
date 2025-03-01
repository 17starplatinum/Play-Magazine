package ru.itmo.cs.play.magazine;

import org.springframework.boot.SpringApplication;

public class TestPlayMagazineApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlayMagazineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
