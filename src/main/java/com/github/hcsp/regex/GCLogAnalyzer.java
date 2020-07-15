package com.github.hcsp.regex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCLogAnalyzer {
    // 在本项目的根目录下有一个gc.log文件，是JVM的GC日志
    // 请从中提取GC活动的信息，每行提取出一个GCActivity对象
    //
    // 2019-08-21T07:48:17.401+0200: 2.924: [GC (Allocation Failure) [PSYoungGen:
    // 393216K->6384K(458752K)] 416282K->29459K(1507328K), 0.0051622 secs] [Times: user=0.02
    // sys=0.00, real=0.01 secs]
    // 例如，对于上面这行GC日志，
    // [PSYoungGen: 393216K->6384K(458752K)] 代表JVM的年轻代总内存为458752，经过GC后已用内存从393216下降到了6384
    // 416282K->29459K(1507328K) 代表JVM总堆内存1507328，经过GC后已用内存从416282下降到了29459
    // user=0.02 sys=0.00, real=0.01 分别代表用户态消耗的时间、系统调用消耗的时间和物理世界真实流逝的时间
    // 请将这些信息解析成一个GCActivity类的实例
    // 如果某行中不包含这些数据，请直接忽略该行
    public static List<GCActivity> parse(File gcLog) throws IOException {
        Pattern logPattern = Pattern.compile("(\\d+)K->(\\d+)K\\((\\d+)K\\)].+?]*(\\d+)K->(\\d+)K\\((\\d+)K\\),.+user=(\\d+\\.\\d+)\\ssys=(\\d+\\.\\d+),\\sreal=(\\d+\\.\\d+)");
        List<GCActivity> gcActivityList = new ArrayList<>();
        List<String> lines = Files.readAllLines(gcLog.toPath());
        for (String line : lines) {
            Matcher matcher = logPattern.matcher(line);
            if (matcher.find()) {
                String youngBefore = matcher.group(1);
                String youngAfter = matcher.group(2);
                String youngTotal = matcher.group(3);
                String heapBefore = matcher.group(4);
                String heapAfter = matcher.group(5);
                String heapTotal = matcher.group(6);
                String user = matcher.group(7);
                String sys = matcher.group(8);
                String real = matcher.group(9);
                gcActivityList.add(new GCActivity(Integer.parseInt(youngBefore),
                        Integer.parseInt(youngAfter),
                        Integer.parseInt(youngTotal),
                        Integer.parseInt(heapBefore),
                        Integer.parseInt(heapAfter),
                        Integer.parseInt(heapTotal),
                        Double.parseDouble(user),
                        Double.parseDouble(sys),
                        Double.parseDouble(real)
                ));
            }
        }
        return gcActivityList;
    }

    public static void main(String[] args) throws IOException {
        List<GCActivity> activities = parse(new File("gc.log"));
        activities.forEach(System.out::println);
    }

    public static class GCActivity {
        // 年轻代GC前内存占用，单位K
        int youngGenBefore;
        // 年轻代GC后内存占用，单位K
        int youngGenAfter;
        // 年轻代总内存，单位K
        int youngGenTotal;
        // JVM堆GC前内存占用，单位K
        int heapBefore;
        // JVM堆GC后内存占用，单位K
        int heapAfter;
        // JVM堆总内存，单位K
        int heapTotal;
        // 用户态时间
        double user;
        // 系统调用消耗时间
        double sys;
        // 物理世界流逝的时间
        double real;

        public GCActivity(
                int youngGenBefore,
                int youngGenAfter,
                int youngGenTotal,
                int heapBefore,
                int heapAfter,
                int heapTotal,
                double user,
                double sys,
                double real) {
            this.youngGenBefore = youngGenBefore;
            this.youngGenAfter = youngGenAfter;
            this.youngGenTotal = youngGenTotal;
            this.heapBefore = heapBefore;
            this.heapAfter = heapAfter;
            this.heapTotal = heapTotal;
            this.user = user;
            this.sys = sys;
            this.real = real;
        }

        @Override
        public String toString() {
            return "GCActivity{"
                    + "youngGenBefore="
                    + youngGenBefore
                    + ", youngGenAfter="
                    + youngGenAfter
                    + ", youngGenTotal="
                    + youngGenTotal
                    + ", heapBefore="
                    + heapBefore
                    + ", heapAfter="
                    + heapAfter
                    + ", heapTotal="
                    + heapTotal
                    + ", user="
                    + user
                    + ", sys="
                    + sys
                    + ", real="
                    + real
                    + '}';
        }

        public int getYoungGenBefore() {
            return youngGenBefore;
        }

        public int getYoungGenAfter() {
            return youngGenAfter;
        }

        public int getYoungGenTotal() {
            return youngGenTotal;
        }

        public int getHeapBefore() {
            return heapBefore;
        }

        public int getHeapAfter() {
            return heapAfter;
        }

        public int getHeapTotal() {
            return heapTotal;
        }

        public double getUser() {
            return user;
        }

        public double getSys() {
            return sys;
        }

        public double getReal() {
            return real;
        }
    }
}
