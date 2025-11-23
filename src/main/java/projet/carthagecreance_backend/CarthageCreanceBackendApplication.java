package projet.carthagecreance_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarthageCreanceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarthageCreanceBackendApplication.class, args);
    }

}
