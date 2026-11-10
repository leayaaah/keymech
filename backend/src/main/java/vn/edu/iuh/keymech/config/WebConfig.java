package vn.edu.iuh.keymech.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AppProperties props;
    public WebConfig(AppProperties props) { this.props = props; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(props.uploadDir()).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
