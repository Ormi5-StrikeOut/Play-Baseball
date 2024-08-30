package org.example.spring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberJoinRequestDto memberJoinRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilter((request, response, chain) -> {
                response.setCharacterEncoding("UTF-8");
                chain.doFilter(request, response);
            }, "/*")
            .build();

        memberJoinRequestDto = MemberJoinRequestDto.builder()
            .email("test@example.com")
            .password("Password1!")
            .nickname("testuser")
            .name("Test User")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .build();
    }

    @Test
    @DisplayName("회원 가입 성공 테스트")
    void registerMember_Success() throws Exception {
        Member savedMember = Member.builder()
            .id(1L)
            .name(memberJoinRequestDto.getName())
            .email(memberJoinRequestDto.getEmail())
            .nickname(memberJoinRequestDto.getNickname())
            .password(memberJoinRequestDto.getPassword())
            .phoneNumber(memberJoinRequestDto.getPhoneNumber())
            .gender(memberJoinRequestDto.getGender())
            .role(MemberRole.USER)
            .build();

        when(memberService.registerMember(any(MemberJoinRequestDto.class))).thenReturn(savedMember);

        MvcResult result = mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberJoinRequestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."))
            .andExpect(jsonPath("$.data").value(savedMember.getNickname()))
            .andDo(print())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Actual response: " + responseContent);
    }

    @Test
    @DisplayName("회원 가입 실패 테스트 - 서버 오류")
    void registerMember_Failure_ServerError() throws Exception {
        when(memberService.registerMember(any(MemberJoinRequestDto.class)))
            .thenThrow(new RuntimeException("서버 오류 발생"));

        MvcResult result = mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberJoinRequestDto)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andDo(print())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Actual response: " + responseContent);
    }

    @Test
    @DisplayName("회원 가입 실패 테스트 - 잘못된 요청")
    void registerMember_Failure_BadRequest() throws Exception {
        String errorMessage = "잘못된 요청입니다.";
        when(memberService.registerMember(any(MemberJoinRequestDto.class)))
            .thenThrow(new IllegalArgumentException(errorMessage));

        MvcResult result = mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberJoinRequestDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("회원가입에 실패했습니다: " + errorMessage))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andDo(print())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Actual response: " + responseContent);
    }

    @Test
    @DisplayName("회원 전체 목록 조회 - 페이징")
    void getAllMembers_no_search() throws Exception {
        // given
        given(memberService.getAllMembers(anyInt(), anyInt()))
            .willReturn(Page.empty());

        // when
        ResultActions actions = mockMvc.perform(
            get("/api/members")
                .param("page", "0")
                .param("size", "10"));

        // then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isMap())
            .andDo(print());
    }

}