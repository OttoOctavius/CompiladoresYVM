package splitstack;

import java.util.ArrayList;
import java.util.List;

import static splitstack.Bytecode.BR;
import static splitstack.Bytecode.BRF;
import static splitstack.Bytecode.BRT;
import static splitstack.Bytecode.CALL;
import static splitstack.Bytecode.GLOAD;
import static splitstack.Bytecode.GSTORE;
import static splitstack.Bytecode.HALT;
import static splitstack.Bytecode.IADD;
import static splitstack.Bytecode.ICONST;
import static splitstack.Bytecode.IEQ;
import static splitstack.Bytecode.ILT;
import static splitstack.Bytecode.IMUL;
import static splitstack.Bytecode.ISUB;
import static splitstack.Bytecode.LOAD;
import static splitstack.Bytecode.POP;
import static splitstack.Bytecode.PRINT;
import static splitstack.Bytecode.RET;
import static splitstack.Bytecode.STORE;

/** A simple stack-based interpreter */
public class VM {
    public static final int DEFAULT_STACK_SIZE = 1000;
    public static final int DEFAULT_CALL_STACK_SIZE = 1000;
    public static final int FALSE = 0;
    public static final int TRUE = 1;

    // registers
    int ip;             // instruction pointer register
    int sp = -1;  		// stack pointer register
    int callsp = -1;    // call stack pointer register

    int startip = 0;	// where execution begins

    // memory
    int[] code;         // word-addressable code memory but still bytecodes.
    int[] globals;      // global variable space
    int[] stack;		// Operand stack, grows upwards
    Context[] callstack;// call stack, grows upwards

    public boolean trace = false;

    public VM(int[] code, int startip, int nglobals) {
        this.code = code;
        this.startip = startip;
        globals = new int[nglobals];
        stack = new int[DEFAULT_STACK_SIZE];
        callstack = new Context[DEFAULT_CALL_STACK_SIZE];
    }

    public void exec() {
        ip = startip;
        cpu();
    }

    /** Simulate the fetch-decode execute cycle */
    protected void cpu() {
        int opcode = code[ip];
        int a,b,addr,regnum;
        while (opcode!= HALT && ip < code.length) {
            if ( trace ) System.err.printf("%-35s", disInstr());
            ip++; //jump to next instruction or to operand
            switch (opcode) {
                case IADD:
                    b = stack[sp--];   			// 2nd opnd at top of stack
                    a = stack[sp--]; 			// 1st opnd 1 below top
                    stack[++sp] = a + b;      	// push result
                    break;
                case ISUB:
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = a - b;
                    break;
                case IMUL:
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = a * b;
                    break;
                case ILT :
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = (a < b) ? TRUE : FALSE;
                    break;
                case IEQ :
                    b = stack[sp--];
                    a = stack[sp--];
                    stack[++sp] = (a == b) ? TRUE : FALSE;
                    break;
                case BR :
                    ip = code[ip++];
                    break;
                case BRT :
                    addr = code[ip++];
                    if ( stack[sp--]==TRUE ) ip = addr;
                    break;
                case BRF :
                    addr = code[ip++];
                    if ( stack[sp--]==FALSE ) ip = addr;
                    break;
                case ICONST:
                    stack[++sp] = code[ip++]; // push operand
                    break;
                case LOAD : // load local or arg
                    regnum = code[ip++];
                    stack[++sp] = callstack[callsp].locals[regnum];
                    break;
                case GLOAD :// load from global memory
                    addr = code[ip++];
                    stack[++sp] = globals[addr];
                    break;
                case STORE :
                    regnum = code[ip++];
                    callstack[callsp].locals[regnum] = stack[sp--];
                    break;
                case GSTORE :
                    addr = code[ip++];
                    globals[addr] = stack[sp--];
                    break;
                case PRINT :
                    System.out.println(stack[sp--]);
                    break;
                case POP:
                    --sp;
                    break;
                case CALL :
                    // expects all args on stack
                    addr = code[ip++];			// target addr of function
                    int nargs = code[ip++];		// how many args got pushed
                    callstack[++callsp] = new Context(ip,nargs);
                    // copy args into new context
                    int firstarg = sp-nargs+1;
                    for (int i=0; i<nargs; i++) {
                        callstack[callsp].locals[i] = stack[firstarg+i];
                    }
                    sp -= nargs;
                    ip = addr;					// jump to function
                    break;
                case RET:
                    ip = callstack[callsp--].returnip;
                    break;
                default :
                    throw new Error("invalid opcode: "+opcode+" at ip="+(ip-1));
            }
            if ( trace ) System.err.println(stackString());
            opcode = code[ip];
        }
        if ( trace ) System.err.printf("%-35s", disInstr());
        if ( trace ) System.err.println(stackString());
        if ( trace ) dumpDataMemory();
    }

    protected String stackString() {
        StringBuilder buf = new StringBuilder();
        buf.append("stack=[");
        for (int i = 0; i <= sp; i++) {
            int o = stack[i];
            buf.append(" ");
            buf.append(o);
        }
        buf.append(" ]");
        return buf.toString();
    }

    protected String disInstr() {
        int opcode = code[ip];
        String opName = Bytecode.instructions[opcode].name;
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%04d:\t%-11s", ip, opName));
        int nargs = Bytecode.instructions[opcode].n;
        if ( nargs>0 ) {
            List<String> operands = new ArrayList<String>();
            for (int i=ip+1; i<=ip+nargs; i++) {
                operands.add(String.valueOf(code[i]));
            }
            for (int i = 0; i<operands.size(); i++) {
                String s = operands.get(i);
                if ( i>0 ) buf.append(", ");
                buf.append(s);
            }
        }
        return buf.toString();
    }

    protected void dumpDataMemory() {
        System.err.println("Data memory:");
        int addr = 0;
        for (int o : globals) {
            System.err.printf("%04d: %s\n", addr, o);
            addr++;
        }
        System.err.println();
    }
}