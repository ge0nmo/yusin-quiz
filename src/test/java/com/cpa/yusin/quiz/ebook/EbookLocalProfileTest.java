package com.cpa.yusin.quiz.ebook;

import com.cpa.yusin.quiz.ebook.service.EbookSnapshotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;

/** 안내서의 독립 로컬 프로필이 운영 비밀 파일이나 S3 호출 없이 초기화되는지 검증합니다. */
@SpringBootTest(properties = {
        "EBOOK_DB_URL=jdbc:h2:mem:ebooklocal;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "EBOOK_DB_USERNAME=sa", "EBOOK_DB_PASSWORD=", "EBOOK_DDL_AUTO=create-drop",
        "EBOOK_JWT_SECRET=ebook-local-profile-test-only-key-not-for-production-0123456789abcdef",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "ADMIN_BOOTSTRAP_LOGIN_ID=", "ADMIN_BOOTSTRAP_PASSWORD="
})
@ActiveProfiles("ebook-local")
class EbookLocalProfileTest {
    @Autowired DataSource dataSource;
    @Autowired EbookSnapshotService snapshots;

    @Test
    void startsWithExplicitLocalSettingsAndAnEmptyIsolatedDatabase() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:ebooklocal");
        }
        assertThat(snapshots.catalog()).isEmpty();
    }
}
