package com.gjg.backend;

import com.gjg.backend.controller.UserController;
import com.gjg.backend.model.*;

import java.util.*;

public class MemoryRepo {
    private static final int USER_PER_PAGE = 10;

    public Hashtable<UUID, User> indexMap = new Hashtable<>(200000);
    private List<User> users = Collections.synchronizedList(new LinkedList<>());

    public void addUser(User user) {
        users.add(user);
        indexMap.put(user.getId(), user);
    }

    public void addUser(int index, User user) {
        users.add(index, user);
        indexMap.put(user.getId(), user);
    }

    public List<User> getUsers() {
        return users;
    }

    public synchronized Response updatePointsOfUser(UUID userId, double gainedScore, UserController controllerClass) {
        Response response = new Response();
        User user = indexMap.get(userId);
        int index = users.indexOf(user);
        ScoreSubmitResponse scoreSubmitResponse = new ScoreSubmitResponse();

        if (gainedScore == 0) {
            response.setCode("200");
            response.setMessage("ok");
            scoreSubmitResponse.user_id = userId.toString();
            scoreSubmitResponse.rank_change = 0;
            scoreSubmitResponse.total_score = user.getPoints();
            return response;
        }

        if (user == null) {
            new Throwable("user not found. user_id: " + userId.toString()).printStackTrace();
            response.setCode("500");
            response.setMessage("User not found");
            return response;
        }
        user.setPoints(user.getPoints() + gainedScore);

        if (controllerClass != null) {
            Scanner.addTask(new Task(user, controllerClass::saveUserToDb, "Score Update"));
        }


        int newPosition = updateUserPosition(0, index, user);
        if (newPosition == -1) {
            new Throwable("Sort error.").printStackTrace();
            response.setCode("500");
            response.setMessage("Internal server error");
            return response;
        }
        removeUser(index + 1);

        scoreSubmitResponse.user_id = userId.toString();
        scoreSubmitResponse.rank_change = index - newPosition;
        scoreSubmitResponse.total_score = user.getPoints();
        response.setCode("200");
        response.setMessage("ok");
        response.setData(scoreSubmitResponse);

        return response;
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

        leaderboardResponse.total_page = (int) Math.ceil(listForIterate.size() / (float) USER_PER_PAGE);

        while (userCount < USER_PER_PAGE && index < listForIterate.size()) {

            User user = listForIterate.get(index++);
            Leaderboard leaderboardItem = new Leaderboard();
            leaderboardItem.rank = users.indexOf(user) + 1;
            leaderboardItem.points = user.getPoints();
            leaderboardItem.display_name = user.getDisplayName();
            leaderboardItem.country = user.getCountry();


            leaderboardResponse.leaderboard.add(leaderboardItem);
            userCount++;
        }

        leaderboardResponse.page = page;
        leaderboardResponse.last_page = leaderboardResponse.total_page <= page;

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

    public Response removeUser(UUID id, UserController controller) {
        Response response = new Response();
        User user = indexMap.get(id);
        if (user == null) {
            response.setCode("500");
            response.setMessage("User not found");
            return response;
        }
        Scanner.addTask(new Task(user, controller::removeUserFromDb, "Delete User"));
        indexMap.remove(id);
        removeUser(users.indexOf(user));

        response.setCode("200");
        response.setMessage("ok");
        return response;
    }

    public void removeUser(int index) {
        users.remove(index);
    }
}
