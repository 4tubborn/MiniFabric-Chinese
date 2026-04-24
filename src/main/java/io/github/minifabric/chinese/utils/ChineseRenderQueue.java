package io.github.minifabric.chinese.utils;

import java.util.ArrayList;
import java.util.List;

public class ChineseRenderQueue {
    public static class Task {
        public int x, y, charIdx, whiteTint;

        public Task(int x, int y, int charIdx, int whiteTint) {
            this.x = x;
            this.y = y;
            this.charIdx = charIdx;
            this.whiteTint = whiteTint;
        }
    }

    public static final List<Task> TASKS = new ArrayList<>();
}