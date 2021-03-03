package com.gjg.backend;

import com.gjg.backend.model.Leaderboard;
import com.gjg.backend.model.LeaderboardResponse;
import com.gjg.backend.model.User;

import java.util.*;

public class MemoryRepo {
    private static final int USER_PER_PAGE = 10;

    public HashMap<UUID, Integer> indexMap = new HashMap<>();
    private List<User> users = Collections.synchronizedList(new LinkedList<>());

    public void addUser(User user) {
        users.add(user);
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

    public synchronized void updatePointsOfUser(UUID userId, double gainedScore) {
        int index = indexMap.get(userId);
        User user = users.get(index);

        if (user == null) {
            new Throwable("user not found. user_id: " + userId.toString()).printStackTrace();
            return;
        }
        user.setPoints(user.getPoints() + gainedScore);


        int newPosition = updateUserPosition(0, index, user);
        if (newPosition == -1) {
            new Throwable("Sort error.").printStackTrace();
            return;
        }
        removeUser(index + 1);
        updateIndexMap(newPosition, index);
    }

    public int updateUserPosition(int topIndex, int usersIndex, User user) {
        if (usersIndex < topIndex) {
            addUser(topIndex, user);
            return topIndex;
        }

        int index = topIndex + (usersIndex - topIndex) / 2;

        User userToCompare = users.get(index);

        if (user.getPoints() > userToCompare.getPoints()) {
            return updateUserPosition(topIndex, index - 1, user);
        } else if (user.getPoints() < userToCompare.getPoints()) {
            return updateUserPosition(index + 1, usersIndex, user);
        } else {
            // equal state.
            addUser(index, user);
            return index;
        }
    }

    public LeaderboardResponse getLeaderboard(int page, String country) {
        int userCount = 0;
        int index = (page - 1) * USER_PER_PAGE;
        List<User> listForIterate;

        LeaderboardResponse leaderboardResponse = new LeaderboardResponse();
        leaderboardResponse.leaderboard = new ArrayList<>(10);


        if (country != null) {
            listForIterate = getUsersByCountry(country);
        } else {
            listForIterate = users;
        }

        leaderboardResponse.total_page = (int) Math.ceil(listForIterate.size() / USER_PER_PAGE);

        while (userCount < USER_PER_PAGE && index < listForIterate.size()) {

            User user = listForIterate.get(index++);
            Leaderboard leaderboardItem = new Leaderboard();
            leaderboardItem.rank = indexMap.get(user.getId()) + 1;
            leaderboardItem.points = user.getPoints();
            leaderboardItem.display_name = user.getDisplay_name();
            leaderboardItem.country = user.getCountry();


            leaderboardResponse.leaderboard.add(leaderboardItem);
            userCount++;
        }

        leaderboardResponse.page = page;
        leaderboardResponse.last_page = index >= users.size();

        return leaderboardResponse;
    }

    public LeaderboardResponse getUsersByPage(int page) {
        return getLeaderboard(page, null);
    }

    private List<User> getUsersByCountry(String country) {
        List<User> userListByCounty = new ArrayList<>();

        for (User user : users) {
            if (user.getCountry().equals(country)) {
                userListByCounty.add(user);
            }
        }

        return userListByCounty;
    }

    public void removeUser(UUID userID) {
        removeUser(indexMap.get(userID));
    }

    public void removeUser(int index) {
        users.remove(index);
    }
}
