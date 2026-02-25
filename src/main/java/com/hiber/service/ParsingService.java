package com.hiber.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiber.model.RootDefinition;
import com.hiber.model.FieldDefinition;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ParsingService {

    private RootDefinition definition;
    private final Path yamlPath;

    final String RED = "\u001B[31m";
    final String GREEN = "\u001B[32m";
    final String YELLOW = "\u001B[33m";
    final String RESET = "\u001B[0m";

    public ParsingService(Path yamlPath) {
        this.yamlPath = yamlPath;

        try {
            this.definition = loadAndValidate();
        } catch (Exception e) {
            System.out.println(RED + "✖ YAML configuration error:" +  e.getMessage() + RESET);
            throw new RuntimeException("Invalid startup configuration", e);
        }
    }

    private RootDefinition loadAndValidate() throws Exception {

        try (InputStream inputStream = Files.newInputStream(yamlPath)) {

            Yaml yaml = new Yaml();
            RootDefinition newDefinition = yaml.loadAs(inputStream, RootDefinition.class);

            validateYamlStructure(newDefinition);

            return newDefinition;
        }
    }

    public boolean reload() {

        try {
            this.definition = loadAndValidate();
            System.out.println();
            System.out.println(GREEN + "✔ Parser configuration successfully reloaded." + RESET);
            return true;

        } catch (Exception e) {
            System.out.println();
            System.out.println(RED + "✖ YAML configuration error:" + e.getMessage() + RESET);
            System.out.println(YELLOW + "⚠ Defaulting to previous parser configuration." + RESET);
            System.out.print("Enter HEX message: ");
            return false;
        }
    }

    private void validateYamlStructure(RootDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException("YAML definition is empty");
        }

        if (definition.getSeq() == null || definition.getSeq().isEmpty()) {
            throw new IllegalArgumentException("seq section is missing or empty");
        }

        Set<String> ids = new HashSet<>();

        for (FieldDefinition field : definition.getSeq()) {

            if (field.getId() == null || field.getId().isBlank()) {
                throw new IllegalArgumentException("Field ID is missing");
            }

            if (!ids.add(field.getId())) {
                throw new IllegalArgumentException("Duplicate field with ID: " + field.getId());
            }

            if (field.getType() == null || field.getType().isBlank()) {
                throw new IllegalArgumentException("Type missing for field: " + field.getId());
            }

            validateType(field.getType(), field.getId());
        }
    }

    private void validateType(String type, String fieldId) {

        type = type.toLowerCase();

        if (!type.matches("[uif][1248]")) {
            throw new IllegalArgumentException(
                    "Invalid type '" + type + "' for field: " + fieldId
            );
        }

        char base = type.charAt(0);
        int size = Integer.parseInt(type.substring(1));

        switch (base) {
            case 'u' -> {
                if (!(size == 1 || size == 2 || size == 4)) {
                    throw new IllegalArgumentException("Unsupported unsigned size in " + fieldId);
                }
            }
            case 'i' -> {
                if (!(size == 1 || size == 2 || size == 4 || size == 8)) {
                    throw new IllegalArgumentException("Unsupported signed size in " + fieldId);
                }
            }
            case 'f' -> {
                if (!(size == 4 || size == 8)) {
                    throw new IllegalArgumentException("Unsupported float size in " + fieldId);
                }
            }
            default -> throw new IllegalArgumentException("Unknown type for field: " + fieldId);
        }
    }

    public Map<String, Object> parse(String hexInput) {

        byte[] bytes = HexFormat.of().parseHex(hexInput);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        if (definition.getMeta() != null && definition.getMeta().getEndian() != null) {

            if ("le".equalsIgnoreCase(definition.getMeta().getEndian())) {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            else if ("be".equalsIgnoreCase(definition.getMeta().getEndian())) {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }

        } else {
            buffer.order(ByteOrder.BIG_ENDIAN);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (FieldDefinition field : definition.getSeq()) {

            String type = field.getType().toLowerCase();
            char baseType = type.charAt(0);
            int size = Integer.parseInt(type.substring(1));

            Object value = switch (baseType) {
                case 'u' -> readUnsigned(buffer, size);
                case 'i' -> readSigned(buffer, size);
                case 'f' -> readFloat(buffer, size);
                default -> throw new IllegalArgumentException("Unsupported type: " + type);
            };

            if (field.getMultiplier() != null && value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                value = numValue * field.getMultiplier();
            }

            if (field.getOffset() != null && value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                value = numValue + field.getOffset();
            }

            result.put(field.getId(), value);
        }
        return result;
    }

    private Object readUnsigned(ByteBuffer buffer, int size) {

        return switch (size) {
            case 1 -> Byte.toUnsignedInt(buffer.get());
            case 2 -> Short.toUnsignedInt(buffer.getShort());
            case 4 -> Integer.toUnsignedLong(buffer.getInt());
            default -> throw new IllegalArgumentException("Unsupported unsigned size: " + size);
        };
    }

    private Object readSigned(ByteBuffer buffer, int size) {

        return switch (size) {
            case 1 -> buffer.get();
            case 2 -> buffer.getShort();
            case 4 -> buffer.getInt();
            case 8 -> buffer.getLong();
            default -> throw new IllegalArgumentException("Unsupported signed size: " + size);
        };
    }

    private Object readFloat(ByteBuffer buffer, int size) {

        return switch (size) {
            case 4 -> buffer.getFloat();
            case 8 -> buffer.getDouble();
            default -> throw new IllegalArgumentException("Unsupported float size: " + size);
        };
    }
}