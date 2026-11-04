package dmodule.pattern;

import stud.g01.problem.npuzzle.PuzzleBoard;
import java.util.*;

public class PDBGenerator {
    public static class GenResult {
        public final byte[] pdbTable;
        public final long expandedNodes;
        public final long timeMs;

        public GenResult(byte[] pdbTable, long expandedNodes, long timeMs) {
            this.pdbTable = pdbTable;
            this.expandedNodes = expandedNodes;
            this.timeMs = timeMs;
        }
    }

    public GenResult generateBFS(Pattern pattern, PuzzleBoard goalBoard) {
        long start = System.nanoTime();

        // 4 个方块的最大排列组合大约是 52.4 万，开 100 万保证不溢出
        int tableSize = 1000000;
        byte[] pdb = new byte[tableSize];
        Arrays.fill(pdb, (byte) -1);
        Queue<Long> queue = new ArrayDeque<>();

        int[] goalPositions = new int[16];
        Arrays.fill(goalPositions, -1);
        int goalBlank = -1;
        for (int pos = 0; pos < 16; pos++) {
            int tile = goalBoard.getTile(pos);
            if (tile == 0) goalBlank = pos;
            else if (pattern.containsTile(tile)) goalPositions[tile] = pos;
        }

        long startKey = PatternState.encode(goalPositions, goalBlank);
        int startHash = (int)((startKey ^ (startKey >>> 32)) % tableSize);
        if(startHash < 0) startHash += tableSize;

        pdb[startHash] = 0;
        queue.offer(startKey);

        long expandCnt = 0;
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            long curKey = queue.poll();
            expandCnt++;

            int curHash = (int)((curKey ^ (curKey >>> 32)) % tableSize);
            if(curHash < 0) curHash += tableSize;
            int curCost = pdb[curHash];

            int blankPos = (int) (curKey & 0xF);
            int[] tilePos = new int[16];
            Arrays.fill(tilePos, -1);

            long temp = curKey >> 4;
            for (int i = 15; i >= 1; i--) {
                if (temp == 0 && i < 15) break;
                tilePos[i] = (int) (temp & 0xF);
                temp >>= 4;
            }

            int r = blankPos / 4;
            int c = blankPos % 4;

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr < 0 || nr >= 4 || nc < 0 || nc >= 4) continue;

                int nextBlankPos = nr * 4 + nc;

                int movedTile = -1;
                for (int t = 1; t <= 15; t++) {
                    if (tilePos[t] == nextBlankPos) {
                        movedTile = t;
                        break;
                    }
                }

                boolean isPatternTile = false;
                if (movedTile != -1) {
                    isPatternTile = pattern.containsTile(movedTile);
                }

                int stepCost = isPatternTile ? 1 : 0;

                if (movedTile != -1) {
                    tilePos[movedTile] = blankPos;
                }
                long nextKey = PatternState.encode(tilePos, nextBlankPos);

                int nextHash = (int)((nextKey ^ (nextKey >>> 32)) % tableSize);
                if(nextHash < 0) nextHash += tableSize;

                if (pdb[nextHash] == -1) {
                    pdb[nextHash] = (byte) (curCost + stepCost);
                    queue.offer(nextKey);
                }

                if (movedTile != -1) {
                    tilePos[movedTile] = nextBlankPos;
                }
            }
        }
        long costMs = (System.nanoTime() - start) / 1_000_000;
        return new GenResult(pdb, expandCnt, costMs);
    }

    public GenResult generateBiBFS(Pattern pattern, PuzzleBoard goalBoard) {
        return generateBFS(pattern, goalBoard);
    }
}
