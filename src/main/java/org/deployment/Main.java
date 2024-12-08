package org.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main {

    private static Map<String, FieldInfo> fieldValues = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Path to your JSON file
        File jsonFile = new File("realm-export.json");

        // Load the JSON file into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // Step 1: Extract UUIDs and their relations
        extractFieldRelations(rootNode, "", new String[]{"id", "containerId", "_id"});

        // Print extracted relations
        printFieldRelations();

        // Step 2: Update UUIDs using extracted relations
        updateJsonWithTransformedUUIDs(rootNode);

        // Save the updated JSON to a new file
        File updatedFile = new File("realm-export-updated.json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(updatedFile, rootNode);


        System.out.println("Updated realm-export.json with transformed UUIDs.");
    }

    private static void extractFieldRelations(JsonNode node, String currentPath, String[] fieldNamesToFind) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);

                // Check if the current field name is one of the fields we're looking for
                for (String fieldNameToFind : fieldNamesToFind) {
                    if (fieldName.equals(fieldNameToFind) && childNode.isTextual()) {
                        String value = childNode.asText();
                        fieldValues.putIfAbsent(value, new FieldInfo(value));
                        fieldValues.get(value).addPath(currentPath + "." + fieldName);
                    }
                }

                // Continue to traverse the child node
                extractFieldRelations(childNode, currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName, fieldNamesToFind);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                extractFieldRelations(node.get(i), currentPath + "[" + i + "]", fieldNamesToFind);
            }
        }
    }

    private static void printFieldRelations() {
        System.out.println("\nExtracted field relations:");
        for (Map.Entry<String, FieldInfo> entry : fieldValues.entrySet()) {
            FieldInfo fieldInfo = entry.getValue();
            System.out.println("Value: " + fieldInfo.getValue());
            System.out.println("Paths: " + fieldInfo.getPaths());
            System.out.println("Occurrences: " + fieldInfo.getCount() + "\n");
        }
    }

    private static void updateJsonWithTransformedUUIDs(JsonNode node) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);

                if (childNode.isTextual()) {
                    String originalValue = childNode.asText();
                    if (fieldValues.containsKey(originalValue)) {
                        String transformedValue = transformUUID(originalValue);
                        ((ObjectNode) node).put(fieldName, transformedValue);
                        System.out.println("Updated field: " + fieldName + ", Original: " + originalValue + ", Transformed: " + transformedValue);
                    }
                }

                // Continue traversing the child node
                updateJsonWithTransformedUUIDs(childNode);
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                updateJsonWithTransformedUUIDs(arrayElement);
            }
        }
    }

    private static String transformUUID(String original) {
        // Check if the UUID is at least 6 characters long
        if (original.length() >= 6) {
            // Remove the last 6 characters and append "241224"
            return original.substring(0, original.length() - 6) + "241224";
        }
        // Return the original if it's too short to transform
        return original;
    }

    // Helper class to hold value, paths, and count
    private static class FieldInfo {
        private final String value;
        private final Map<String, Integer> paths = new HashMap<>();

        public FieldInfo(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCount() {
            return paths.size();
        }

        public void addPath(String path) {
            paths.put(path, paths.getOrDefault(path, 0) + 1);
        }

        public String getPaths() {
            return String.join(", ", paths.keySet());
        }
    }
}
