package com.example.springboot.service;

import com.example.springboot.dao.QuestionMapper;
import com.example.springboot.entity.Question;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 评分服务 - 从数据库查询正确答案进行评分
 */
@Service
public class GradingService {

    @Autowired
    private QuestionMapper questionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据用户答案和题目ID列表进行评分
     * @param userAnswersJson 用户答案JSON，格式: [{"questionId": 1, "userAnswer": "A", "score": 6}, ...]
     * @param totalScore 试卷总分
     * @return 评分结果JSON，包含得分和详情
     */
    public String gradeExam(String userAnswersJson, int totalScore) {
        try {
            JsonNode userAnswers = objectMapper.readTree(userAnswersJson);
            ArrayNode detailsArray = objectMapper.createArrayNode();
            int earnedScore = 0;

            for (JsonNode answer : userAnswers) {
                Long questionId = answer.get("questionId").asLong();
                String userAnswer = answer.has("userAnswer") ? answer.get("userAnswer").asText() : "";
                String userCode = answer.has("userCode") ? answer.get("userCode").asText() : "";
                // 使用前端传来的分数（动态调整后的分数）
                int qScore = answer.has("score") ? answer.get("score").asInt() : 0;

                // 从数据库查询题目（只获取正确答案，不使用数据库中的分数）
                Question question = questionMapper.selectById(questionId);
                if (question == null) {
                    continue;
                }

                ObjectNode detail = objectMapper.createObjectNode();
                detail.put("questionId", questionId);
                detail.put("questionTitle", question.getTitle());
                detail.put("questionType", question.getType());
                detail.put("questionScore", qScore);  // 使用动态分数
                detail.put("userAnswer", userAnswer);
                detail.put("earnedScore", 0);
                detail.put("isCorrect", false);

                // 根据题型评分
                switch (question.getType()) {
                    case 1: // 单选题
                        String correctSingle = question.getCorrectOption() != null ? 
                            question.getCorrectOption().trim().toUpperCase() : "";
                        String userSingle = userAnswer.trim().toUpperCase();
                        detail.put("correctAnswer", correctSingle);
                        if (!correctSingle.isEmpty() && correctSingle.equals(userSingle)) {
                            earnedScore += qScore;
                            detail.put("earnedScore", qScore);
                            detail.put("isCorrect", true);
                        }
                        // 添加选项信息
                        ObjectNode options = objectMapper.createObjectNode();
                        options.put("A", question.getOptionA());
                        options.put("B", question.getOptionB());
                        options.put("C", question.getOptionC());
                        options.put("D", question.getOptionD());
                        detail.set("options", options);
                        break;

                    case 2: // 多选题
                        String correctMulti = question.getCorrectOption() != null ? 
                            question.getCorrectOption().trim().toUpperCase() : "";
                        String userMulti = userAnswer.trim().toUpperCase();
                        detail.put("correctAnswer", correctMulti);
                        detail.put("userAnswer", userMulti); // 记录用户选择
                        // 排序后比较
                        char[] correctChars = correctMulti.toCharArray();
                        char[] userChars = userMulti.toCharArray();
                        Arrays.sort(correctChars);
                        Arrays.sort(userChars);
                        String sortedCorrect = new String(correctChars);
                        String sortedUser = new String(userChars);
                        if (!sortedUser.isEmpty() && sortedCorrect.equals(sortedUser)) {
                            earnedScore += qScore;
                            detail.put("earnedScore", qScore);
                            detail.put("isCorrect", true);
                        }
                        // 添加选项信息
                        ObjectNode multiOptions = objectMapper.createObjectNode();
                        multiOptions.put("A", question.getOptionA());
                        multiOptions.put("B", question.getOptionB());
                        multiOptions.put("C", question.getOptionC());
                        multiOptions.put("D", question.getOptionD());
                        detail.set("options", multiOptions);
                        break;

                    case 3: // 判断题
                        Integer correctJudge = question.getJudgeAnswer();
                        String userJudge = userAnswer.trim();
                        detail.put("correctAnswer", correctJudge != null && correctJudge == 1 ? "正确" : "错误");
                        detail.put("userAnswer", "1".equals(userJudge) ? "正确" : ("0".equals(userJudge) ? "错误" : "未作答"));
                        if (correctJudge != null && userJudge.equals(correctJudge.toString())) {
                            earnedScore += qScore;
                            detail.put("earnedScore", qScore);
                            detail.put("isCorrect", true);
                        }
                        break;

                    case 4: // 编程题 - 不自动评分，需要教师批改
                        String sampleInput = question.getSampleInput() != null ? question.getSampleInput() : "";
                        String sampleOutput = question.getSampleOutput() != null ? question.getSampleOutput().trim() : "";
                        detail.put("correctAnswer", "输入: " + sampleInput + "\n期望输出: " + sampleOutput);
                        detail.put("sampleInput", sampleInput);
                        detail.put("sampleOutput", sampleOutput);
                        detail.put("userAnswer", userCode);
                        detail.put("needsGrading", true);  // 标记需要教师批改
                        detail.put("earnedScore", 0);  // 初始分数为0，等待教师批改
                        detail.put("isCorrect", false);

                        // 运行代码获取输出，但不自动评分
                        if (!userCode.trim().isEmpty()) {
                            try {
                                String output = compileAndRunCode(userCode, sampleInput);
                                String userOutput = extractProgramOutput(output);
                                detail.put("userOutput", userOutput);
                            } catch (Exception e) {
                                detail.put("userOutput", "运行失败: " + e.getMessage());
                            }
                        }
                        break;
                }

                detailsArray.add(detail);
            }

            // 检查是否有编程题需要批改
            boolean hasProgramQuestion = false;
            for (JsonNode detail : detailsArray) {
                if (detail.has("needsGrading") && detail.get("needsGrading").asBoolean()) {
                    hasProgramQuestion = true;
                    break;
                }
            }

            // 构建返回结果
            ObjectNode result = objectMapper.createObjectNode();
            result.put("score", earnedScore);
            result.put("totalScore", totalScore);
            result.put("needsGrading", hasProgramQuestion);  // 是否需要教师批改
            result.set("answerDetails", detailsArray);

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"评分失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 从编译输出中提取程序输出
     */
    private String extractProgramOutput(String output) {
        String tag = "[程序输出]";
        int idx = output.indexOf(tag);
        if (idx != -1) {
            String rest = output.substring(idx + tag.length());
            int endErr = rest.indexOf("[错误输出]");
            int endExit = rest.indexOf("[退出码]");
            int end = rest.length();
            if (endErr != -1) end = Math.min(end, endErr);
            if (endExit != -1) end = Math.min(end, endExit);
            return rest.substring(0, end).trim();
        }
        return output.trim();
    }

    /**
     * 编译并运行C代码
     */
    private String compileAndRunCode(String code, String input) throws Exception {
        String dirName = "c_run_" + UUID.randomUUID().toString().replace("-", "");
        Path tempDir = Files.createTempDirectory(dirName);
        
        Path cFile = tempDir.resolve("main.c");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        Path exeFile = isWindows ? tempDir.resolve("main.exe") : tempDir.resolve("main");

        try {
            Files.write(cFile, code.getBytes(StandardCharsets.UTF_8));

            // 编译
            ProcessBuilder compilePb;
            if (isWindows) {
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
                return "[编译失败]\n" + compileOutput;
            }

            // 运行
            ProcessBuilder runPb = new ProcessBuilder(exeFile.toString());
            runPb.directory(tempDir.toFile());
            Process runProcess = runPb.start();

            try (OutputStream os = runProcess.getOutputStream()) {
                if (input != null && !input.isEmpty()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            boolean finished = runProcess.waitFor(5, TimeUnit.SECONDS);
            StringBuilder runOutput = new StringBuilder();

            if (!finished) {
                runProcess.destroyForcibly();
                return "[运行超时]";
            }

            try (InputStream is = runProcess.getInputStream()) {
                String out = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (!out.isEmpty()) {
                    runOutput.append("[程序输出]\n").append(out);
                }
            }

            return runOutput.toString();
        } finally {
            // 清理临时文件
            try {
                Files.walk(tempDir)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            } catch (IOException ignored) {}
        }
    }
}
