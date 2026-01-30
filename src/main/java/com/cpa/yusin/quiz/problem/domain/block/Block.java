package com.cpa.yusin.quiz.problem.domain.block;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        visible = true,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextBlock.class, name = "text"),
        @JsonSubTypes.Type(value = ImageBlock.class, name = "image"),
        @JsonSubTypes.Type(value = ListBlock.class, name = "list"),
        @JsonSubTypes.Type(value = ListItemBlock.class, name = "listItem")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Block
{
    private String type;
    private String align;
}