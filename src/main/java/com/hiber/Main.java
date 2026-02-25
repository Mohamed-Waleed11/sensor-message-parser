package com.hiber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiber.service.ParsingService;

import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Path yamlPath = Paths.get("src/main/resources/file.yaml");
        ParsingService parser = new ParsingService(yamlPath);
        ObjectMapper mapper = new ObjectMapper();
        startYamlWatcher(parser, yamlPath);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter HEX message: ");
            String hex = scanner.nextLine().trim();

            if (hex.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                Map<String, Object> result = parser.parse(hex);
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                System.out.println(json);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void startYamlWatcher(ParsingService parser, Path yamlPath) throws Exception {

        WatchService watchService = FileSystems.getDefault().newWatchService();
        yamlPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        new Thread(() -> {

            long lastReload = 0;

            while (true) {
                try {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.equals(yamlPath.getFileName())) {

                            long now = System.currentTimeMillis();

                            if (now - lastReload < 500) {
                                continue;
                            }

                            lastReload = now;
                            Thread.sleep(150); //important
                            boolean success = parser.reload();

                            if (success) {
                                System.out.print("Enter HEX message: ");
                            }
                        }
                    }

                    key.reset();

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

        }).start();
    }}