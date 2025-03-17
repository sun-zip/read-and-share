package com.flab.readnshare.domain.follow.controller;

import com.flab.readnshare.domain.follow.facade.FollowFacade;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.config.WebMvcConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FollowApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebMvcConfig.class
        )
)
@MockBean(JpaMetamodelMappingContext.class)
class FollowApiControllerTest {

    @MockBean
    FollowFacade followFacade;

    @Autowired
    MockMvc mockMvc;

    @DisplayName("특정 유저의 팔로워 목록을 조회한다.")
    @Test
    void followersOf() throws Exception {
        // Given
        List<MemberResponseDto> result = List.of();
        BDDMockito.given(followFacade.getFollowersOf(anyString()))
                .willReturn(result);

        // When
        // Then
        mockMvc
                .perform(
                        get("/api/follow/followers/{memberEmail}", "test")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}
