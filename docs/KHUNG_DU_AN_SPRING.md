# Khung dự án Spring Boot – Web TMĐT bàn phím cơ (Nhóm 4)

Dựng theo Kế hoạch bản 3, `CODING_CONVENTIONS.md` và `01-schema.sql`.

**Giả định:** phiên bản Boot và Spring AI chưa được nhóm chốt (câu hỏi 5 của kế hoạch). Khung này dùng đề xuất của kế hoạch: Spring Boot 4.x, Spring AI 2.0.x, JDK 21. Số phiên bản đặt trong `pom.xml`; kiểm tra lại ở start.spring.io rồi sửa. Code Security dùng lambda DSL nên chạy được cả Security 6 và 7, trừ một chỗ ghi chú ở `CartAccessManager`.

---

## Bước 1. Cây thư mục

```
keyboard-shop/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/vn/edu/iuh/keyboardshop/
│       │   ├── KeyboardShopApplication.java
│       │   ├── config/        SecurityConfig, WebConfig, AppProperties
│       │   ├── controller/    AuthController, ProductController, CartController, ...
│       │   ├── service/       AuthService, CartService, OrderService, ...
│       │   ├── repository/    UserRepository, ProductRepository, ...
│       │   ├── model/         User, Product, ... + enums/
│       │   ├── dto/           record *Request / *Response
│       │   ├── security/      AppUserDetails, AppUserDetailsService, CartAccessManager
│       │   ├── exception/     BusinessException, ResourceNotFoundException, GlobalExceptionHandler
│       │   ├── util/          SessionKeys, CodeGenerator
│       │   ├── ai/            ChatbotService, AiChatbotService, RuleBasedChatbotService, ProductIndexer
│       │   └── tracking/      ActivityTracker, ProfileService
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   ├── application-test.yml
│       │   ├── chatbot/{system-prompt.txt, chatbot-intents.json, knowledge/*.md}
│       │   └── templates/mail/{welcome,order,reset-code}.html
│       └── test/java/vn/edu/iuh/keyboardshop/
├── frontend/            (React + Vite, làm sau)
├── database/{01-schema.sql, 02-data.sql, dev-accounts.sql}
├── uploads/
├── docs/
├── docker-compose.yml
├── .env.example
├── .editorconfig
├── .gitignore
└── README.md
```

Tạo nhanh bằng Git Bash (hoặc WSL):

```bash
mkdir keyboard-shop && cd keyboard-shop
git init
B=backend/src/main/java/vn/edu/iuh/keyboardshop
mkdir -p $B/{config,controller,service,repository,model/enums,dto,security,exception,util,ai,tracking}
mkdir -p backend/src/main/resources/chatbot/knowledge backend/src/main/resources/templates/mail
mkdir -p backend/src/test/java/vn/edu/iuh/keyboardshop
mkdir -p frontend database uploads docs
touch uploads/.gitkeep
# chép 01-schema.sql và 02-data.sql vào database/
```

Ghi chú:

- Quy ước nhóm cấm tạo package ngoài danh sách, nên enum để trong `model/enums` (thư mục con của `model`). Nếu nhóm không muốn, để thẳng trong `model`.
- Tên package gốc `vn.edu.iuh.keyboardshop` là tên tự đặt, đổi theo ý nhóm.

---

## Bước 2. `backend/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <!-- Chốt ở M1 sau spike: Boot 4.x đi với Spring AI 2.0.x; Boot 3.5.x đi với Spring AI 1.1.x -->
    <version>4.0.0</version>
    <relativePath/>
  </parent>

  <groupId>vn.edu.iuh</groupId>
  <artifactId>keyboard-shop</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>keyboard-shop</name>

  <properties>
    <java.version>21</java.version>
    <spring-ai.version>2.0.0</spring-ai.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-bom</artifactId>
        <version>${spring-ai.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Web, Data, Security, Validation -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>

    <!-- Email -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-thymeleaf</artifactId></dependency>

    <!-- CSDL -->
    <dependency>
      <groupId>org.mariadb.jdbc</groupId>
      <artifactId>mariadb-java-client</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Spring AI: Gemini chat + embedding + MariaDB Vector -->
    <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-starter-model-google-genai</artifactId></dependency>
    <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-starter-model-google-genai-embedding</artifactId></dependency>
    <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-starter-vector-store-mariadb</artifactId></dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <!-- Test -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>mariadb</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <excludes>
            <exclude><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

Nếu Boot 4 báo thiếu class khi build, đối chiếu lại tên starter theo tài liệu của phiên bản bạn dùng (kế hoạch cũng dặn làm việc này ở Sprint 1).

Sau đó chạy `mvn -N wrapper:wrapper` trong `backend/` để có `./mvnw` như quy ước.

---

## Bước 3. Cấu hình

### `backend/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: keyboard-shop
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate        # schema chỉ sửa ở database/01-schema.sql
    properties:
      hibernate.jdbc.time_zone: Asia/Ho_Chi_Minh

  servlet:
    multipart:
      max-file-size: 2MB
      max-request-size: 3MB

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_APP_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY:}
        chat:
          options: { model: gemini-2.5-flash, temperature: 0.2 }
        embedding:
          text:
            options: { model: gemini-embedding-001, dimensions: 768 }
    vectorstore:
      mariadb:
        initialize-schema: false
        distance-type: COSINE
        dimensions: 768

server:
  port: 8080
  servlet:
    session:
      timeout: 30m
      cookie:
        http-only: true
        same-site: lax

app:
  cors-origin: ${APP_CORS_ORIGIN:http://localhost:5173}
  upload-dir: ${APP_UPLOAD_DIR:./uploads}
  chatbot:
    mode: ${APP_CHATBOT_MODE:ai}   # ai | rule
    personalization: true
    timeout-seconds: 8
    rate-limit-per-minute: 10
    rate-limit-per-hour-global: 200
```

### `application-dev.yml`

```yaml
spring:
  mail:
    host: localhost       # MailHog
    port: 1025
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false
logging:
  level:
    vn.edu.iuh.keyboardshop: DEBUG
```

### `application-test.yml`

```yaml
app:
  chatbot:
    mode: rule            # test không gọi Gemini thật
spring:
  ai:
    google:
      genai:
        api-key: test-key
```

### `.env.example` (giá trị giả)

```
DB_URL=jdbc:mariadb://localhost:3306/keyboard_shop
DB_USERNAME=shop
DB_PASSWORD=change-me
MAIL_USERNAME=
MAIL_APP_PASSWORD=
GEMINI_API_KEY=
APP_CHATBOT_MODE=ai
APP_UPLOAD_DIR=./uploads
APP_CORS_ORIGIN=http://localhost:5173
```

### `docker-compose.yml` (ở gốc kho)

```yaml
services:
  db:
    image: mariadb:11.8
    environment:
      MARIADB_DATABASE: keyboard_shop
      MARIADB_USER: ${DB_USERNAME:-shop}
      MARIADB_PASSWORD: ${DB_PASSWORD:-change-me}
      MARIADB_ROOT_PASSWORD: root-change-me
    ports: ["3306:3306"]
    volumes:
      - dbdata:/var/lib/mysql
      - ./database/01-schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro
      - ./database/02-data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro
  mailhog:
    image: mailhog/mailhog
    ports: ["1025:1025", "8025:8025"]
volumes:
  dbdata:
```

Chỉ mount hai file SQL, không mount cả thư mục `database/`, để `dev-accounts.sql` không bị nạp tự động.

### `.gitignore`

```
.env
application-local.yml
node_modules/
target/
dist/
*.log
.idea/
.vscode/
uploads/*
!uploads/.gitkeep
```

Ảnh mẫu của seed vẫn phải vào Git: chép ảnh mẫu vào `uploads/` rồi thêm bằng `git add -f`, hoặc đổi quy tắc ignore thành chỉ chặn ảnh upload mới (ví dụ `uploads/uploaded-*`). Nên hỏi nhóm chọn cách nào.

### `.editorconfig`

```
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true

[*.java]
indent_style = space
indent_size = 4

[*.{js,jsx,json,yml,yaml,sql}]
indent_style = space
indent_size = 2
```

---

## Bước 4. Lớp nền

### `KeyboardShopApplication.java`

```java
package vn.edu.iuh.keyboardshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class KeyboardShopApplication {
  public static void main(String[] args) {
    SpringApplication.run(KeyboardShopApplication.class, args);
  }
}
```

### `config/AppProperties.java`

```java
package vn.edu.iuh.keyboardshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String corsOrigin, String uploadDir, Chatbot chatbot) {
  public record Chatbot(String mode, boolean personalization, int timeoutSeconds,
                        int rateLimitPerMinute, int rateLimitPerHourGlobal) {}
}
```

### Enum (`model/enums/`, mỗi enum một file, ánh xạ bằng `EnumType.STRING`)

```java
public enum Role { CUSTOMER, STAFF, ADMIN }
public enum VisibilityStatus { ACTIVE, HIDDEN }              // products, categories
public enum OrderStatus { NEW, CONFIRMED, SHIPPING, COMPLETED, CANCELLED }
public enum PaymentMethod { COD, BANK_TRANSFER }
public enum PaymentStatus { UNPAID, PAID }
public enum CustomStatus { PENDING, QUOTED, ACCEPTED, IN_PROGRESS, COMPLETED, REJECTED, CANCELLED }
public enum EventType { VIEW_PRODUCT, SEARCH, ADD_TO_CART, PURCHASE }
```

### Entity mẫu `model/User.java`

```java
package vn.edu.iuh.keyboardshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.keyboardshop.model.enums.Role;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(length = 15)
  private String phone;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();   // schema không có DEFAULT thời gian
  }
}
```

Sáu entity còn lại làm theo đúng mẫu này, bám `01-schema.sql`:

| Entity | Ghi chú |
| --- | --- |
| `Category` | `status` kiểu `VisibilityStatus` |
| `Product` | `@ManyToOne(fetch = LAZY) Category category`; `price` là `BigDecimal`; `stockQuantity` là `int`; `@DecimalMin("1")` cho giá, `@Min(0)` cho tồn kho |
| `Order` | `@OneToMany(mappedBy = "order") List<OrderItem> items`; `orderCode` UNIQUE |
| `OrderItem` | `unitPrice` là giá chốt lúc mua |
| `CustomRequest` | có `updatedAt`, cập nhật bằng `@PrePersist` và `@PreUpdate` |
| `CustomRequestHistory` | `oldStatus` cho phép null ở dòng khởi tạo |
| `UserActivity` | `visitorId`, `userId`, `productId`, `categoryId` dạng `Long`, không cần quan hệ |

Không tạo entity cho `vector_store`, vì Spring AI tự quản.

### `exception/`

```java
// BusinessException.java
public class BusinessException extends RuntimeException {
  private final String code;
  public BusinessException(String code, String message) { super(message); this.code = code; }
  public String getCode() { return code; }
}

// ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException() { super("Không tìm thấy dữ liệu"); }
  public ResourceNotFoundException(String message) { super(message); }
}

// RateLimitedException.java
public class RateLimitedException extends RuntimeException {
  public RateLimitedException() { super("Bạn gửi câu hỏi quá nhanh, vui lòng thử lại sau"); }
}

// ErrorResponse.java
public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
  public static ErrorResponse of(int status, String error, String message) {
    return new ErrorResponse(status, error, message, LocalDateTime.now());
  }
}
```

### `exception/GlobalExceptionHandler.java`

```java
package vn.edu.iuh.keyboardshop.exception;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private ResponseEntity<ErrorResponse> build(HttpStatus s, String error, String msg) {
    return ResponseEntity.status(s).body(ErrorResponse.of(s.value(), error, msg));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> business(BusinessException e) {
    return build(HttpStatus.CONFLICT, e.getCode(), e.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> denied(AccessDeniedException e) {
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này");
  }

  @ExceptionHandler(RateLimitedException.class)
  public ResponseEntity<ErrorResponse> rate(RateLimitedException e) {
    return build(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getDefaultMessage()).collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> other(Exception e) {
    log.error("Lỗi không mong đợi", e);   // không trả stack trace ra ngoài
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
  }
}
```

`BusinessException` mặc định trả 409 theo quy ước. Riêng `INVALID_CODE` của Quên mật khẩu, kế hoạch yêu cầu 400: khi làm G-06 thêm lớp `InvalidInputException` (400) hoặc cho `BusinessException` nhận thêm tham số status.

### `config/WebConfig.java` (phục vụ `/uploads/**` từ thư mục ngoài classpath)

```java
package vn.edu.iuh.keyboardshop.config;

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
```

### `security/AppUserDetails.java`

```java
package vn.edu.iuh.keyboardshop.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.keyboardshop.model.User;

public class AppUserDetails implements UserDetails {
  private final Long id;
  private final String email;
  private final String password;
  private final String role;
  private final boolean enabled;

  public AppUserDetails(User u) {
    this.id = u.getId(); this.email = u.getEmail(); this.password = u.getPassword();
    this.role = u.getRole().name(); this.enabled = u.isEnabled();
  }
  public Long getId() { return id; }
  public String getRole() { return role; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }
  @Override public String getPassword() { return password; }
  @Override public String getUsername() { return email; }
  @Override public boolean isEnabled() { return enabled; }
}
```

### `security/AppUserDetailsService.java`

```java
package vn.edu.iuh.keyboardshop.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import vn.edu.iuh.keyboardshop.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository users;
  public AppUserDetailsService(UserRepository users) { this.users = users; }

  @Override
  public UserDetails loadUserByUsername(String email) {
    return users.findByEmail(email)
        .map(AppUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
  }
}
```

`isEnabled()` trả `false` thì Spring Security tự ném `DisabledException` khi `authenticate`; `AuthController` sau này gom mọi lỗi đăng nhập về một thông báo chung.

### `repository/UserRepository.java`

```java
package vn.edu.iuh.keyboardshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keyboardshop.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);

  // Quy ước: không xóa vật lý users
  @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
  @Override default void delete(User entity) { throw new UnsupportedOperationException(); }
}
```

Mọi repository nghiệp vụ (`users`, `products`, `categories`, `orders`, `custom_requests`) nên chặn `delete` theo mẫu này. Nếu lặp nhiều, tạo interface `@NoRepositoryBean NoDeleteRepository<T, ID>` rồi cho các repository kế thừa.

### `security/CartAccessManager.java` (giỏ hàng: Guest và Customer vào được, Staff và Admin bị 403)

```java
package vn.edu.iuh.keyboardshop.security;

import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class CartAccessManager implements AuthorizationManager<RequestAuthorizationContext> {

  @Override
  public AuthorizationDecision check(Supplier<Authentication> auth, RequestAuthorizationContext ctx) {
    Authentication a = auth.get();
    boolean blocked = a != null && a.getAuthorities().stream()
        .anyMatch(g -> g.getAuthority().equals("ROLE_STAFF") || g.getAuthority().equals("ROLE_ADMIN"));
    return new AuthorizationDecision(!blocked);
  }
}
```

Spring Security 7 (Boot 4) có thể đổi chữ ký `check` thành `authorize` (trả `AuthorizationResult`) hoặc đánh dấu `check` là deprecated. Nếu IDE báo lỗi ở đây, đó là lý do, và cũng là điều câu hỏi 5 cảnh báo. Khi đó đổi theo gợi ý của IDE; logic giữ nguyên.

### `config/SecurityConfig.java`

Cài đặt đúng bảng quy tắc ở mục 7 của kế hoạch. Chưa có `AuthController` thì chưa đăng nhập được.

```java
package vn.edu.iuh.keyboardshop.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.edu.iuh.keyboardshop.security.CartAccessManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
    return c.getAuthenticationManager();
  }

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(AppProperties props) {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of(props.corsOrigin()));
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/api/**", cfg);
    return src;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http,
                                  CartAccessManager cartAccess,
                                  SecurityContextRepository repo,
                                  CorsConfigurationSource cors) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)       // giai đoạn dev; quyết định bật lại ở Sprint 4
      .cors(c -> c.configurationSource(cors))
      .securityContext(sc -> sc.securityContextRepository(repo))
      .authorizeHttpRequests(a -> a
        // Matcher cụ thể đặt trước matcher chung
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
        .requestMatchers("/api/orders/**", "/api/custom-requests/**", "/api/users/me/**").hasRole("CUSTOMER")
        .requestMatchers("/api/cart/**").access(cartAccess)
        .requestMatchers("/api/auth/**", "/api/products/**", "/api/categories/**",
                         "/api/chat/**", "/uploads/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**").permitAll()
        .anyRequest().denyAll())
      .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) ->
            writeError(res, 401, "UNAUTHORIZED", "Vui lòng đăng nhập"))
        .accessDeniedHandler((req, res, ex) ->
            writeError(res, 403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này")))
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable);
    return http.build();
  }

  private static void writeError(HttpServletResponse res, int status, String error, String msg)
      throws IOException {
    res.setStatus(status);
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter().write(
        "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}"
            .formatted(status, error, msg, LocalDateTime.now()));
  }
}
```

Lưu ý:

- `/api/auth/register` chặn người đã đăng nhập sẽ kiểm ở `AuthController` (trả 403), không phải ở matcher.
- `/api/orders/**`, `/api/custom-requests/**`, `/api/users/me/**` yêu cầu `hasRole("CUSTOMER")`: Guest nhận 401, Staff và Admin nhận 403, đúng bảng quy tắc.
- `.anyRequest().denyAll()` để endpoint mới quên khai báo sẽ bị chặn thay vì mở. Thêm endpoint nào thì thêm matcher và test phân quyền cho cả 4 vai trò.

### `util/SessionKeys.java`

```java
package vn.edu.iuh.keyboardshop.util;

public final class SessionKeys {
  private SessionKeys() {}
  public static final String CART = "cart";
  public static final String VISITOR_ID = "visitorId";
  public static final String CHAT_HISTORY = "chatHistory";
  public static final String RESET_CODE = "resetCode";
}
```

---

## Bước 5. Chạy thử

```bash
docker compose up -d                          # MariaDB 11.8 + MailHog
cp .env.example .env                          # rồi điền DB_PASSWORD
cd backend
export $(grep -v '^#' ../.env | xargs)        # nạp biến môi trường (Git Bash)
./mvnw spring-boot:run
```

Kết quả mong đợi: ứng dụng khởi động được, `ddl-auto=validate` không báo lỗi bảng hoặc cột. Nếu có lỗi `Schema-validation`, đối chiếu tên cột trong entity với `01-schema.sql`.

Kiểm tra nhanh bằng trình duyệt:

| URL | Kết quả đúng |
| --- | --- |
| `http://localhost:8080/api/products` | 404 (chưa có controller, nhưng không bị 401) |
| `http://localhost:8080/api/staff/x` | 401 JSON tiếng Việt, chứng tỏ Security đang hoạt động |
| `http://localhost:8025` | Giao diện MailHog |

Vector store có thể báo lỗi khi khởi động nếu bảng `vector_store` chưa khớp với thư viện. Đây chính là bước spike mà `01-schema.sql` đã dặn: chạy một lần với `initialize-schema=true` trên CSDL trống, xem `SHOW CREATE TABLE vector_store`, rồi sửa khối DDL trong `01-schema.sql` cho khớp (và báo cả nhóm vì đây là thay đổi schema).

---

## Bước kế tiếp (theo thứ tự sprint của kế hoạch)

1. Hoàn thiện 6 entity còn lại và repository.
2. `AuthController` + `AuthService`: đăng nhập thủ công (`changeSessionId`, lưu `SecurityContext`, xóa giỏ của Staff/Admin), đăng ký, đăng xuất, `me`, quên mật khẩu, đổi mật khẩu.
3. `ProductController` + `CategoryController` (G-01, G-02) + `ActivityTracker`.
4. `CartController` + `OrderService.placeOrder` (UPDATE có điều kiện, việc phụ sau commit).
5. Custom, Staff, Admin, Chatbot (Spring AI).

Nên làm bước 2 trước vì mọi chức năng sau đều phụ thuộc đăng nhập và phân quyền.
