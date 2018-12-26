package splitstack;

import static splitstack.Bytecode.BRF;
import static splitstack.Bytecode.CALL;
import static splitstack.Bytecode.HALT;
import static splitstack.Bytecode.IADD;
import static splitstack.Bytecode.ICONST;
import static splitstack.Bytecode.ILT;
import static splitstack.Bytecode.ISUB;
import static splitstack.Bytecode.LOAD;
import static splitstack.Bytecode.PRINT;
import static splitstack.Bytecode.RET;

public class Fibonacci {
    /*
    def fib(n):
        if n < 2: return n
        return fib(n-2) + fib(n-1)
     */
    static int FIB_ADDRESS = 0;
    static int MAIN_ADDRESS = 28;
    static int[] fibonacci = {
//.def FIB: ARGS=1, LOCALS=0	ADDRESS
//	IF N < 2 RETURN N
            LOAD, 0,				// 0
            ICONST, 2,				// 2
            ILT,					// 4
            BRF, 10,				// 5
            LOAD, 0,				// 7
            RET,					// 9
//CONT:
//	RETURN FIB(N-2) + FIB(N-1)
            LOAD, 0,				// 10
            ICONST, 2,				// 12
            ISUB,					// 14
            CALL, FIB_ADDRESS, 1,   // 15
            LOAD, 0,				// 18
            ICONST, 1,				// 20
            ISUB,					// 22
            CALL, FIB_ADDRESS, 1,   // 23
            IADD,					// 26
            RET,					// 27
//.DEF MAIN: ARGS=0, LOCALS=0
// PRINT FIB(10)
            ICONST, 10,				// 28    <-- MAIN METHOD!
            CALL, FIB_ADDRESS, 1,   // 30
            PRINT,					// 33
            HALT					// 34
    };

    public static void main(String[] args) {
        VM vm = new VM(fibonacci, MAIN_ADDRESS, 0);
        vm.trace = true;
        vm.exec();
    }
}