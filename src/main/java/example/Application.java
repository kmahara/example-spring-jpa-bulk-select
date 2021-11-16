package example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class Application implements CommandLineRunner {

  @Autowired
  private TestService service;

  @Override
  public void run(String... args) throws Exception {
    service.init();
//    service.test1();
    service.test2();
  }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
