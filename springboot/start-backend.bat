@echo off
echo 正在启动Spring Boot后端服务...
echo.

REM 检查Java是否安装
java -version
if %errorlevel% neq 0 (
    echo 错误：未找到Java，请确保Java已安装并在PATH中
    pause
    exit /b 1
)

echo.
echo 正在启动应用...
echo 请等待服务启动完成...
echo.

REM 设置类路径并启动应用
java -cp "target/classes;%USERPROFILE%/.m2/repository/org/springframework/boot/spring-boot-starter-web/3.4.12/spring-boot-starter-web-3.4.12.jar;%USERPROFILE%/.m2/repository/org/springframework/boot/spring-boot-starter/3.4.12/spring-boot-starter-3.4.12.jar;%USERPROFILE%/.m2/repository/org/springframework/boot/spring-boot/3.4.12/spring-boot-3.4.12.jar;%USERPROFILE%/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/3.4.12/spring-boot-autoconfigure-3.4.12.jar;%USERPROFILE%/.m2/repository/org/springframework/spring-core/6.2.14/spring-core-6.2.14.jar;%USERPROFILE%/.m2/repository/org/springframework/spring-context/6.2.14/spring-context-6.2.14.jar;%USERPROFILE%/.m2/repository/org/springframework/spring-web/6.2.14/spring-web-6.2.14.jar;%USERPROFILE%/.m2/repository/org/springframework/spring-webmvc/6.2.14/spring-webmvc-6.2.14.jar;%USERPROFILE%/.m2/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar;%USERPROFILE%/.m2/repository/org/mybatis/spring/boot/mybatis-spring-boot-starter/3.0.5/mybatis-spring-boot-starter-3.0.5.jar;%USERPROFILE%/.m2/repository/org/mybatis/mybatis/3.5.16/mybatis-3.5.16.jar;%USERPROFILE%/.m2/repository/org/mybatis/mybatis-spring/3.0.4/mybatis-spring-3.0.4.jar" com.example.springboot.SpringbootApplication

pause