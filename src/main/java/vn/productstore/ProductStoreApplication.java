package vn.productstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ProductStoreApplication extends SpringBootServletInitializer {
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(new Class[] {ProductStoreApplication.class});
  }

  public static void main(String[] args) {
    SpringApplication.run(ProductStoreApplication.class, (String[]) args);
  }
}
