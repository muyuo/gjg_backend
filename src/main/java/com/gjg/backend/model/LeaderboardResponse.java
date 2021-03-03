package com.gjg.backend.model;

import java.util.List;

public class LeaderboardResponse {
    public int page;
    public int total_page;
    public boolean last_page;
    public List<Leaderboard> leaderboard;
}
