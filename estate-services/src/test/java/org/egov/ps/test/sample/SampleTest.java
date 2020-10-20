package org.egov.ps.test.sample;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.egov.ps.test.validator.ApplicationValidatorServiceTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SampleTest {

    @Test
    public void testSimpleJSONPath() {
        String simpleApplicationObjectJSON = ApplicationValidatorServiceTests
                .getFileContents("simpleApplicationObject.json");
        System.out.println(simpleApplicationObjectJSON);
        DocumentContext documentContext = JsonPath.parse(simpleApplicationObjectJSON);
        Map<String, Object> purchaser = documentContext.read("$.purchaser");
        System.out.println("purchaser: " + purchaser);
        String purchaserName = documentContext.read("$.purchaser.name");
        assertEquals("foo", purchaserName);

        String modeOfTransfer = documentContext.read("modeOfTransfer");
        System.out.println("modeOfTransfer: " + modeOfTransfer);
        assertEquals("GIFT", modeOfTransfer);
    }

    @Test
    public void testMapFiltering() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("apple", 5);
        map.put("ball", 4);
        map.put("cat", 3);
        map.put("dog", 3);
        map.put("elephant", 8);

        // Filter elements that have values greater than 4
        Map<String, Integer> filteredMap = map.entrySet().stream().filter(entry -> entry.getValue() > 4)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("Filtered map by using Map.Entry::getKey: " + filteredMap);

        filteredMap = map.entrySet().stream().filter(entry -> entry.getValue() > 4)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        System.out.println("Filtered map by using e -> e.getKey() " + filteredMap);

        filteredMap = map.entrySet().stream().filter(entry -> entry.getValue() > 4)
                .collect(Collectors.toMap(e -> e.getKey(), Map.Entry::getValue));
        System.out.println("Filtered map by using e -> e.getKey() and Entry::getValue " + filteredMap);

        filteredMap = map.entrySet().stream().filter(entry -> entry.getValue() > 4).collect(Collectors.toMap(e -> {
            return new StringBuilder(e.getKey()).reverse().toString();
        }, Map.Entry::getValue));
        System.out.println("Filtered map by using e -> e.getKey() and Entry::getValue " + filteredMap);
    }

    @Test
    public void testArrayJSONPath() {
        String simpleApplicationObjectJSON = ApplicationValidatorServiceTests.getFileContents("testJSONPath.json");
        System.out.println(simpleApplicationObjectJSON);
        DocumentContext documentContext = JsonPath.parse(simpleApplicationObjectJSON);
        List<String> ownerNames = documentContext.read("$.owners.*.name");
        assertEquals("Ramu", ownerNames.get(0));
        assertEquals("Shamu", ownerNames.get(1));
    }

    // @Test
    public void testIsArray() {
        String str = "Hello";
        String atr[][] = new String[10][20];
        System.out.println("Checking for str...");
        checkArray(str);
        System.out.println("Checking for atr...");
        checkArray(atr);
        System.out.println("Checking for empty string array...");
        checkArray(new String[] {});
    }

    private void checkArray(Object abc) {
        boolean x = abc.getClass().isArray();
        if (x == true) {
            Object[] arr = (Object[]) abc;
            System.out.println("The Object is an Array of size " + arr.length);
        } else {
            System.out.println("The Object is not an Array");
        }
    }

    @Test
    public void testPrefixExtraction() {
        String[] inputs = {"EstateBranch", "BuildingBranch", "ManimajraBranch", "OwnershipTransfer", "DuplicateCopy"};
        String[] outputs = {"EB", "BB", "MB", "OT", "DC" };
        for (int i = 0; i < inputs.length; i++) {
            assertEquals(outputs[i], extractPrefix(inputs[i]));
        }
    }

    private String extractPrefix(String inputString) {
        String outputString = "";

        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            outputString += Character.isUpperCase(c) ? c : "";
        }
        return outputString;
    }
}