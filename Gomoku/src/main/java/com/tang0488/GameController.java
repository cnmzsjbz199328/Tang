package com.tang0488;

import com.tang0488.Poem.Poem;
import com.tang0488.Poem.PoemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.core.AbstractDestinationResolvingMessagingTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/game")
public class GameController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    public GameController(RoomService roomService, SimpMessagingTemplate messagingTemplate, PoemService poemService) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    private String getRoomName(HttpSession session){
        return (String) session.getAttribute("roomName");
    }

    //@PreAuthorize("isAuthenticated()")
    @GetMapping
    public String getGame(Model model, HttpSession session) {
        String roomName = getRoomName(session);
        if (roomName != null) {
            Game game = roomService.getRoom(roomName).getGame();
            model.addAttribute("game", game);
        }
        return "index";
    }

    @GetMapping("/random-poem")
    public Poem getRandomPoem(HttpSession session) {
        String roomName = getRoomName(session);
        return roomService.getRoom(roomName).getGame().getPoemService().getRandomPoem();
    }

    @GetMapping("/players")
    @ResponseBody
    public List<String> getPlayers(HttpSession session) {
        String roomName = getRoomName(session);
        return roomService.getRoom(roomName).getGame().getPlayers().stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRoom(@RequestParam String roomName, @RequestParam String password, HttpSession session) {
        if (roomService.roomExists(roomName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Room name already exists.");
        }
        roomService.createRoom(roomName, password);

        joinRoom(roomName, password, session);
        return ResponseEntity.ok(roomName);
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(@RequestParam String roomName, @RequestParam String password, HttpSession session) {
        String oldRoomName = getRoomName(session);
        String oldUsername = (String) session.getAttribute("username");

        if (oldRoomName != null && oldUsername != null) {
            roomService.leaveRoom(oldRoomName, oldUsername);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User " + oldUsername + " has left the room.");
            response.put("users", roomService.getRoom(oldRoomName).getGame().getUserPool().getUsers());
            messagingTemplate.convertAndSend("/topic/users/" + oldRoomName, response);

        }

        session.removeAttribute("roomName");
        session.removeAttribute("username");

        boolean success = roomService.joinRoom(roomName, password);
        if (success) {
            session.setAttribute("roomName", roomName);
            Map<String, Object> response = new HashMap<>();
            response.put("users", roomService.getRoom(roomName).getGame().getUserPool().getUsers());
            messagingTemplate.convertAndSend("/topic/users/" + roomName, response);
            return ResponseEntity.ok("Joined room successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid room ID or password.");
        }
    }

    // 增加的接口，用于获取当前游戏状态
    @GetMapping("/state")
    public Map<String, Object> getCurrentGameState(HttpSession session) {
        String roomName = getRoomName(session);
        Map<String, Object> gameState = new HashMap<>();
        gameState.put("currentPlayer", roomService.getRoom(roomName).getGame().getCurrentPlayer());
        gameState.put("board", roomService.getBoard(roomName).getBoard());
        gameState.put("users", roomService.getRoom(roomName).getGame().getUserPool().getUsers());
//        gameState.put("poem", poemService.getRandomPoem().getLines());
        return gameState;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/move")
    @ResponseBody
    public Map<String, Object> processMove(@RequestBody Map<String, Integer> move, HttpSession session) {
        System.out.println("Processing move: " + move);

        String roomName = getRoomName(session);
        Game game = roomService.getRoom(roomName).getGame();
        UserPool userPool = game.getUserPool();
        PoemService poemService = game.getPoemService();

        String sessionUsername = (String) session.getAttribute("username");  // 从会话中获取当前用户名
        if (sessionUsername == null) {
            return Map.of("message", "You need to be registered and logged in to make a move.");
        }

        int row = move.get("row");
        int col = move.get("col");
        Map<String, Object> response = new HashMap<>();

        if (!sessionUsername.equals(game.getCurrentPlayer())) {
            response.put("message", "It's not your turn or invalid session.");
            return response;
        }

        boolean moveMade = game.makeMove(row, col);
        response.put("moveMade", moveMade);
        response.put("currentPlayer", game.getCurrentPlayer());
        response.put("board", game.getBoard().getBoard());

        if (moveMade) {
            if (game.checkWin(game.getCurrentPlayer())) { // 修正调用方法
                int scoreIncrement = game.getBoard().clearWinningLine(game.getCurrentPlayer());
                User currentUser = userPool.findByUsername(game.getCurrentPlayer());
                currentUser.addScore(scoreIncrement);
                response.put("winner", game.getCurrentPlayer());
                response.put("score", currentUser.getScore());  // 更新分数
                response.put("poem", poemService.getRandomPoem().getLines());// Send poem when player wins
                response.put("removedPoints", game.getRemovedPoints());  // 添加获胜棋子列表
                response.put("randomRemovedPoints", game.getRandomRemovedPoints());  // 添加随机清除棋子列表
            }
            game.nextPlayer();
            // 添加策略玩家的判断和执行策略
            if (game.getCurrentPlayer().equals("G")) {  // 假设策略玩家的用户名为"G"
                game.getMoveStrategy().makeMove(game.getBoard(), game.getCurrentPlayer());
                response.put("board", game.getBoard().getBoard());
                if (game.checkWin(game.getCurrentPlayer())) {
                    int scoreIncrement = game.getBoard().clearWinningLine(game.getCurrentPlayer());
                    User currentUser = userPool.findByUsername(game.getCurrentPlayer());
                    currentUser.addScore(scoreIncrement);
                    response.put("winner", game.getCurrentPlayer());
                    response.put("score", currentUser.getScore());
                    response.put("poem", poemService.RandomWin());  // Ensure this returns a Poem object with a content attribute
                    response.put("removedPoints", game.getRemovedPoints());  // 添加获胜棋子列表
                    response.put("randomRemovedPoints", game.getRandomRemovedPoints());  // 添加随机清除棋子列表
                }
                game.nextPlayer();
            }
            response.put("currentPlayer", game.getCurrentPlayer());
        }
        response.put("users", userPool.getUsers()); // 修改点6：添加用户列表到响应
        // 广播更新给所有客户端
        messagingTemplate.convertAndSend("/topic/game-progress/" + roomName, response);
        System.out.println("Response: " + response);
        return response;
    }

    @PostMapping("/strategy")
    @ResponseBody
    public void setStrategy(@RequestParam String strategy,HttpSession session) {
        String roomName = getRoomName(session);
        Game game = roomService.getRoom(roomName).getGame();
        MoveStrategy randomMoveStrategy= game.getMoveStrategy();
        MoveStrategy smartMoveStrategy =game.getMoveStrategy();
        if ("random".equalsIgnoreCase(strategy)) {
            game.setMoveStrategy(new RandomMoveStrategy());
        } else if ("smart".equalsIgnoreCase(strategy)) {
            game.setMoveStrategy(new SmartMoveStrategy(new RandomMoveStrategy()));
        }
    }

    @GetMapping("/check-session")
    @ResponseBody
    public Map<String, Object> checkSession(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            if (username != null) {
                response.put("username", username);
            }
        }
        return response;
    }


    @PostMapping("/register")
    @ResponseBody
    /*
优点：
灵活性：HttpServletRequest 提供了对整个请求上下文的访问，包括参数、头信息、会话等，这使得你可以在一个参数中访问更多的请求数据。
广泛适用：在需要访问除会话外的其他请求数据时，使用 HttpServletRequest 可以避免需要多个参数。
缺点：
复杂性：如果你的方法只需要 HttpSession，那么使用 HttpServletRequest 可能稍显冗余，因为它提供了对整个请求的访问权限，即使这些可能在当前上下文中并不需要。
     */
    public Map<String, Object> registerPlayer(@RequestParam String username, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        HttpSession session = request.getSession();
        String roomName = getRoomName(session);
        // 检查会话中是否已经绑定了用户名，防止重复注册
        if (session.getAttribute("username") != null) {
            response.put("message", "You are already registered as " + request.getSession().getAttribute("username"));
            return response;
        }

        Room room = roomService.getRoom(roomName);
        UserPool userPool = room.getGame().getUserPool();

        // 检查用户名是否已被占用
        if (userPool.findByUsername(username) != null) {
            response.put("message", "Username already taken.");
            return response;
        }

        // 创建新用户对象
        User newUser = new User();
        newUser.setName(username);

        // 将新用户添加到用户池中
        userPool.addUser(newUser);

        // 通过WebSocket广播新的用户列表
        messagingTemplate.convertAndSend("/topic/users/" +roomName, userPool.getUsers());

        // 在会话中设置用户名，标记用户已注册
        session.setAttribute("username", username);
        response.put("message", "Player registered successfully.");
        System.out.println("Session username set: " + session.getAttribute("username"));

        // 将用户列表添加到响应中，使客户端可以立即看到更新
        response.put("users", userPool.getUsers());
        return response;
    }
}