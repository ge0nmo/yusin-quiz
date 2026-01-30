package com.cpa.yusin.quiz.problem.domain.block;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextBlock extends Block
{
    @Builder.Default
    private List<Span> spans = new ArrayList<>();
}