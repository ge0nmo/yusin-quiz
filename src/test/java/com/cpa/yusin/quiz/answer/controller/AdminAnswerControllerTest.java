package com.cpa.yusin.quiz.answer.controller;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnswerControllerTest {

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private AdminAnswerController adminAnswerController;

    private Member adminMember;
    private Principal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .id(1L)
                .email("admin@test.com")
                .password("encoded-password")
                .username("admin")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build();

        MemberDetails memberDetails = new MemberDetails(adminMember, null);
        adminPrincipal = new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());
    }

    @Test
    void createAnswerShouldUseAuthenticatedAdminAndWrapResponse() {
        AdminAnswerRegisterRequest request = new AdminAnswerRegisterRequest("관리자 답변");
        when(answerService.save(request, 10L, adminMember)).thenReturn(99L);

        ResponseEntity<GlobalResponse<Long>> response =
                adminAnswerController.createAnswer(10L, request, adminPrincipal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(99L);
        verify(answerService).save(request, 10L, adminMember);
    }

    @Test
    void updateAnswerShouldReturnEmptyWrappedResponse() {
        AdminAnswerUpdateRequest request = new AdminAnswerUpdateRequest("수정된 관리자 답변");

        ResponseEntity<GlobalResponse<Void>> response = adminAnswerController.updateAnswer(7L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(GlobalResponse.class);
        GlobalResponse<Void> body = response.getBody();
        assertThat(body.getData()).isNull();
        assertThat(body.getPageInfo()).isNull();
        verify(answerService).updateInAdminPage(request, 7L);
    }

    @Test
    void getAnswersShouldIncludePageInfo() {
        AnswerDTO answer = AnswerDTO.builder()
                .id(3L)
                .content("답변 내용")
                .createdAt(LocalDateTime.of(2026, 3, 9, 10, 0))
                .memberId(adminMember.getId())
                .username(adminMember.getUsername())
                .build();

        when(answerService.getAnswersByQuestionId(5L, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(answer), PageRequest.of(0, 2), 3));

        ResponseEntity<GlobalResponse<List<AnswerDTO>>> response =
                adminAnswerController.getAnswers(5L, PageRequest.of(0, 2));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(answer);
        assertThat(response.getBody().getPageInfo()).isNotNull();
        assertThat(response.getBody().getPageInfo().getTotalElements()).isEqualTo(3);
        assertThat(response.getBody().getPageInfo().getTotalPages()).isEqualTo(2);
        assertThat(response.getBody().getPageInfo().getCurrentPage()).isEqualTo(1);
        assertThat(response.getBody().getPageInfo().getPageSize()).isEqualTo(2);
        verify(answerService).getAnswersByQuestionId(5L, PageRequest.of(0, 2));
    }

    @Test
    void deleteAnswerShouldDelegateUsingAuthenticatedAdmin() {
        adminAnswerController.deleteAnswer(12L, adminPrincipal);

        verify(answerService).deleteAnswer(12L, adminMember);
    }
}
