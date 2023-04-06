package zerobase.bud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "zerobase.bud")
@EnableScheduling
public class BudApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudApplication.class, args);
    }

}
