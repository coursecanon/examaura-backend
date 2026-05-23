package com.coursecanon.examaura.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.coursecanon.examaura.entity.enums.*;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_questions_type", columnList = "question_type")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "question_type", nullable = false, columnDefinition = "question_type_enum")
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_image_url", length = 512)
    private String questionImageUrl;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "position", nullable = false)
    private Integer position;

    // JSONB columns for polymorphic data
    @Type(JsonType.class)
    @Column(name = "options", columnDefinition = "jsonb")
    private JsonNode options;

    @Type(JsonType.class)
    @Column(name = "correct_answer", nullable = false, columnDefinition = "jsonb")
    private JsonNode correctAnswer;

    @Type(JsonType.class)
    @Column(name = "statements", columnDefinition = "jsonb")
    private JsonNode statements;

    @Type(JsonType.class)
    @Column(name = "match_pairs", columnDefinition = "jsonb")
    private JsonNode matchPairs;

    @Type(JsonType.class)
    @Column(name = "categories", columnDefinition = "jsonb")
    private JsonNode categories;

    @Type(JsonType.class)
    @Column(name = "classify_items", columnDefinition = "jsonb")
    private JsonNode classifyItems;

    @Column(name = "sentence_template", columnDefinition = "TEXT")
    private String sentenceTemplate;

    @Type(JsonType.class)
    @Column(name = "inline_dropdowns", columnDefinition = "jsonb")
    private JsonNode inlineDropdowns;

    @Type(JsonType.class)
    @Column(name = "dropdown_rows", columnDefinition = "jsonb")
    private JsonNode dropdownRows;



    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QuestionAnswer> answers = new ArrayList<>();
}
