package com.cpa.yusin.quiz.file.infrastructure;

import com.cpa.yusin.quiz.file.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<File, Long>
{
}
