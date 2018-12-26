package splitstack;

import static splitstack.Bytecode.BR;
import static splitstack.Bytecode.BRF;
import static splitstack.Bytecode.CALL;
import static splitstack.Bytecode.GLOAD;
import static splitstack.Bytecode.GSTORE;
import static splitstack.Bytecode.HALT;
import static splitstack.Bytecode.IADD;
import static splitstack.Bytecode.ICONST;
import static splitstack.Bytecode.ILT;
import static splitstack.Bytecode.IMUL;
import static splitstack.Bytecode.ISUB;
import static splitstack.Bytecode.LOAD;
import static splitstack.Bytecode.PRINT;
import static splitstack.Bytecode.RET;

public class Test {
    static int[] hello = {
            ICONST, 1,
            ICONST, 2,
            IADD,
            PRINT,
            HALT
    };

    /*
    iconst(1);
    iconst(2);
    iadd();
    print();
    halt();
     */

    static int[] loop = {
            // .GLOBALS 2; N, I
            // N = 10						ADDRESS
            ICONST, 10,				// 0
            GSTORE, 0,				// 2
            // I = 0
            ICONST, 0,				// 4
            GSTORE, 1,				// 6
            // WHILE I<N:
            // START (8):
            GLOAD, 1,				// 8
            GLOAD, 0,				// 10
            ILT,					// 12
            BRF, 24,				// 13
            //     I = I + 1
            GLOAD, 1,				// 15
            ICONST, 1,				// 17
            IADD,					// 19
            GSTORE, 1,				// 20
            BR, 8,					// 22
            // DONE (24):
            // PRINT "LOOPED "+N+" TIMES."
            HALT					// 24
    };

    static int fact = 0;
    static int[] factorial = {
//.def fact: ARGS=1, LOCALS=0		ADDRESS
//	IF N < 2 RETURN 1
            LOAD, 0,				// 0
            ICONST, 2,				// 2
            ILT,					// 4
            BRF, 10,				// 5
            ICONST, 1,				// 7
            RET,					// 9
//CONT:
//	RETURN N * FACT(N-1)
            LOAD, 0,				// 10
            LOAD, 0,				// 12
            ICONST, 1,				// 14
            ISUB,					// 16
            CALL, fact, 1,			// 17
            IMUL,					// 20
            RET,					// 21
//.DEF MAIN: ARGS=0, LOCALS=0
// PRINT FACT(1)
            ICONST, 5,				// 22    <-- MAIN METHOD!
            CALL, fact, 1,				// 24
            PRINT,					// 27
            HALT					// 28
    };

    public static void main(String[] args) {
        VM vm = new VM(factorial, 22, 0);
        vm.trace = true;
        vm.exec();
    }
}