package com.coursecanon.examaura.service;

import com.coursecanon.examaura.entity.Question;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AnswerEvaluator {

    /**
     * Routes the evaluation based on the question type.
     * Note: We now pass the entire Question object because answers for
     * complex types are hidden inside statements, categories, etc.
     */
    public boolean evaluate(Question question, JsonNode userAnswer) {
        // If the user skipped the question or the payload is empty
        if (userAnswer == null || userAnswer.isNull() || userAnswer.isMissingNode()) {
            return false;
        }

        try {
            return switch (question.getQuestionType()) {
                case OBJECTIVE -> evaluateObjective(question, userAnswer);
                case MULTIPLE_CHOICE -> evaluateMultipleChoice(question, userAnswer);
                case YES_NO_GRID -> evaluateYesNoGrid(question, userAnswer);
                case DRAG_MATCH -> evaluateDragMatch(question, userAnswer);
                case DRAG_CLASSIFY -> evaluateDragClassify(question, userAnswer);
                case MATCHING_DROPDOWN -> evaluateMatchingDropdown(question, userAnswer);
                case INLINE_DROPDOWN -> evaluateInlineDropdown(question, userAnswer);
                default -> false; // Failsafe
            };
        } catch (Exception e) {
            // If Jackson fails to parse or cast an unexpected frontend payload, mark it incorrect
            return false;
        }
    }

    private boolean evaluateObjective(Question question, JsonNode userAnswer) {
        // DB: {"answer": 1} OR 1. Frontend: 1
        JsonNode correctNode = question.getCorrectAnswer();
        if (correctNode == null) return false;

        int expected = correctNode.has("answer") ? correctNode.get("answer").asInt() : correctNode.asInt();
        return expected == userAnswer.asInt();
    }

    private boolean evaluateMultipleChoice(Question question, JsonNode userAnswer) {
        // DB: {"answers": [0, 1]}. Frontend: [0, 1]
        JsonNode correctNode = question.getCorrectAnswer();
        if (correctNode == null) return false;

        JsonNode correctArray = correctNode.has("answers") ? correctNode.get("answers") : correctNode;
        if (!userAnswer.isArray() || correctArray.size() != userAnswer.size()) return false;

        Set<Integer> expectedSet = new HashSet<>();
        correctArray.forEach(node -> expectedSet.add(node.asInt()));

        Set<Integer> actualSet = new HashSet<>();
        userAnswer.forEach(node -> actualSet.add(node.asInt()));

        return expectedSet.equals(actualSet);
    }

    private boolean evaluateYesNoGrid(Question question, JsonNode userAnswer) {
        // DB: statements -> [{"id": "stmt1", "correctAnswer": "yes"}]
        // Frontend: {"stmt1": "yes"}
        if (question.getStatements() == null || !userAnswer.isObject()) return false;

        for (JsonNode stmt : question.getStatements()) {
            String id = stmt.get("id").asText();
            String expectedAns = stmt.get("correctAnswer").asText();
            if (!userAnswer.has(id) || !userAnswer.get(id).asText().equals(expectedAns)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateDragMatch(Question question, JsonNode userAnswer) {
        // DB: matchPairs -> [{"id": "pair1", "term": "..."}]
        // Frontend: {"pair1": "pair1"}
        if (question.getMatchPairs() == null || !userAnswer.isObject()) return false;

        for (JsonNode pair : question.getMatchPairs()) {
            String id = pair.get("id").asText();
            if (!userAnswer.has(id) || !userAnswer.get(id).asText().equals(id)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateDragClassify(Question question, JsonNode userAnswer) {
        // DB: classifyItems -> [{"id": "item1", "correctCategoryId": "cat1"}]
        // Frontend: {"cat1": ["item1"]}
        if (question.getClassifyItems() == null || !userAnswer.isObject()) return false;

        for (JsonNode item : question.getClassifyItems()) {
            String itemId = item.get("id").asText();
            String correctCat = item.get("correctCategoryId").asText();

            boolean found = false;
            if (userAnswer.has(correctCat) && userAnswer.get(correctCat).isArray()) {
                for (JsonNode userItem : userAnswer.get(correctCat)) {
                    if (userItem.asText().equals(itemId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean evaluateMatchingDropdown(Question question, JsonNode userAnswer) {
        // DB: dropdownRows -> [{"id": "row1", "correctAnswer": 2}]
        // Frontend: {"row1": 2}
        if (question.getDropdownRows() == null || !userAnswer.isObject()) return false;

        for (JsonNode row : question.getDropdownRows()) {
            String id = row.get("id").asText();
            int expected = row.get("correctAnswer").asInt();
            if (!userAnswer.has(id) || userAnswer.get(id).asInt() != expected) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateInlineDropdown(Question question, JsonNode userAnswer) {
        // DB: inlineDropdowns -> [{"correctAnswer": 0}, {"correctAnswer": 1}]
        // Frontend: {"0": 0, "1": 1}
        if (question.getInlineDropdowns() == null || !userAnswer.isObject()) return false;

        for (int i = 0; i < question.getInlineDropdowns().size(); i++) {
            JsonNode dropdown = question.getInlineDropdowns().get(i);
            int expected = dropdown.get("correctAnswer").asInt();
            String indexStr = String.valueOf(i);
            if (!userAnswer.has(indexStr) || userAnswer.get(indexStr).asInt() != expected) {
                return false;
            }
        }
        return true;
    }
}