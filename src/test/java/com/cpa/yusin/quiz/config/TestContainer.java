package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapperImpl;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.service.ChoiceServiceImpl;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.infrastructure.CascadeDeleteServiceImpl;
import com.cpa.yusin.quiz.common.service.CascadeDeleteService;
import com.cpa.yusin.quiz.exam.controller.AdminExamController;
import com.cpa.yusin.quiz.exam.controller.mapper.ExamMapper;
import com.cpa.yusin.quiz.exam.controller.mapper.ExamMapperImpl;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.service.ExamServiceImpl;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.jwt.JwtServiceImpl;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.AdminMemberController;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapperImpl;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.service.AuthenticationServiceImpl;
import com.cpa.yusin.quiz.member.service.MemberServiceImpl;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.mock.*;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapperImpl;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.service.ProblemServiceImpl;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.controller.AdminSubjectController;
import com.cpa.yusin.quiz.subject.controller.mapper.SubjectMapper;
import com.cpa.yusin.quiz.subject.controller.mapper.SubjectMapperImpl;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectValidatorImpl;
import com.cpa.yusin.quiz.subject.service.SubjectServiceImpl;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.subject.service.port.SubjectValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestContainer
{
    private static final String FAKE_SECRET_KEY = "thisIsATestSecretKeyUsedOnlyForTesasdfeefsdfewdfesredfesfwqewdasdqrewtingPurposes";

    public final MemberRepository memberRepository;
    public final MemberService memberService;
    public final MemberDetailsService memberDetailsService;
    public final CustomAuthenticationProvider authenticationProvider;
    public final PasswordEncoder passwordEncoder;
    public final JwtService jwtService;
    public final AuthenticationService authenticationService;
    public final MemberMapper memberMapper;
    public final AdminMemberController adminMemberController;

    /**
     *  subject
     */
    public final SubjectRepository subjectRepository;
    public final SubjectValidator subjectValidator;
    public final SubjectService subjectService;
    public final SubjectMapper subjectMapper;
    public final AdminSubjectController adminSubjectController;

    /**
     *  exam
     */
    public final ExamRepository examRepository;
    public final ExamMapper examMapper;
    public final ExamService examService;
    public final AdminExamController adminExamController;

    /**
     *  choice
     */
    public final ChoiceMapper choiceMapper;
    public final ChoiceRepository choiceRepository;
    public final ChoiceService choiceService;

    /**
     *  problem
     */
    public final ProblemMapper problemMapper;
    public final ProblemRepository problemRepository;
    public final ProblemService problemService;

    public final CascadeDeleteService cascadeDeleteService;

    public TestContainer()
    {
        this.memberRepository = new FakeMemberRepository();
        this.subjectRepository = new FakeSubjectRepository();
        this.examRepository = new FakeExamRepository();
        this.problemRepository = new FakeProblemRepository();
        this.choiceRepository = new FakeChoiceRepository();

        cascadeDeleteService = new CascadeDeleteServiceImpl(this.subjectRepository, this.examRepository, this.problemRepository, this.choiceRepository);

        this.memberMapper = new MemberMapperImpl();
        this.memberService = new MemberServiceImpl(this.memberRepository, this.memberMapper);
        this.memberDetailsService = new MemberDetailsService(this.memberRepository);
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authenticationProvider = new CustomAuthenticationProvider(this.memberDetailsService, this.passwordEncoder);
        this.jwtService = new JwtServiceImpl(FAKE_SECRET_KEY);
        this.authenticationService = new AuthenticationServiceImpl(this.passwordEncoder, this.jwtService,
                this.memberRepository, this.authenticationProvider, this.memberDetailsService, this.memberMapper);
        this.adminMemberController = new AdminMemberController(this.memberService);



        this.subjectValidator = new SubjectValidatorImpl(this.subjectRepository);
        this.subjectMapper = new SubjectMapperImpl();
        this.subjectService = new SubjectServiceImpl(this.subjectRepository, this.subjectMapper, this.subjectValidator, this.cascadeDeleteService);
        this.adminSubjectController = new AdminSubjectController(this.subjectService);

        this.examMapper = new ExamMapperImpl();
        this.examService = new ExamServiceImpl(this.examRepository, this.examMapper, this.subjectService, this.subjectMapper, this.cascadeDeleteService);
        this.adminExamController = new AdminExamController(this.examService);

        this.choiceMapper = new ChoiceMapperImpl();
        this.choiceService = new ChoiceServiceImpl(this.choiceRepository, this.choiceMapper);

        this.problemMapper = new ProblemMapperImpl();

        this.problemService = new ProblemServiceImpl(this.problemRepository, this.problemMapper,
                this.examService, this.choiceService);


    }

}
