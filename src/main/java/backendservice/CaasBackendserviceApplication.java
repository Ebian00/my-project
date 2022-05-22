package backendservice;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
public class CaasBackendserviceApplication {
	@Autowired
	  private ObjectMapper objectMapper;
	public static void main(String[] args) {
		SpringApplication.run(CaasBackendserviceApplication.class, args);
	}
	 @PostConstruct
	  public void setUp() {
	    objectMapper.registerModule(new JavaTimeModule());
	  }
	 @Bean
	  public FilterRegistrationBean processCorsFilter() {
	      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	      CorsConfiguration config = new CorsConfiguration();
	      config.setAllowCredentials(true);
	      config.addAllowedOrigin("*");
	      config.addAllowedHeader("*");
	      config.addAllowedMethod("*");
	      source.registerCorsConfiguration("/**", config);

	      FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
	      bean.setOrder(0);
	      return bean;
	  }
	
}
