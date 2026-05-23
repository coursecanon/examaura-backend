package com.coursecanon.examaura.entity.enums;

public enum QuizDifficulty {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String value;

    QuizDifficulty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QuizDifficulty fromValue(String value) {
        for (QuizDifficulty difficulty : QuizDifficulty.values()) {
            if (difficulty.value.equalsIgnoreCase(value)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty: " + value);
    }
}
