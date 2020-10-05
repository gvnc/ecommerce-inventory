package ecommerce.app.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.app.backend.service.SyncProductsService;
import ecommerce.app.backend.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "ecommerce.app.backend.repository")
public class Application implements CommandLineRunner {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SyncProductsService syncProductsService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	public void configureJackson(ObjectMapper objectMapper) {
		objectMapper.setTimeZone(Utils.getTimezone());
	}

	@Override
	public void run(String...args){
		log.info("Application started and ready.");
		syncProductsService.syncAllMarketPlaces();
	}
}
