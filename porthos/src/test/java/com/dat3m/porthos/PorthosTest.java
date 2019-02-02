package com.dat3m.porthos;

import com.dat3m.dartagnan.Dartagnan;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.dat3m.dartagnan.wmm.utils.Mode;
import com.dat3m.dartagnan.parsers.cat.ParserCat;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.wmm.Wmm;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PorthosTest {

    private static final String CAT_RESOURCE_PATH = "../";
    private static final String BENCHMARKS_RESOURCE_PATH = "../";

    @Parameterized.Parameters(name = "{index}: {0} {2} -> {3} steps={6} relax={7} idl={8}")
    public static Iterable<Object[]> data() throws IOException {

        Wmm wmmSc = new ParserCat().parse(CAT_RESOURCE_PATH + "cat/sc.cat", Arch.NONE);
        Wmm wmmTso = new ParserCat().parse(CAT_RESOURCE_PATH + "cat/tso.cat", Arch.TSO);
        Wmm wmmPpc = new ParserCat().parse(CAT_RESOURCE_PATH + "cat/power.cat", Arch.POWER);
        Wmm wmmArm = new ParserCat().parse(CAT_RESOURCE_PATH + "cat/arm.cat", Arch.ARM);

        return Arrays.asList(new Object[][] {
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.RELAX },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.IDL },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", false, "sc", "tso", wmmSc, wmmTso, 2, Mode.LFP},

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.RELAX },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.IDL },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "power", wmmTso, wmmPpc, 2, Mode.LFP},

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.RELAX },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.IDL },

                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Bakery.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Burns.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Dekker.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Lamport.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Parker.pts", false, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Peterson.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP},
                { BENCHMARKS_RESOURCE_PATH + "benchmarks/Szymanski.pts", true, "tso", "arm", wmmTso, wmmArm, 2, Mode.LFP}
        });
    }

    private String input;
    private boolean expected;
    private Arch source;
    private Arch target;
    private Wmm sourceWmm;
    private Wmm targetWmm;
    private int steps;
    private Mode mode;

    public PorthosTest(String input, boolean expected, Arch source, Arch target, Wmm sourceWmm, Wmm targetWmm, int steps, Mode mode) {
        this.input = input;
        this.expected = expected;
        this.source = source;
        this.target = target;
        this.sourceWmm = sourceWmm;
        this.targetWmm = targetWmm;
        this.steps = steps;
        this.mode = mode;
    }

    @Test
    public void test() {
        try {
            Program program = Dartagnan.parseProgram(input);
            if (program.getAss() != null) {
                Context ctx = new Context();
                Solver s1 = ctx.mkSolver(ctx.mkTactic(Dartagnan.TACTIC));
                Solver s2 = ctx.mkSolver(ctx.mkTactic(Dartagnan.TACTIC));
                PorthosResult result = Porthos.testProgram(s1, s2, ctx,program,
                        source, target, sourceWmm, targetWmm, steps, mode, false);
                assertEquals(expected, result.getIsPortable());
            }
        } catch (IOException e){
            fail("Missing resource file");
        }
    }
}
