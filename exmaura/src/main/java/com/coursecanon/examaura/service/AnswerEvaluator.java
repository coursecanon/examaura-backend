package com.coursecanon.examaura.service;


//Handling polymorphic JSON grading is one of the trickiest parts of building a certification simulator. The primary challenge is that standard JSON equality checks are too strict: if a user selects ["B", "A"] for a multiple-choice question, but the database stores ["A", "B"], a basic string comparison will mark it wrong, even though it is logically correct.
//
//To fix this, we need a dedicated evaluation component that understands the rules of each specific QuestionType.
//
//This component isolates the Jackson JsonNode traversal logic so your main service remains clean.

import com.coursecanon.examaura.entity.enums.QuestionType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class AnswerEvaluator {

    /**
     * Routes the evaluation based on the question type.
     */
    public boolean evaluate(QuestionType type, JsonNode correctAnswer, JsonNode userAnswer) {
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }

        return switch (type) {
            case OBJECTIVE -> evaluateObjective(correctAnswer, userAnswer);
            case MULTIPLE_CHOICE -> evaluateArrayUnordered(correctAnswer, userAnswer);
            case DRAG_CLASSIFY -> evaluateDragClassify(correctAnswer, userAnswer);
            case YES_NO_GRID, MATCHING_DROPDOWN -> evaluateExactMatch(correctAnswer, userAnswer);
            default -> false; // Failsafe for unmapped types
        };
    }

    /**
     * For single-choice questions. Checks exact text value.
     * Example: "Option B" == "Option B"
     */
    private boolean evaluateObjective(JsonNode correct, JsonNode user) {
        return correct.asText().trim().equalsIgnoreCase(user.asText().trim());
    }

    /**
     * For multiple-choice questions.
     * Compares two JSON Arrays but ignores the order of the elements inside them.
     * Example: ["A", "B"] equals ["B", "A"]
     */
    private boolean evaluateArrayUnordered(JsonNode correct, JsonNode user) {
        if (!correct.isArray() || !user.isArray()) return false;
        if (correct.size() != user.size()) return false;

        Set<String> correctSet = new HashSet<>();
        correct.forEach(node -> correctSet.add(node.asText().trim()));

        Set<String> userSet = new HashSet<>();
        user.forEach(node -> userSet.add(node.asText().trim()));

        return correctSet.equals(userSet);
    }

    /**
     * For drag-and-drop classification.
     * Expects a JSON Object where keys are categories and values are arrays of items.
     * Example: {"Compute": ["VM", "Functions"], "Storage": ["Blob"]}
     */
    private boolean evaluateDragClassify(JsonNode correct, JsonNode user) {
        if (!correct.isObject() || !user.isObject()) return false;
        if (correct.size() != user.size()) return false;

        Iterator<Map.Entry<String, JsonNode>> fields = correct.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String category = field.getKey();
            JsonNode correctItems = field.getValue();
            JsonNode userItems = user.get(category);

            // If the user missed a category entirely, or the items inside don't match (unordered)
            if (userItems == null || !evaluateArrayUnordered(correctItems, userItems)) {
                return false;
            }
        }
        return true;
    }

    /**
     * For grid and matching questions.
     * Expects a flat JSON object mapping rows to selections.
     * Jackson's equals() handles objects perfectly, ignoring key order natively.
     * Example: {"Row1": "Yes", "Row2": "No"}
     */
    private boolean evaluateExactMatch(JsonNode correct, JsonNode user) {
        return correct.equals(user);
    }
}
