package com.dat3m.dartagnan.encoding;

import com.dat3m.dartagnan.program.Thread;
import com.dat3m.dartagnan.program.analysis.BranchEquivalence;
import com.dat3m.dartagnan.program.analysis.ThreadSymmetry;
import com.dat3m.dartagnan.program.event.Tag;
import com.dat3m.dartagnan.program.event.core.Event;
import com.dat3m.dartagnan.utils.equivalence.EquivalenceClass;
import com.dat3m.dartagnan.verification.Context;
import com.dat3m.dartagnan.wmm.Wmm;
import com.dat3m.dartagnan.wmm.analysis.RelationAnalysis;
import com.dat3m.dartagnan.wmm.axiom.Axiom;
import com.dat3m.dartagnan.wmm.relation.Relation;
import com.dat3m.dartagnan.wmm.utils.Tuple;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dat3m.dartagnan.configuration.OptionNames.BREAK_SYMMETRY_BY_SYNC_DEGREE;
import static com.dat3m.dartagnan.configuration.OptionNames.BREAK_SYMMETRY_ON_RELATION;

@Options
public class SymmetryEncoder implements Encoder {

    private static final Logger logger = LogManager.getLogger(SymmetryEncoder.class);

    private final EncodingContext context;
    private final List<Axiom> axioms;
    private final ThreadSymmetry symm;
    private final BranchEquivalence branchEq;
    private final Relation rel;
    private final boolean breakOnControlFlow;

    @Option(name = BREAK_SYMMETRY_ON_RELATION,
            description = "The relation which should be used to break symmetry." +
            "Empty, if symmetry shall not be encoded." +
            "Special value \"_cf\" allows for breaking on control flow rather than relations.",
            secure = true)
    private String symmBreakRelName = "";

    @Option(name = BREAK_SYMMETRY_BY_SYNC_DEGREE,
            description = "Orders the relation edges to break on based on their synchronization strength.",
            secure = true)
    private boolean breakBySyncDegree = true;

    // =====================================================================

    private SymmetryEncoder(EncodingContext c, Wmm m, Context a) {
        context = c;
        axioms = List.copyOf(m.getAxioms());
        symm = c.getAnalysisContext().get(ThreadSymmetry.class);
        branchEq = a.requires(BranchEquivalence.class);
        a.requires(RelationAnalysis.class);

        if (symmBreakRelName.isEmpty()) {
            logger.info("Symmetry breaking disabled.");
            this.rel = null;
            this.breakOnControlFlow = false;
        } else if (symmBreakRelName.equals("_cf")) {
            logger.info("Breaking symmetry on control flow.");
            this.rel = null;
            this.breakOnControlFlow = true;
        } else {
            this.rel = c.getTask().getMemoryModel().getRelation(symmBreakRelName);
            this.breakOnControlFlow = false;
            if (this.rel == null) {
                logger.warn("The wmm has no relation named {} to break symmetry on." +
                        " Symmetry breaking was disabled.", symmBreakRelName);
            } else {
                logger.info("Breaking symmetry on relation: " + symmBreakRelName);
                logger.info("Breaking by sync degree: " + breakBySyncDegree);
            }
        }
    }

    public static SymmetryEncoder withContext(EncodingContext context, Wmm memoryModel, Context analysisContext) throws InvalidConfigurationException {
        SymmetryEncoder encoder = new SymmetryEncoder(context, memoryModel, analysisContext);
        context.getTask().getConfig().inject(encoder);
        return encoder;
    }

    @Override
    public void initializeEncoding(SolverContext context) { }

    public BooleanFormula encodeFullSymmetry() {
        final BooleanFormulaManager bmgr = context.getBooleanFormulaManager();
        BooleanFormula enc = bmgr.makeTrue();
        if (rel == null && !breakOnControlFlow) {
            return enc;
        }

        for (EquivalenceClass<Thread> symmClass : symm.getNonTrivialClasses()) {
            enc = bmgr.and(enc, breakOnControlFlow ?
                    encodeCfSymmetryClass(symmClass) :
                    encodeRelSymmetryClass(symmClass)
            );
        }
        return enc;
    }

    private BooleanFormula encodeRelSymmetryClass(EquivalenceClass<Thread> symmClass) {
        Preconditions.checkArgument(symmClass.getEquivalence() == symm,
                "Symmetry class belongs to unexpected symmetry relation.");
        Preconditions.checkState(rel != null, "Relation to break symmetry on is NULL");
        final Relation rel = this.rel;
        final BooleanFormulaManager bmgr = context.getBooleanFormulaManager();
        final Thread rep = symmClass.getRepresentative();
        final List<Thread> symmThreads = new ArrayList<>(symmClass);
        symmThreads.sort(Comparator.comparingInt(Thread::getId));


        // ===== Construct row =====
        //IMPORTANT: Each thread writes to its own special location for the purpose of starting/terminating threads
        // These need to get skipped.
        Thread t1 = symmThreads.get(0);
        List<Tuple> t1Tuples = new ArrayList<>();
        for (Tuple t : rel.getMaxTupleSet()) {
            Event a = t.getFirst();
            Event b = t.getSecond();
            if (!a.is(Tag.C11.PTHREAD) && !b.is(Tag.C11.PTHREAD)
                    && a.getThread() == t1 && symmClass.contains(b.getThread())) {
                t1Tuples.add(t);
            }
        }
        sort(t1Tuples);

        // Construct symmetric rows
        BooleanFormula enc = bmgr.makeTrue();
        for (int i = 1; i < symmThreads.size(); i++) {
            Thread t2 = symmThreads.get(i);
            Function<Event, Event> p = symm.createTransposition(t1, t2);
            List<Tuple> t2Tuples = t1Tuples.stream().map(t -> t.permute(p)).collect(Collectors.toList());

            List<BooleanFormula> r1 = t1Tuples.stream().map(t -> context.edge(rel, t)).collect(Collectors.toList());
            List<BooleanFormula> r2 = t2Tuples.stream().map(t -> context.edge(rel, t)).collect(Collectors.toList());
            final String id = "_" + rep.getId() + "_" + i;
            enc = bmgr.and(enc, encodeLexLeader(id, r2, r1, context)); // r1 >= r2

            t1 = t2;
            t1Tuples = t2Tuples;
        }

        return enc;
    }

    private BooleanFormula encodeCfSymmetryClass(EquivalenceClass<Thread> symmClass) {
        Preconditions.checkArgument(symmClass.getEquivalence() == symm,
                "Symmetry class belongs to unexpected symmetry relation.");
        final BooleanFormulaManager bmgr = context.getBooleanFormulaManager();
        final Thread rep = symmClass.getRepresentative();
        final List<Thread> symmThreads = new ArrayList<>(symmClass);
        symmThreads.sort(Comparator.comparingInt(Thread::getId));


        // ===== Construct row =====
        Thread t1 = symmThreads.get(0);
        List<Event> t1BranchRoots = new ArrayList<>();
        for (BranchEquivalence.Class c : branchEq.getAllEquivalenceClasses()) {
            if (c == branchEq.getInitialClass() || c == branchEq.getUnreachableClass()) {
                continue;
            }
            if (c.getRepresentative().getThread().equals(t1)) {
                t1BranchRoots.add(c.getRepresentative());
            }
        }
        t1BranchRoots.sort(Comparator.naturalOrder());

        // Construct symmetric rows
        BooleanFormula enc = bmgr.makeTrue();
        for (int i = 1; i < symmThreads.size(); i++) {
            Thread t2 = symmThreads.get(i);
            Function<Event, Event> perm = symm.createTransposition(t1, t2);
            List<Event> t2BranchRoots = t1BranchRoots.stream().map(perm).collect(Collectors.toList());

            List<BooleanFormula> r1 = t1BranchRoots.stream().map(context::execution).collect(Collectors.toList());
            List<BooleanFormula> r2 = t2BranchRoots.stream().map(context::execution).collect(Collectors.toList());
            final String id = "_" + rep.getId() + "_" + i;
            enc = bmgr.and(enc, encodeLexLeader(id, r2, r1, context)); // r1 >= r2

            t1 = t2;
            t1BranchRoots = t2BranchRoots;
        }

        return enc;
    }

    private void sort(List<Tuple> row) {
        if (!breakBySyncDegree) {
            // ===== Natural order =====
            row.sort(Comparator.naturalOrder());
            return;
        }

        // ====== Sync-degree based order ======

        // Setup of data structures
        Set<Event> inEvents = new HashSet<>();
        Set<Event> outEvents = new HashSet<>();
        for (Tuple t : row) {
            inEvents.add(t.getFirst());
            outEvents.add(t.getSecond());
        }

        Map<Event, Integer> combInDegree = new HashMap<>(inEvents.size());
        Map<Event, Integer> combOutDegree = new HashMap<>(outEvents.size());

        for (Event e : inEvents) {
            int syncDeg = axioms.stream()
                    .mapToInt(ax -> ax.getRelation().getMinTupleSet().getBySecond(e).size() + 1).max().orElse(0);
            combInDegree.put(e, syncDeg);
        }
        for (Event e : outEvents) {
            int syncDec = axioms.stream()
                    .mapToInt(ax -> ax.getRelation().getMinTupleSet().getByFirst(e).size() + 1).max().orElse(0);
            combOutDegree.put(e, syncDec);
        }

        // Sort by sync degrees
        row.sort(Comparator.<Tuple>comparingInt(t -> combInDegree.get(t.getFirst()) * combOutDegree.get(t.getSecond())).reversed());
    }

    // ========================= Static utility ===========================


    /*
        Encodes that any assignment obeys "r1 <= r2" where the order is
        the lexicographic order based on "false < true".
        In other words, for all assignments to the variables of r1/r2,
        the first time r1(i) and r2(i) get different truth values,
        we will have r1(i) = FALSE and r2(i) = TRUE.

        NOTE: Creates extra variables named "yi_<uniqueIdent>" which can cause conflicts if
              <uniqueIdent> is not uniquely used.
    */
    public static BooleanFormula encodeLexLeader(String uniqueIdent, List<BooleanFormula> r1, List<BooleanFormula> r2, EncodingContext context) {
        Preconditions.checkArgument(r1.size() == r2.size());
        BooleanFormulaManager bmgr = context.getBooleanFormulaManager();
        // Return TRUE if there is nothing to encode
        if(r1.isEmpty()) {
        	return bmgr.makeTrue();
        }
        final int size = r1.size();
        final String suffix = "_" + uniqueIdent;

        // We interpret the variables of <ri> as x1(ri), ..., xn(ri).
        // We create helper variables y0_suffix, ..., y(n-1)_suffix (note the index shift compared to xi)
        // xi gets related to y(i-1) and yi

        BooleanFormula ylast = bmgr.makeVariable("y0" + suffix); // y(i-1)
        BooleanFormula enc = bmgr.equivalence(ylast, bmgr.makeTrue());
        // From x1 to x(n-1)
        for (int i = 1; i < size; i++) {
            BooleanFormula y = bmgr.makeVariable("y" + i + suffix); // yi
            BooleanFormula a = r1.get(i-1); // xi(r1)
            BooleanFormula b = r2.get(i-1); // xi(r2)
            enc = bmgr.and(
                    enc,
                    bmgr.or(y, bmgr.not(ylast), bmgr.not(a)), // (see below)
                    bmgr.or(y, bmgr.not(ylast), b),           // "y(i-1) implies ((xi(r1) >= xi(r2))  =>  yi)"
                    bmgr.or(bmgr.not(ylast), bmgr.not(a), b)  // "y(i-1) implies (xi(r1) <= xi(r2))"
                    // NOTE: yi = TRUE means the prefixes (x1, x2, ..., xi) of the rows r1/r2 are equal
                    //       yi = FALSE means that no conditions are imposed on xi
                    // The first point, where y(i-1) is TRUE but yi is FALSE, is the breaking point
                    // where xi(r1) < xi(r2) holds (afterwards all yj (j >= i+1) are unconstrained and can be set to
                    // FALSE by the solver)
            );
            ylast = y;
        }
        // Final iteration for xn is handled differently as there is no variable yn anymore.
        BooleanFormula a = r1.get(size-1);
        BooleanFormula b = r2.get(size-1);
        enc = bmgr.and(enc, bmgr.or(bmgr.not(ylast), bmgr.not(a), b));


        return enc;
    }
}
