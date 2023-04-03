package zerobase.bud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "zerobase.bud")
public class BudApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudApplication.class, args);
    }

}
