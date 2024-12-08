package org.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        // Path to your JSON file
        File jsonFile = new File("realm-export.json");

        // Load the JSON file into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // Define the UUID pattern
        Pattern uuidPattern = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");

        // Search for UUIDs
        searchForUUIDs(rootNode, "", uuidPattern);
    }

    private static void searchForUUIDs(JsonNode node, String currentPath, Pattern uuidPattern) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);
                searchForUUIDs(childNode, currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName, uuidPattern);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                searchForUUIDs(node.get(i), currentPath + "[" + i + "]", uuidPattern);
            }
        } else if (node.isValueNode()) {
            String value = node.asText();
            Matcher matcher = uuidPattern.matcher(value);
            if (matcher.matches()) {
                System.out.println("Found UUID: " + value + " at path: " + currentPath);
            }
        }
    }

}
