package com.tang0488;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserPool {
    private List<User> users;

    public UserPool() {
        users = new ArrayList<>();
        users.add(new User("G"));
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        if (user.getName().equals("G")) {
            return; // 策略玩家已经在构造函数中添加，避免重复添加
        }

        if (users.size() < 2) {
            // 如果玩家数少于2，将新用户插入到策略玩家前面
            users.add(0, user);
        } else {
            // 如果已有两个或更多玩家，直接添加到列表末尾
            users.add(user);
        }
    }

    public User findByUsername(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void removeUser(String username){
        users.removeIf(user -> user.getName().equals(username));
    }
}
