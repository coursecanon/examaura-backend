package com.coursecanon.examaura.entity.enums;

public enum QuestionType {
    OBJECTIVE("objective"),
    MULTIPLE_CHOICE("multiple-choice"),
    YES_NO_GRID("yes-no-grid"),
    DRAG_MATCH("drag-match"),
    DRAG_CLASSIFY("drag-classify"),
    INLINE_DROPDOWN("inline-dropdown"),
    MATCHING_DROPDOWN("matching-dropdown");

    private final String value;

    QuestionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QuestionType fromValue(String value) {
        for (QuestionType type : QuestionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown question type: " + value);
    }
}
