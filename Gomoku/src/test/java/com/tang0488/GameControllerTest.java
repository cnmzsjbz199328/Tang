package com.tang0488;
import com.tang0488.Poem.Poem;
import com.tang0488.Poem.PoemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private Game game;

    @MockBean
    private UserPool userPool;

    @MockBean
    private PoemService poemService;

    @Mock
    private User mockUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessMove() {
        Map<String, Integer> move = new HashMap<>();
        move.put("row", 7);
        move.put("col", 7);

        // 模拟游戏行为
        when(game.makeMove(7, 7)).thenReturn(true);
        when(game.getCurrentPlayer()).thenReturn("player1");
        when(game.getBoard()).thenReturn(new Board());
        when(game.checkWin("player1")).thenReturn(true);
        when(game.getRemovedPoints()).thenReturn(new ArrayList<>());
        when(game.getRandomRemovedPoints()).thenReturn(new ArrayList<>());
        when(userPool.findByUsername("player1")).thenReturn(mockUser);
        when(mockUser.getScore()).thenReturn(6);
        when(poemService.getRandomPoem()).thenReturn(new Poem("Test Poem"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Integer>> request = new HttpEntity<>(move, headers);

        ResponseEntity<Map> response = restTemplate.exchange("http://localhost:" + port + "/game/move", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("moveMade")).isEqualTo(true);
        assertThat(response.getBody().get("winner")).isEqualTo("player1");
        assertThat(response.getBody().get("score")).isEqualTo(6);
        assertThat(response.getBody().get("poem")).isEqualTo("Test Poem");
    }

    @Test
    public void testProcessMoveWin() {
        Map<String, Integer> move = new HashMap<>();
        move.put("row", 7);
        move.put("col", 7);

        // 模拟游戏行为
        when(game.makeMove(7, 7)).thenReturn(true);
        when(game.getCurrentPlayer()).thenReturn("player1");
        when(game.getBoard()).thenReturn(new Board());
        when(game.checkWin("player1")).thenReturn(true);
        when(game.getRemovedPoints()).thenReturn(new ArrayList<>());
        when(game.getRandomRemovedPoints()).thenReturn(new ArrayList<>());
        when(userPool.findByUsername("player1")).thenReturn(mockUser);
        when(mockUser.getScore()).thenReturn(6);
        when(poemService.getRandomPoem()).thenReturn(new Poem("Test Poem"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Integer>> request = new HttpEntity<>(move, headers);

        ResponseEntity<Map> response = restTemplate.exchange("http://localhost:" + port + "/game/move", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("moveMade")).isEqualTo(true);
        assertThat(response.getBody().get("winner")).isEqualTo("player1");
        assertThat(response.getBody().get("score")).isEqualTo(6);
        assertThat(response.getBody().get("poem")).isEqualTo("Test Poem");
    }
}
