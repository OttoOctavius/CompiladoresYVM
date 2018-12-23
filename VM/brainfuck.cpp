#include <iostream>
#include <fstream>
#include <vector>
#include <string>

using namespace std;

bool decodificarflag;
unsigned char exp=1;
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
    exp = mem[ip];
    decodificarflag = !reducida(exp);
}

void decode(){
    if( decodificarflag )
        ; //transformar codigoS
}

void run(){
    switch(exp){
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
    myfile.open ("example.bf");
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