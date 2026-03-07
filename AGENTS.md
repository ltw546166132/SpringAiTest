# AGENTS.md

## Build, Lint, and Test Commands

### Basic Maven Commands

```bash
# Build the project (skip tests by default)
mvn clean package

# Install to local repository
mvn clean install

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=DemoUnitTest

# Run a specific test method
mvn test -Dtest=DemoUnitTest#testTest

# Run tests with specific profile
mvn test -Pdev
mvn test -Plocal
mvn test -Pprod
```

### Application Management

```bash
# Application startup script
./script/bin/ry.sh start
./script/bin/ry.sh stop
./script/bin/ry.sh restart
./script/bin/ry.sh status
```

### Environment Profiles

- **local**: Local development profile
- **dev**: Development profile (default)
- **prod**: Production profile

## Code Style Guidelines

### Java Coding Style

**General Principles:**
- Follow Alibaba Java Coding Guidelines
- Use Java 17+ features where appropriate
- Maintain consistency with existing code patterns

**Package Structure:**
- Top-level packages: `org.dromara.{module}.{submodule}`
- Example: `org.dromara.system.service.impl`
- Package names use lowercase
- Use clear, descriptive package names

**Class Naming:**
- **Entities/Models**: PascalCase, e.g., `SysClient`, `User`
- **BO (Business Objects)**: lowerCamelCase with `Bo` suffix, e.g., `SysClientBo`
- **VO (View Objects)**: lowerCamelCase with `Vo` suffix, e.g., `SysClientVo`
- **Interfaces**: PascalCase, e.g., `ISysClientService`
- **Implementation**: PascalCase with `Impl` suffix, e.g., `SysClientServiceImpl`
- **Constants**: UPPER_CASE with underscores, e.g., `CacheNames.SYS_CLIENT`
- **Controllers**: PascalCase, e.g., `IndexController`
- **Services**: PascalCase with `Service` suffix, e.g., `SysClientService`

**Method Naming:**
- Query methods: `query...`, `select...`, `get...`
- Insert methods: `insert...`, `add...`, `save...`
- Update methods: `update...`, `modify...`, `edit...`
- Delete methods: `delete...`, `remove...`
- Boolean methods: `is...`, `has...`, `can...`
- Private helper methods: PascalCase (not starting with prefix)

**Variable Naming:**
- Local variables: camelCase, e.g., `userId`, `pageQuery`
- Final constants: UPPER_SNAKE_CASE
- Collection variables: plural nouns with camelCase, e.g., `users`, `clientList`

### Import Ordering

```java
// 1. Third-party imports
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

// 2. Application imports
import org.dromara.system.service.ISysClientService;
import org.dromara.system.domain.SysClient;

// 3. Standard imports (alphabetical within group)
import java.util.Collection;
import java.util.List;
```

### Annotations

**Common Annotations:**
- **Lombok**: `@RequiredArgsConstructor`, `@Slf4j`, `@Data`, `@Builder`
- **Spring**: `@Service`, `@Component`, `@RestController`, `@Autowired`, `@Value`
- **Validation**: `@NotNull`, `@NotBlank`, `@Size`, `@Email`
- **MyBatis-Plus**: `@TableName`, `@TableId`, `@TableField`
- **Testing**: `@SpringBootTest`, `@Test`, `@DisplayName`, `@Disabled`
- **Security**: `@SaIgnore`, `@SaCheckPermission`, `@SaCheckRole`

**Annotation Usage Patterns:**
- Use `@RequiredArgsConstructor` instead of manual constructor injection
- Use `@Slf4j` for logging (not manual logger initialization)
- Use `@Data` for DTOs and simple classes (not for service entities)
- Use `@Value` for configuration properties
- Group annotations by type (Spring, Lombok, Application, Standard)

### Code Organization

**Service Layer Pattern:**
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class SysClientServiceImpl implements ISysClientService {
    private final SysClientMapper baseMapper;

    // Public methods (interfaces)
    @Override
    public SysClientVo queryById(Long id) { ... }

    // Private helper methods
    private LambdaQueryWrapper<SysClient> buildQueryWrapper(SysClientBo bo) { ... }
}
```

**Controller Layer Pattern:**
```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/system/client")
public class SysClientController {
    private final ISysClientService clientService;

    @GetMapping("/{id}")
    public R<SysClientVo> getInfo(@PathVariable Long id) { ... }

    @PostMapping
    public R<Void> add(@Validated @RequestBody SysClientBo bo) { ... }
}
```

### Documentation

**Javadoc Style:**
- Class level: `/** Description **/` followed by `@author` and `@date`
- Method level: One-line description, then parameters and return types
- Use standard Javadoc tags: `@param`, `@return`, `@throws`

```java
/**
 * 客户端管理Service业务层处理
 *
 * @author Michelle.Chung
 * @date 2023-06-18
 */
@Override
public SysClientVo queryById(Long id) {
    // Implementation
}
```

### Formatting Rules

**EditorConfig Configuration:**
- Indent: 4 spaces (not tabs)
- Line endings: LF
- Encoding: UTF-8
- Trailing whitespace: remove
- Final new line: add (except for markdown files)

**File Length:**
- Keep files under 500 lines when possible
- Extract long methods into smaller, focused methods
- Group related code together

**Method Length:**
- Keep methods under 50 lines
- Complex logic should be broken into smaller helper methods

### Database Layer

**MyBatis-Plus Patterns:**
- Use `LambdaQueryWrapper` instead of string-based queries
- Use `selectVoPage` for paginated queries
- Use `selectVoList` for list queries
- Use `selectVoById` for single record queries
- Use `selectVoOne` for single record query with conditions
- Use `insert` for insert operations
- Use `updateById` for update operations
- Use `deleteByIds` for batch delete operations

```java
// Example query pattern
LambdaQueryWrapper<SysClient> lqw = buildQueryWrapper(bo);
Page<SysClientVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
return TableDataInfo.build(result);

// Example query builder pattern
private LambdaQueryWrapper<SysClient> buildQueryWrapper(SysClientBo bo) {
    LambdaQueryWrapper<SysClient> lqw = Wrappers.lambdaQuery();
    lqw.eq(StringUtils.isNotBlank(bo.getClientId()), SysClient::getClientId, bo.getClientId());
    lqw.eq(StringUtils.isNotBlank(bo.getClientKey()), SysClient::getClientKey, bo.getClientKey());
    lqw.orderByAsc(SysClient::getId);
    return lqw;
}
```

### Validation

**Validation Annotations:**
- Use `@NotNull` for non-null fields
- Use `@NotBlank` for strings that cannot be empty
- Use `@Size` for collection or string length validation
- Use `@Email` for email validation
- Use `@Pattern` for regex patterns
- Apply validation at controller input layer (BO/DTO classes)

```java
@Data
public class SysClientBo {
    @NotBlank(message = "Client key cannot be blank")
    private String clientKey;

    @Size(min = 6, max = 32, message = "Client secret length must be between 6 and 32")
    private String clientSecret;
}
```

### Logging

**Logging Patterns:**
- Use `@Slf4j` annotation
- Log at appropriate levels: `info`, `debug`, `warn`, `error`
- Log errors with exceptions: `log.error("Message", exception)`
- Log method entry/exit for complex operations
- Use parameterized logging for performance

```java
@Slf4j
public class SysClientServiceImpl implements ISysClientService {

    @Override
    public Boolean updateByBo(SysClientBo bo) {
        log.info("Updating client with id: {}", bo.getId());
        try {
            return baseMapper.updateById(convert(bo)) > 0;
        } catch (Exception e) {
            log.error("Failed to update client: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Error Handling

**Exception Handling:**
- Use Spring's `@ExceptionHandler` in controllers for exception handling
- Throw meaningful exceptions with appropriate messages
- Log errors before rethrowing
- Never catch exceptions without logging or handling them

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        log.error("Service exception: {}", e.getMessage());
        return R.fail(e.getMessage());
    }
}
```

### Testing

**Testing Patterns:**
- Use `@SpringBootTest` for integration tests
- Use `@DisplayName` for test descriptions
- Use `@Disabled` for temporarily disabled tests
- Use `@Test` for test methods
- Use test prefixes like `test...` for clarity

```java
@SpringBootTest
@DisplayName("单元测试案例")
public class DemoUnitTest {

    @Autowired
    private CaptchaProperties captchaProperties;

    @DisplayName("测试验证码配置")
    @Test
    public void testCaptchaProperties() {
        assertNotNull(captchaProperties);
    }
}
```

### Maven Configuration

**Module Structure:**
```
ruoyi-vue-plus (parent)
├── ruoyi-admin (web application entry point)
├── ruoyi-common (common utilities and core)
│   ├── ruoyi-common-core
│   ├── ruoyi-common-mybatis
│   ├── ruoyi-common-satoken
│   └── ...
├── ruoyi-modules (business modules)
│   ├── ruoyi-system
│   ├── ruoyi-job
│   ├── ruoyi-generator
│   ├── ruoyi-workflow
│   └── ruoyi-demo
└── ruoyi-extend (extensions)
    ├── ruoyi-monitor-admin
    └── ruoyi-snailjob-server
```

**Dependency Management:**
- Use dependency versions from parent pom
- Don't hardcode versions in child modules
- Use `<exclusions>` to remove transitive dependencies when necessary
- Configure `skipTests` in parent pom (set to `true` for releases)

## Common Pitfalls

1. **Don't use string-based MyBatis queries** - always use LambdaQueryWrapper
2. **Don't create manual constructors** - use Lombok @RequiredArgsConstructor
3. **Don't forget to add Javadoc** - it's used for API documentation
4. **Don't ignore validation** - always validate input at controller layer
5. **Don't catch and suppress exceptions** - log them or handle them properly
6. **Don't skip tests** - always run tests before committing
7. **Don't hardcode versions** - use dependency management

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis-Plus Documentation](https://baomidou.com/)
- [Sa-Token Documentation](https://sa-token.cc/)
- [Lombok Documentation](https://projectlombok.org/)
- [RuoYi-Vue-Plus Documentation](https://plus-doc.dromara.org)
