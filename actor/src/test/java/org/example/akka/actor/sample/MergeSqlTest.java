package org.example.akka.actor.sample;

import cn.hutool.core.io.FileUtil;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author wangkh
 */
public class MergeSqlTest {

    @Test
    public void testMerge() {
        String path = "C:\\Users\\HMI01\\Desktop\\调度系统\\azkaban\\sql";
        File root = new File(path);
        String sql = Arrays.stream(root.listFiles())
                .filter(file -> {
                    String name = file.getName();
                    return name.endsWith(".sql") && name.startsWith("create");
                }).map(file -> {
                    return FileUtil.readString(file, StandardCharsets.UTF_8);
                }).reduce("", (a, b) -> a + "\n" + b);
        System.out.println(sql);
        FileUtil.writeString(sql, new File("C:\\Users\\HMI01\\Desktop\\调度系统\\azkaban\\all.sql"), StandardCharsets.UTF_8);
    }
}
