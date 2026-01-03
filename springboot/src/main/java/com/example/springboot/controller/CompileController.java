package com.example.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api")
public class CompileController {

    @PostMapping("/compile-c")
    public ResponseEntity<String> compileAndRun(@RequestBody Map<String, String> body) {
        String code = body.getOrDefault("code", "");
        String input = body.getOrDefault("input", "");
        if (code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>("代码不能为空", HttpStatus.BAD_REQUEST);
        }

        // 生成临时目录
        String dirName = "c_run_" + UUID.randomUUID().toString().replace("-", "");
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(dirName);
        } catch (IOException e) {
            return new ResponseEntity<>("创建临时目录失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path cFile = tempDir.resolve("main.c");
        Path exeFile;
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            exeFile = tempDir.resolve("main.exe");
        } else {
            exeFile = tempDir.resolve("main");
        }
        Path outputFile = tempDir.resolve("output.txt");

        try {
            Files.write(cFile, code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ResponseEntity<>("写入源码文件失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 编译
        try {
            ProcessBuilder compilePb;
            if (isWindows) {
                // 使用用户提供的gcc绝对路径，避免PATH环境变量问题
                String gccPath = "D:\\Users\\l\\Downloads\\x86_64-8.1.0-release-win32-sjlj-rt_v6-rev0\\mingw64\\bin\\gcc.exe";
                compilePb = new ProcessBuilder(gccPath, cFile.toString(), "-o", exeFile.toString());
            } else {
                compilePb = new ProcessBuilder("gcc", cFile.toString(), "-o", exeFile.toString());
            }
            compilePb.directory(tempDir.toFile());
            compilePb.redirectErrorStream(true);
            Process compileProcess = compilePb.start();
            String compileOutput;
            try (InputStream is = compileProcess.getInputStream()) {
                compileOutput = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            int compileExit = compileProcess.waitFor();
            if (compileExit != 0) {
                String msg = "[编译失败]\n" + compileOutput;
                Files.write(outputFile, msg.getBytes(StandardCharsets.UTF_8));
                String text = Files.readString(outputFile, StandardCharsets.UTF_8);
                return new ResponseEntity<>(text, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("调用gcc失败，请确认服务器已安装gcc并配置PATH: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 运行
        try {
            ProcessBuilder runPb;
            if (isWindows) {
                runPb = new ProcessBuilder(exeFile.toString());
            } else {
                runPb = new ProcessBuilder(exeFile.toString());
            }
            runPb.directory(tempDir.toFile());
            Process runProcess = runPb.start();

            // 写入标准输入
            try (OutputStream os = runProcess.getOutputStream()) {
                if (input != null && !input.isEmpty()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            // 等待执行（带超时），防止死循环占满资源
            boolean finished = runProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            StringBuilder runOutput = new StringBuilder();

            if (!finished) {
                runProcess.destroyForcibly();
                String msg = "[运行超时]\n程序执行超过 5 秒，已被终止。";
                Files.write(outputFile, msg.getBytes(StandardCharsets.UTF_8));
                String text = Files.readString(outputFile, StandardCharsets.UTF_8);
                return new ResponseEntity<>(text, HttpStatus.OK);
            }

            // 读取标准输出和错误
            try (InputStream is = runProcess.getInputStream()) {
                String out = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!out.isEmpty()) {
                    runOutput.append("[程序输出]\n").append(out);
                }
            }
            try (InputStream es = runProcess.getErrorStream()) {
                String err = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                if (!err.isEmpty()) {
                    if (runOutput.length() > 0) {
                        runOutput.append("\n");
                    }
                    runOutput.append("[错误输出]\n").append(err);
                }
            }

            int runExit = runProcess.exitValue();
            if (runOutput.length() > 0) {
                runOutput.append("\n");
            }
            runOutput.append("[退出码] ").append(runExit);

            Files.write(outputFile, runOutput.toString().getBytes(StandardCharsets.UTF_8));
            String text = Files.readString(outputFile, StandardCharsets.UTF_8);
            return new ResponseEntity<>(text, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("运行程序失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 简单清理临时目录（可选）
            try {
                Files.walk(tempDir)
                        .sorted((p1, p2) -> p2.compareTo(p1))
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (IOException ignored) {} }
                        );
            } catch (IOException ignored) {}
        }
    }
}
