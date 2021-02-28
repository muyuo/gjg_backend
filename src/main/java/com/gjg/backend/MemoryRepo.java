package com.gjg.backend;

import com.gjg.backend.model.User;

import java.util.*;

public class MemoryRepo {
    public HashMap<UUID, Integer> indexMap = new HashMap<>();
    private List<User> users = Collections.synchronizedList(new LinkedList<>());

    public void addUser(User user) {
        users.add(user);
        user.setRank(users.size());
        indexMap.put(user.getId(), users.size() - 1);
    }

    public void addUser(int index, User user) {
        users.add(index, user);
        indexMap.put(user.getId(), index);
    }

    public void updateIndexMap(int startIndex, int stopIndex) {
        for (int i = startIndex; i <= stopIndex; i++) {
            indexMap.put(users.get(i).getId(), i);
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public synchronized void updatePointsOfUser (UUID userId, double gainedScore) {
        int index = indexMap.get(userId);
        User user = users.get(index);
        user.setPoints(user.getPoints() + gainedScore);

        if (user == null) {
            new Throwable("user not found. user_id: " + userId.toString()).printStackTrace();
            return;
        }

        removeUser(index);
        int newPosition = updateUserPosition(0, index, user);
        if (newPosition == -1) {
            new Throwable("Sort error.").printStackTrace();
            return;
        }

        updateIndexMap(newPosition, index);
    }

    public int updateUserPosition(int topIndex, int usersIndex, User user) {
        if (usersIndex == topIndex) {
            addUser(usersIndex, user);
            return usersIndex;
        } else if (usersIndex < topIndex) {
            if (usersIndex < 0) {
                addUser(0, user);
                return 0;
            } else {
                return -1;
            }
        }

        int index = topIndex + (usersIndex - topIndex) / 2;

        User userToCompare = users.get(index);

        if (user.getPoints() > userToCompare.getPoints()) {
            return updateUserPosition(topIndex, index - 1, user);
        } else  if (user.getPoints() < userToCompare.getPoints()) {
            return updateUserPosition(index + 1, usersIndex, user);
        } else {
            // equal state.
            addUser(index, user);
            return index;
        }
    }

    public void removeUser(UUID userID) {
        removeUser(indexMap.get(userID));
    }

    public void removeUser(int index) {
        users.remove(index);
    }
}
