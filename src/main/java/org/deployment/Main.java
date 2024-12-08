package org.deployment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Main {

    private static Map<String, FieldInfo> fieldValues = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Path to your JSON file
        File jsonFile = new File("realm-export.json");

        // Load the JSON file into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // Search for specific field names (id, containerId, _id)
        searchForFieldNames(rootNode, "", new String[]{"id", "containerId", "_id"});

        // Print grouped values with their counts
        printGroupedFieldValues();
    }

    private static void searchForFieldNames(JsonNode node, String currentPath, String[] fieldNamesToFind) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);

                // Check if the current field name is one of the fields we're looking for
                for (String fieldNameToFind : fieldNamesToFind) {
                    if (fieldName.equals(fieldNameToFind)) {
                        // If found, process the value
                        String value = childNode.asText();
                        fieldValues.putIfAbsent(value, new FieldInfo(value));
                        fieldValues.get(value).addPath(currentPath + "." + fieldName);
                    }
                }

                // Continue to traverse the child node
                searchForFieldNames(childNode, currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName, fieldNamesToFind);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                searchForFieldNames(node.get(i), currentPath + "[" + i + "]", fieldNamesToFind);
            }
        }
    }

    private static void printGroupedFieldValues() {
        System.out.println("\nGrouped field values:");
        for (Map.Entry<String, FieldInfo> entry : fieldValues.entrySet()) {
            FieldInfo fieldInfo = entry.getValue();
            System.out.println("Value: " + fieldInfo.getValue());
            System.out.println("Paths: " + fieldInfo.getPaths());
            System.out.println("Occurrences: " + fieldInfo.getCount() + "\n");
        }
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
