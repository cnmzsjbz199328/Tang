package com.tang0488;

import com.tang0488.Poem.PoemService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public void createRoom(String roomName, String password) {
        Game game = new Game(new UserPool(), new RandomMoveStrategy(), new SmartMoveStrategy(new RandomMoveStrategy()), new PoemService());
        rooms.put(roomName, new Room(roomName, password, game));
    }

    public boolean roomExists(String roomName) {
        return rooms.containsKey(roomName);
    }

    public boolean joinRoom(String roomName, String password) {
        Room room = rooms.get(roomName);
        return room != null && room.getPassword().equals(password);
    }

    public void leaveRoom(String roomName, String username) {
        Room room = rooms.get(roomName);
        if (room != null) {
            Game game =room.getGame();
            game.getUserPool().removeUser(username);

            // 获取当前玩家列表和当前玩家索引
            List<User> players = game.getPlayers();
            int currentPlayerIndex = game.getCurrentPlayerIndex();

            // 检查当前玩家是否为离开的玩家，并调整索引
            if (currentPlayerIndex >= players.size()) {
                game.setCurrentPlayerIndex(currentPlayerIndex % players.size());
            }

            // 检查如果玩家列表为空，移除房间
            if (players.isEmpty()) {
                rooms.remove(roomName);
            }
        }
    }

    public Board getBoard(String roomName) {
        Room room = rooms.get(roomName);
        return room != null ? room.getGame().getBoard() : null;
    }

    public Room getRoom(String roomName) {
        return rooms.get(roomName);
    }
}

@Getter
@Setter
class Room {
    private String roomName;
    private String password;
    private Game game;

    public Room(String roomName, String password, Game game) {
        this.roomName = roomName;
        this.password = password;
        this.game = game;
    }
}