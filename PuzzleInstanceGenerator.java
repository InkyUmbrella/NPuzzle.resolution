package dmodule.test;

import stud.g01.problem.npuzzle.PuzzleBoard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PuzzleInstanceGenerator {

    /**
     * 生成count个4阶(15‑puzzle)可解实例
     */
    public List<PuzzleBoard> generateSolvable(int count) {
        List<PuzzleBoard> result = new ArrayList<>();
        while (result.size() < count) {
            // 0~15打乱
            List<Integer> list = IntStream.rangeClosed(0,15).boxed().collect(Collectors.toList());
            Collections.shuffle(list);
            int[] tiles = list.stream().mapToInt(Integer::intValue).toArray();
            PuzzleBoard board = new PuzzleBoard(4, tiles);
            if(isSolvable(board)){
                result.add(board);
            }
        }
        return result;
    }

    /**
     * 判断15‑puzzle是否可解
     * 逆序数(排除空格0) + 空格所在行(从1开始计数) 偶数 → 可解
     */
    public boolean isSolvable(PuzzleBoard board){
        int[] tiles = board.getTilesCopy();
        int inv = 0;
        int blankIdx = board.getBlankIndex();
        for(int i=0;i<tiles.length;i++){
            if(tiles[i]==0) continue;
            for(int j=i+1;j<tiles.length;j++){
                if(tiles[j]==0) continue;
                if(tiles[i]>tiles[j]) inv++;
            }
        }
        int blankRow1 = blankIdx / 4 + 1;
        return (inv + blankRow1) %2 == 0;
    }

    /**
     * 获取作业第三阶段那个最难实例
     * 截图棋盘：
     * 15 14 13 12
     * 11 10 9 8
     * 7 6 5 4
     * 3 1 2 #(0)
     */
    public PuzzleBoard getHardExamInstance(){
        int[] tiles = {
                15,14,13,12,
                11,10,9,8,
                7,6,5,4,
                3,1,2,0
        };
        return new PuzzleBoard(4, tiles);
    }

    /** 获取标准目标状态 */
    public PuzzleBoard getGoal(){
        int[] tiles = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0};
        return new PuzzleBoard(4,tiles);
    }
}
