package zerobase.bud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "zerobase.bud")
public class ChatApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApiApplication.class, args);
    }

}
