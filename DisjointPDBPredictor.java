package dmodule.pattern;

import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.problem.npuzzle.PuzzleBoard;
import java.util.ArrayList;
import java.util.List;

public class DisjointPDBPredictor implements Predictor {
    private final Pattern[] patterns;
    private final List<byte[]> pdbTables;

    public long totalExpandBFS = 0;
    public long totalTimeBFS = 0;

    public DisjointPDBPredictor(PuzzleBoard goalBoard, boolean useBiBFS) {
        patterns = Pattern.buildDisjointPatterns();
        pdbTables = new ArrayList<>();
        PDBGenerator generator = new PDBGenerator();

        for (int i = 0; i < patterns.length; i++) {
            Pattern pat = patterns[i];
            PDBGenerator.GenResult res = useBiBFS ? generator.generateBiBFS(pat, goalBoard) : generator.generateBFS(pat, goalBoard);

            pdbTables.add(res.pdbTable);
            totalExpandBFS += res.expandedNodes;
            totalTimeBFS += res.timeMs;

            System.out.printf("[PDB] pattern %d expanded=%d, time=%dms%n",
                    i, res.expandedNodes, res.timeMs);
        }
        System.out.printf("[PDB] total: expanded=%d, time=%dms%n", totalExpandBFS, totalTimeBFS);
    }

    @Override
    public int heuristics(State state, State goal) {
        PuzzleBoard board = (PuzzleBoard) state;
        int sum = 0;
        for (int i = 0; i < patterns.length; i++) {
            PatternState proj = new PatternState(board, patterns[i]);
            int hash = (int)((proj.key ^ (proj.key >>> 32)) % 1000000);
            if (hash < 0) hash += 1000000;

            byte val = pdbTables.get(i)[hash];
            if (val != -1) {
                sum += val;
            }
        }
        return sum;
    }
}
