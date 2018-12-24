#include <iostream>
#include <fstream>
#include <vector>
#include <string>

using namespace std;

bool decodificarflag;
char expresion;
unsigned char p = 0;
unsigned int ip = 0;

vector<unsigned char> stack;
string mem;


bool reducida(unsigned char exp){
    return true;
}

void block(){
    char temp;
    while(mem[ip]!=']'){
        if( mem[ip] == '[' )
            block();
        ip++;
    }
}

void fetch(){
    expresion = mem[ip];
    decodificarflag = !reducida(expresion);
}

void decode(){
    if( decodificarflag )
        ; //transformar codigoS
}

void run(){
    switch(expresion){
        case '+':
            stack[p]++;
            break;
        case '-':
            stack[p]--;
            break;
        case '>':
            p++; break;
        case '<':
            p--; break;
        case ',':
            cin >> stack[p];
            break;
        case '.':
            cout << stack[p];
            break;
    }
    ip++;
}

int main(){
    ifstream myfile;
    myfile.open ("examplein.bf");
    myfile >> mem;
    myfile.close();

    stack.reserve(255);
    while(mem[ip]!= 0){
        fetch();
        decode();
        run();
    }
    return 0;
}