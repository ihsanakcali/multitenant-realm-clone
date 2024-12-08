package org.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Main {

    private static Map<String, Integer> fieldCount = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Path to your JSON file
        File jsonFile = new File("realm-export.json");

        // Load the JSON file into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // Search for specific field names (id, containerId)
        searchForFieldNames(rootNode, "", new String[]{"id", "containerId", "_id"});

        // Print counts for field names
        printFieldCounts();
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
                        // If found, increment count and print the path
                        fieldCount.put(fieldName, fieldCount.getOrDefault(fieldName, 0) + 1);
                        System.out.println("Found field: " + fieldName + " at path: " + currentPath + "." + fieldName);
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

    private static void printFieldCounts() {
        System.out.println("\nField counts:");
        for (Map.Entry<String, Integer> entry : fieldCount.entrySet()) {
            System.out.println("Field name: " + entry.getKey() + " appears " + entry.getValue() + " times.");
        }
    }
}
