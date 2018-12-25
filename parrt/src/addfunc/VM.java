package addfunc;

import java.util.ArrayList;
import java.util.List;

import static addfunc.Bytecode.BR;
import static addfunc.Bytecode.BRF;
import static addfunc.Bytecode.BRT;
import static addfunc.Bytecode.CALL;
import static addfunc.Bytecode.GLOAD;
import static addfunc.Bytecode.GSTORE;
import static addfunc.Bytecode.HALT;
import static addfunc.Bytecode.IADD;
import static addfunc.Bytecode.ICONST;
import static addfunc.Bytecode.IEQ;
import static addfunc.Bytecode.ILT;
import static addfunc.Bytecode.IMUL;
import static addfunc.Bytecode.ISUB;
import static addfunc.Bytecode.LOAD;
import static addfunc.Bytecode.POP;
import static addfunc.Bytecode.PRINT;
import static addfunc.Bytecode.RET;
import static addfunc.Bytecode.STORE;

/** A simple stack-based interpreter */
public class VM {
	public static final int DEFAULT_STACK_SIZE = 1000;
	public static final int FALSE = 0;
	public static final int TRUE = 1;

	// registers
	int ip;             // instruction pointer register
	int sp = -1;  		// stack pointer register
	int fp = -1;        // frame pointer register

	int startip = 0;	// where execution begins

	// memory
	int[] code;         // word-addressable code memory but still bytecodes.
	int[] globals;      // global variable space
	int[] stack;		// Operand stack, grows upwards

	public boolean trace = false;

	public VM(int[] code, int startip, int nglobals) {
		this.code = code;
		this.startip = startip;
		globals = new int[nglobals];
		stack = new int[DEFAULT_STACK_SIZE];
	}

	public void exec() {
		ip = startip;
		cpu();
	}

	/** Simulate the fetch-decode execute cycle */
	protected void cpu() {
		int opcode = code[ip];
		int a,b,addr,offset;
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
				case LOAD : // load local or arg; 1st local is fp+1, args are fp-3, fp-4, fp-5, ...
					offset = code[ip++];
					stack[++sp] = stack[fp+offset];
					break;
				case GLOAD :// load from global memory
					addr = code[ip++];
					stack[++sp] = globals[addr];
					break;
				case STORE :
					offset = code[ip++];
					stack[fp+offset] = stack[sp--];
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
					stack[++sp] = nargs;	 	// save num args
					stack[++sp] = fp;		 	// save fp
					stack[++sp] = ip;		 	// push return address
					fp = sp;					// fp points at ret addr on stack
					ip = addr;					// jump to function
					// code preamble of func must push space for locals
					break;
				case RET:
					int rvalue = stack[sp--];	// pop return value
					sp = fp;					// jump over locals to fp which points at ret addr
					ip = stack[sp--];			// pop return address, jump to it
					fp = stack[sp--];			// restore fp
					nargs = stack[sp--];		// how many args to throw away?
					sp -= nargs;				// pop args
					stack[++sp] = rvalue;	  	// leave result on stack
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