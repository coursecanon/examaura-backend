package com.coursecanon.examaura.service.evaluation;

import com.coursecanon.examaura.entity.enums.QuestionType;
import com.coursecanon.examaura.service.AnswerEvaluator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerEvaluatorTest {

    private AnswerEvaluator evaluator;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        evaluator = new AnswerEvaluator();
        mapper = new ObjectMapper();
    }

    // ==========================================
    // 1. OBJECTIVE (Single Choice)
    // ==========================================
    @Test
    @DisplayName("Objective - Exact Match")
    void testObjective_Correct() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("\"Option B\"");
        JsonNode user = mapper.readTree("\"Option B\"");
        assertTrue(evaluator.evaluate(QuestionType.OBJECTIVE, correct, user));
    }

    @Test
    @DisplayName("Objective - Case & Whitespace Insensitive")
    void testObjective_CorrectWithFormatting() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("\"Option B\"");
        JsonNode user = mapper.readTree("\"  option b  \""); // Messy user input
        assertTrue(evaluator.evaluate(QuestionType.OBJECTIVE, correct, user));
    }

    @Test
    @DisplayName("Objective - Incorrect")
    void testObjective_Incorrect() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("\"Option B\"");
        JsonNode user = mapper.readTree("\"Option A\"");
        assertFalse(evaluator.evaluate(QuestionType.OBJECTIVE, correct, user));
    }

    // ==========================================
    // 2. MULTIPLE CHOICE (Unordered Arrays)
    // ==========================================
    @Test
    @DisplayName("Multiple Choice - Correct but Out of Order")
    void testMultipleChoice_CorrectOutOfOrder() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("[\"A\", \"B\", \"C\"]");
        JsonNode user = mapper.readTree("[\"C\", \"A\", \"B\"]"); // Different order
        assertTrue(evaluator.evaluate(QuestionType.MULTIPLE_CHOICE, correct, user));
    }

    @Test
    @DisplayName("Multiple Choice - Missing an Option")
    void testMultipleChoice_IncorrectSize() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("[\"A\", \"B\"]");
        JsonNode user = mapper.readTree("[\"A\"]");
        assertFalse(evaluator.evaluate(QuestionType.MULTIPLE_CHOICE, correct, user));
    }

    @Test
    @DisplayName("Multiple Choice - Wrong Option Selected")
    void testMultipleChoice_IncorrectMatch() throws JsonProcessingException {
        JsonNode correct = mapper.readTree("[\"A\", \"B\"]");
        JsonNode user = mapper.readTree("[\"A\", \"C\"]");
        assertFalse(evaluator.evaluate(QuestionType.MULTIPLE_CHOICE, correct, user));
    }

    // ==========================================
    // 3. DRAG_CLASSIFY (Objects with Arrays)
    // ==========================================
    @Test
    @DisplayName("Drag Classify - Correct with Unordered Items inside Categories")
    void testDragClassify_Correct() throws JsonProcessingException {
        String correctJson = "{ \"Compute\": [\"VM\", \"Functions\"], \"Storage\": [\"Blob\"] }";
        // User puts "Functions" before "VM", and ordered "Storage" before "Compute"
        String userJson = "{ \"Storage\": [\"Blob\"], \"Compute\": [\"Functions\", \"VM\"] }";

        assertTrue(evaluator.evaluate(QuestionType.DRAG_CLASSIFY,
                mapper.readTree(correctJson), mapper.readTree(userJson)));
    }

    @Test
    @DisplayName("Drag Classify - Missing a Category")
    void testDragClassify_MissingCategory() throws JsonProcessingException {
        String correctJson = "{ \"Compute\": [\"VM\"], \"Storage\": [\"Blob\"] }";
        String userJson = "{ \"Compute\": [\"VM\"] }"; // Forgot storage entirely

        assertFalse(evaluator.evaluate(QuestionType.DRAG_CLASSIFY,
                mapper.readTree(correctJson), mapper.readTree(userJson)));
    }

    @Test
    @DisplayName("Drag Classify - Wrong Item in Category")
    void testDragClassify_WrongItem() throws JsonProcessingException {
        String correctJson = "{ \"Compute\": [\"VM\"] }";
        String userJson = "{ \"Compute\": [\"Blob\"] }"; // Put storage item in compute

        assertFalse(evaluator.evaluate(QuestionType.DRAG_CLASSIFY,
                mapper.readTree(correctJson), mapper.readTree(userJson)));
    }

    // ==========================================
    // 4. EXACT MATCH (Grid / Matching)
    // ==========================================
    @Test
    @DisplayName("Exact Match - Correct with Unordered Keys")
    void testExactMatch_Correct() throws JsonProcessingException {
        String correctJson = "{ \"Row1\": \"Yes\", \"Row2\": \"No\" }";
        String userJson = "{ \"Row2\": \"No\", \"Row1\": \"Yes\" }"; // Different key order

        // Jackson ObjectNode .equals() safely ignores key order
        assertTrue(evaluator.evaluate(QuestionType.YES_NO_GRID,
                mapper.readTree(correctJson), mapper.readTree(userJson)));
    }

    @Test
    @DisplayName("Exact Match - Incorrect Answer")
    void testExactMatch_Incorrect() throws JsonProcessingException {
        String correctJson = "{ \"Row1\": \"Yes\" }";
        String userJson = "{ \"Row1\": \"No\" }";

        assertFalse(evaluator.evaluate(QuestionType.MATCHING_DROPDOWN,
                mapper.readTree(correctJson), mapper.readTree(userJson)));
    }

    // ==========================================
    // 5. EDGE CASES
    // ==========================================
    @Test
    @DisplayName("Edge Case - Null Inputs")
    void testNullInputs() {
        // Should safely return false without throwing NullPointerExceptions
        assertFalse(evaluator.evaluate(QuestionType.OBJECTIVE, null, null));
    }
}