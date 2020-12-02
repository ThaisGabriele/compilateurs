

import java.io.*;
import java.io.File; 
import java.io.IOException;
 


public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _numero = 2;
	public static final int _strConst = 3;
	public static final int _inicio = 4;
	public static final int _fim = 5;
	public static final int _variavel = 6;
	public static final int _caso = 7;
	public static final int _enquanto = 8;
	public static final int _repita = 9;
	public static final int _leia = 10;
	public static final int _escreva = 11;
	public static final int _retorne = 12;
	public static final int _constante = 13;
	public static final int _novo = 14;
	public static final int _algoritmo = 15;
	public static final int _se = 16;
	public static final int _senao = 17;
	public static final int _entao = 18;
	public static final int _fimse = 19;
	public static final int _para = 20;
	public static final int _ate = 21;
	public static final int _faca = 22;
	public static final int _fimpara = 23;
	public static final int _maior = 24;
	public static final int _menor = 25;
	public static final int _maiorIgual = 26;
	public static final int _menorIgual = 27;
	public static final int _diferente = 28;
	public static final int _resto = 29;
	public static final int maxT = 51;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	String beginDocument (String s) {
    return "<html style='font: 18px Helvetica sans-serif><body>" + s + "</body></html>";
} 

String ident(String s, int margin) {
    return "<div style='margin-left:" + margin +"px'>" + s + "</div>";
}

String toBlueBold(String s) { 
    return "</span><br/><span style='color: #0000ff;'><strong>"+ s + "</strong></span>";    
}
 
String toRedBold(String s) { 
    return  "<span style='color: #ff0000;'><strong>" + s + "</strong></span>";    
}

String toBlackBold(String s) { 
    return  "<strong>" + s +"</strong>";
}
 
void appendFile(String saidaFormatada) {
    try {
        String str = saidaFormatada;
        BufferedWriter writer = new BufferedWriter(new FileWriter("index.html", true));
        writer.append(str);
    
        writer.close();
        
    } catch (IOException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }
}
 


	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void UPortugol() {
		Bloco();
		while (la.kind == 30) {
			Bloco();
		}
	}

	void Bloco() {
		String res; String saidaFormatada  = "";
		res = DeclProc();
		saidaFormatada = saidaFormatada + res;  
		while (StartOf(1)) {
			res = Instrucao();
			saidaFormatada = saidaFormatada + res; 
		}
		appendFile(beginDocument(saidaFormatada)); 
	}

	String  DeclProc() {
		String  res;
		res = ""; String v1, v2, t1 = ""; 
		Expect(30);
		res = toBlueBold(t.val); 
		Expect(1);
		res = res + " " + toBlackBold(t.val); 
		Expect(31);
		res = res + t.val; 
		v1  = Parametros();
		res = res + v1; 
		Expect(32);
		res = res + t.val; 
		if (la.kind == 33) {
			Get();
			res = res +  t.val; 
			t1  = Tipo();
			res = res + t1; 
		}
		Expect(4);
		res = res + toBlackBold(ident(t.val,20)); 
		while (StartOf(1)) {
			v2 = Instrucao();
			res = res +  ident(v2,40) ; 
		}
		Expect(5);
		res = res + toRedBold(ident(t.val,20)); 
		return res;
	}

	String  Instrucao() {
		String  res;
		res = "";  String v1, v2,v3,v4,v5,v6 = "";  
		if (StartOf(2)) {
			v1 = DeclVar();
			res = res + v1; 
		} else if (la.kind == 16) {
			v3 = Condicao();
			res = res + v3; 
			Expect(37);
			res = res + toBlackBold(ident(t.val,20)); 
		} else if (la.kind == 12) {
			v4 = Retorno();
			res = res + v4; 
		} else if (la.kind == 11) {
			v5 = Escreva();
			res = res + v5; 
		} else if (la.kind == 20) {
			v6 = Para();
			res = res + v6; 
			Expect(38);
			res = res + toBlackBold(ident(t.val,20)); 
		} else SynErr(52);
		return res;
	}

	String  Parametros() {
		String  res;
		res = ""; 
		while (la.kind == 1) {
			Get();
			res = res + " " + toBlackBold(t.val); 
			Expect(33);
			res = res + " " + t.val; 
			Expect(34);
			res = res + toRedBold(t.val); 
			if (la.kind == 35 || la.kind == 36) {
				if (la.kind == 35) {
					Get();
				} else {
					Get();
				}
			}
			res = res + t.val; 
		}
		return res;
	}

	String  Tipo() {
		String  res;
		res = ""; 
		Expect(34);
		res = res + " " + toBlackBold(t.val); 
		return res;
	}

	String  DeclVar() {
		String  res;
		res = ""; String v1, v2 = ""; 
		if (StartOf(3)) {
			v1 = ComAtribuicao();
			Expect(39);
			res = res + v1; 
		} else if (la.kind == 6) {
			Get();
			v2 = SemAtribuicao();
			res = res + " " + v2;
		} else SynErr(53);
		return res;
	}

	String  Condicao() {
		String  res;
		res = " ";  String v1, v2,v3=" ";
		Expect(16);
		res =  t.val + " ";
		v1 = Expr();
		res = " " + v1 + " "; 
		switch (la.kind) {
		case 24: {
			Get();
			break;
		}
		case 25: {
			Get();
			break;
		}
		case 27: {
			Get();
			break;
		}
		case 26: {
			Get();
			break;
		}
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			break;
		}
		default: SynErr(54); break;
		}
		v3 = Expr();
		res = res + " " + v3 + " " ; 
		Expect(18);
		res = res + toBlackBold(t.val); 
		v2 = Instrucao();
		res = res + ident(v2,40); 
		if (la.kind == 17) {
			Get();
			v2 = Instrucao();
			res = res + ident(v2,40); 
		}
		return res;
	}

	String  Retorno() {
		String  res;
		res = "";  String v1=""; 
		Expect(12);
		res = res + toBlackBold(ident(t.val,20)); 
		v1 = Expr();
		res = res + v1; 
		Expect(39);
		res = res + t.val;
		return res;
	}

	String  Escreva() {
		String  res;
		res = "";String v1,v2="";
		Expect(11);
		res = res + t.val;
		Expect(31);
		res = res + t.val;
		if (StartOf(3)) {
			v1 = Expr();
			res = res + v1;
			if (la.kind == 35) {
				Get();
				res = res + t.val;
				Expect(3);
				res = res + t.val;
			}
		} else if (la.kind == 3) {
			Get();
			res = res + t.val;
		} else SynErr(55);
		Expect(41);
		res = res + t.val;
		return res;
	}

	String  Para() {
		String  res;
		res = "";String v1,v2,v3="";
		Expect(20);
		v1 = ComAtribuicao();
		System.out.println(res); res = res + toBlackBold(v1);
		Expect(21);
		v2 = Expr();
		res = res + v2 + " ";
		Expect(22);
		res = res + t.val;
		v3 = Instrucao();
		res = res + ident(v3,20);
		return res;
	}

	String  Expr() {
		String  res;
		res = "";  String v1, v2="";
		if (la.kind == 44) {
			Get();
		}
		res = res + t.val;
		v1 = Termo();
		res = res + v1; 
		while (la.kind == 44 || la.kind == 45) {
			if (la.kind == 45) {
				Get();
				res = res + t.val; 
				v2 = Termo();
				res = res + v2; 
			} else {
				Get();
				res = res + t.val; 
				v2 = Termo();
				res = res + v2; 
			}
		}
		return res;
	}

	String  ComAtribuicao() {
		String  res;
		res = "";  String v1,v2=""; 
		v1 = Expr();
		res = res + v1;
		Expect(40);
		v2 = Expr();
		res = res + v2; 
		return res;
	}

	String  SemAtribuicao() {
		String  res;
		res = ""; String v1=""; 
		v1 = Expr();
		res = v1 + " "; 
		while (la.kind == 35) {
			Get();
			v1 = Expr();
			res =  res + v1 + " "; 
		}
		Expect(33);
		res = res + t.val;
		Expect(34);
		res = res + t.val;
		Expect(39);
		res = res + t.val;
		return res;
	}

	String  FatorVetor() {
		String  res;
		res = "";  String v1, v2=""; 
		if (la.kind == 42) {
			Get();
			v1 = Expr();
			res =  " " + toBlackBold(" " + v1); 
			Expect(43);
			res = res + t.val ; 
		} else if (la.kind == 14) {
			Get();
			res = res + toBlackBold(t.val); 
			Expect(1);
			res = res + toBlackBold(t.val); 
			if (StartOf(3)) {
				v1 = Expr();
				res = res + v1; 
			} else if (la.kind == 48) {
				v2 = Inicializa();
				res = res + v2; 
			} else SynErr(56);
		} else SynErr(57);
		return res;
	}

	String  Inicializa() {
		String  res;
		res = ""; 
		Expect(48);
		res = res + t.val; 
		while (la.kind == 2) {
			Get();
			res = res + toBlackBold(t.val); 
			if (la.kind == 35) {
				Get();
			}
			res = res + t.val; 
		}
		Expect(49);
		res = res + t.val; 
		return res;
	}

	String  Termo() {
		String  res;
		res = "";  String v1, v2="";
		v1 = Fator();
		res = res + v1; 
		while (la.kind == 46 || la.kind == 47) {
			if (la.kind == 46) {
				Get();
				res = res + t.val; 
				v2 = Fator();
				res = res + v2; 
			} else {
				Get();
				res = res + t.val; 
				v2 = Fator();
				res = res + v2; 
			}
		}
		return res;
	}

	String  Fator() {
		String  res;
		res = "";  String v1,v2=""; 
		if (la.kind == 1) {
			Get();
			res = res + toBlackBold(t.val); 
			if (la.kind == 14 || la.kind == 42) {
				v1 = FatorVetor();
				res = res + v1; 
			}
		} else if (la.kind == 2) {
			Get();
			res = res + toBlackBold(t.val); 
		} else if (la.kind == 50) {
			v2 = TamanhoVet();
			res = res + v2; 
		} else SynErr(58);
		return res;
	}

	String  TamanhoVet() {
		String  res;
		res = ""; String v1=""; 
		Expect(50);
		res = res + t.val; 
		Expect(31);
		res = res + t.val; 
		if (la.kind == 2) {
			Get();
		} else if (StartOf(3)) {
			v1 = Expr();
		} else SynErr(59);
		res = res + v1; 
		Expect(32);
		res = res + t.val; 
		return res;
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		UPortugol();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,T,T,x, x,x,T,x, x,x,x,T, T,x,x,x, T,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,T,x, x},
		{x,T,T,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,T,x, x},
		{x,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,T,x, x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "numero expected"; break;
			case 3: s = "strConst expected"; break;
			case 4: s = "inicio expected"; break;
			case 5: s = "fim expected"; break;
			case 6: s = "variavel expected"; break;
			case 7: s = "caso expected"; break;
			case 8: s = "enquanto expected"; break;
			case 9: s = "repita expected"; break;
			case 10: s = "leia expected"; break;
			case 11: s = "escreva expected"; break;
			case 12: s = "retorne expected"; break;
			case 13: s = "constante expected"; break;
			case 14: s = "novo expected"; break;
			case 15: s = "algoritmo expected"; break;
			case 16: s = "se expected"; break;
			case 17: s = "senao expected"; break;
			case 18: s = "entao expected"; break;
			case 19: s = "fimse expected"; break;
			case 20: s = "para expected"; break;
			case 21: s = "ate expected"; break;
			case 22: s = "faca expected"; break;
			case 23: s = "fimpara expected"; break;
			case 24: s = "maior expected"; break;
			case 25: s = "menor expected"; break;
			case 26: s = "maiorIgual expected"; break;
			case 27: s = "menorIgual expected"; break;
			case 28: s = "diferente expected"; break;
			case 29: s = "resto expected"; break;
			case 30: s = "\"procedimento\" expected"; break;
			case 31: s = "\"(\" expected"; break;
			case 32: s = "\")\" expected"; break;
			case 33: s = "\":\" expected"; break;
			case 34: s = "\"inteiro\" expected"; break;
			case 35: s = "\",\" expected"; break;
			case 36: s = "\"[]\" expected"; break;
			case 37: s = "\"fimse;\" expected"; break;
			case 38: s = "\"fimpara;\" expected"; break;
			case 39: s = "\";\" expected"; break;
			case 40: s = "\"=\" expected"; break;
			case 41: s = "\");\" expected"; break;
			case 42: s = "\"[\" expected"; break;
			case 43: s = "\"]\" expected"; break;
			case 44: s = "\"-\" expected"; break;
			case 45: s = "\"+\" expected"; break;
			case 46: s = "\"*\" expected"; break;
			case 47: s = "\"/\" expected"; break;
			case 48: s = "\"{\" expected"; break;
			case 49: s = "\"}\" expected"; break;
			case 50: s = "\"tamanho\" expected"; break;
			case 51: s = "??? expected"; break;
			case 52: s = "invalid Instrucao"; break;
			case 53: s = "invalid DeclVar"; break;
			case 54: s = "invalid Condicao"; break;
			case 55: s = "invalid Escreva"; break;
			case 56: s = "invalid FatorVetor"; break;
			case 57: s = "invalid FatorVetor"; break;
			case 58: s = "invalid Fator"; break;
			case 59: s = "invalid TamanhoVet"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
