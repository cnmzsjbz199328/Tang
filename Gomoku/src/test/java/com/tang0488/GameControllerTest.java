package com.tang0488;

import com.tang0488.Poem.PoemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.messaging.simp.SimpMessagingTemplate;

//import javax.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GameController.class, excludeAutoConfiguration = SecurityConfig.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Game game;

    @MockBean
    private RandomMoveStrategy randomMoveStrategy;

    @MockBean
    private SmartMoveStrategy smartMoveStrategy;

    @MockBean
    private UserPool userPool;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private PoemService poemService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GameController(game, randomMoveStrategy, smartMoveStrategy, userPool, messagingTemplate, poemService))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "user1")
    public void testProcessMove_UnauthorizedUser() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("username")).thenReturn("user2"); // 模拟会话中的用户

        Map<String, Integer> move = new HashMap<>();
        move.put("row", 1);
        move.put("col", 1);

        mockMvc.perform(post("/game/move")
                        .sessionAttr("username", "user2")
                        .with(SecurityMockMvcRequestPostProcessors.user("user1")) // 模拟登录的用户
                        .contentType("application/json")
                        .content("{\"row\": 1, \"col\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moveMade").value(false))
                .andExpect(jsonPath("$.message").value("You are not allowed to make this move."));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testProcessMove_AuthorizedUser() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("username")).thenReturn("user1"); // 模拟会话中的用户
        when(game.getCurrentPlayer()).thenReturn("user1");
        when(game.makeMove(1, 1)).thenReturn(true);
        when(game.checkWin("user1")).thenReturn(false);

        Map<String, Integer> move = new HashMap<>();
        move.put("row", 1);
        move.put("col", 1);

        mockMvc.perform(post("/game/move")
                        .sessionAttr("username", "user1")
                        .with(SecurityMockMvcRequestPostProcessors.user("user1")) // 模拟登录的用户
                        .contentType("application/json")
                        .content("{\"row\": 1, \"col\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moveMade").value(true))
                .andExpect(jsonPath("$.currentPlayer").value("user1"));
    }
}
