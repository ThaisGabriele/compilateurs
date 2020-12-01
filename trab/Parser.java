

import java.io.*;
import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
 


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
	public static final int _para = 9;
	public static final int _repita = 10;
	public static final int _leia = 11;
	public static final int _escreva = 12;
	public static final int _retorne = 13;
	public static final int _constante = 14;
	public static final int _novo = 15;
	public static final int _algoritmo = 16;
	public static final int _se = 17;
	public static final int _senao = 18;
	public static final int _entao = 19;
	public static final int _fimse = 20;
	public static final int _maior = 21;
	public static final int _menor = 22;
	public static final int _maiorIgual = 23;
	public static final int _menorIgual = 24;
	public static final int _diferente = 25;
	public static final int _resto = 26;
	public static final int maxT = 46;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	String beginDocument (String s) {
    return "<html><body>" + s + "</body></html>";
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
		while (la.kind == 27) {
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
		System.out.println(saidaFormatada); appendFile(beginDocument(saidaFormatada)); 
	}

	String  DeclProc() {
		String  res;
		res = ""; String v1=""; String v2 = "";String t1 = ""; 
		Expect(27);
		res = toBlueBold(t.val); 
		Expect(1);
		res = res + " " + toBlackBold(t.val); 
		Expect(28);
		res = res + t.val; 
		v1  = Parametros();
		res = res + v1; 
		Expect(29);
		res = res + t.val; 
		if (la.kind == 30) {
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
		res = res + toRedBold(t.val); 
		return res;
	}

	String  Instrucao() {
		String  res;
		res = "";  String v1=""; String v2 = ""; String v3 = "";  String v4 = "";  
		if (la.kind == 1 || la.kind == 6) {
			v1 = DeclVar();
			res = res + v1; 
		} else if (la.kind == 17) {
			v3 = Condicao();
			res = res + v3; 
			Expect(34);
			res = res + toBlackBold(ident(t.val,20)); 
		} else if (la.kind == 13) {
			v4 = Retorno();
			res = res + v4; 
			Expect(35);
		} else SynErr(47);
		return res;
	}

	String  Parametros() {
		String  res;
		res = ""; 
		while (la.kind == 1) {
			Get();
			res = res + " " + toBlackBold(t.val); 
			Expect(30);
			res = res + " " + t.val; 
			Expect(31);
			res = res + toRedBold(t.val); 
			if (la.kind == 32 || la.kind == 33) {
				if (la.kind == 32) {
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
		Expect(31);
		res = res + " " + toBlackBold(t.val); 
		return res;
	}

	String  DeclVar() {
		String  res;
		res = ""; String v1=""; String v2 = ""; 
		if (la.kind == 1) {
			v1 = ComAtribuicao();
			res = res + v1; 
			Expect(35);
			res = res + t.val;
		} else if (la.kind == 6) {
			v2 = SemAtribuicao();
			res = res + v2;
			Expect(35);
			res = res + t.val;
		} else SynErr(48);
		return res;
	}

	String  Condicao() {
		String  res;
		res = " ";  String v1=" ";  String op=" "; String v12=" "; String v2=" ";
		Expect(17);
		v1 = Expr();
		res =  " " + toBlackBold(" " + v1 + " "); 
		switch (la.kind) {
		case 21: {
			Get();
			break;
		}
		case 22: {
			Get();
			break;
		}
		case 24: {
			Get();
			break;
		}
		case 23: {
			Get();
			break;
		}
		case 25: {
			Get();
			break;
		}
		case 26: {
			Get();
			break;
		}
		default: SynErr(49); break;
		}
		v12 = Expr();
		res = res + " " + v12 + " " ; 
		Expect(19);
		res = res + toBlackBold(t.val); 
		v2 = Instrucao();
		res = res + ident(v2,40); 
		if (la.kind == 18) {
			Get();
			v2 = Instrucao();
			res = res + ident(v2,40); 
		}
		return res;
	}

	String  Retorno() {
		String  res;
		res = "";  String v1=""; 
		Expect(13);
		res = res + toBlackBold(ident(t.val,20)); 
		v1 = Expr();
		res = res + v1; 
		return res;
	}

	String  Expr() {
		String  res;
		res = "";  String v1=""; String v2=""; 
		if (la.kind == 39) {
			Get();
		}
		res = res + t.val;
		v1 = Termo();
		res = res + v1; 
		while (la.kind == 39 || la.kind == 40) {
			if (la.kind == 40) {
				Get();
				v2 = Termo();
				res = res + v2; 
			} else {
				Get();
				v2 = Termo();
				res = res + v2; 
			}
		}
		return res;
	}

	String  ComAtribuicao() {
		String  res;
		res = "";  String v1=""; 
		Expect(1);
		res = res + " "+ toBlackBold(t.val);
		Expect(36);
		res = res + t.val;
		v1 = Expr();
		res = res + v1; 
		Expect(35);
		res = res + t.val;
		return res;
	}

	String  SemAtribuicao() {
		String  res;
		res = ""; String v1=""; 
		Expect(6);
		res = res + toBlackBold(t.val); 
		v1 = Expr();
		res = res + v1; 
		if (la.kind == 32) {
			Get();
		}
		res = res + t.val;
		if (la.kind == 30) {
			Get();
			Expect(31);
		}
		res = res + t.val;
		while (la.kind == 6) {
			Get();
			res = res + toBlackBold(t.val); 
			v1 = Expr();
			res = res + v1; 
			if (la.kind == 32) {
				Get();
			}
			res = res + t.val;
			if (la.kind == 30) {
				Get();
				Expect(31);
			}
			res = res + t.val;
		}
		return res;
	}

	String  FatorVetor() {
		String  res;
		res = "";  String v1=""; String v2=""; 
		if (la.kind == 37) {
			Get();
			v1 = Expr();
			res =  " " + toBlackBold(" " + v1); 
			Expect(38);
			res = res + t.val ; 
		} else if (la.kind == 15) {
			Get();
			res = res + toBlackBold(t.val); 
			Expect(1);
			res = res + toBlackBold(t.val); 
			if (la.kind == 1 || la.kind == 2 || la.kind == 39) {
				v1 = Expr();
				res = res + v1; 
			} else if (la.kind == 43) {
				v2 = Inicializa();
				res = res + v2; 
			} else SynErr(50);
		} else SynErr(51);
		return res;
	}

	String  Inicializa() {
		String  res;
		res = ""; 
		Expect(43);
		res = res + t.val; 
		while (la.kind == 2) {
			Get();
			res = res + toBlackBold(t.val); 
			if (la.kind == 32) {
				Get();
			}
			res = res + t.val; 
		}
		Expect(44);
		res = res + t.val; 
		return res;
	}

	String  Termo() {
		String  res;
		res = "";  String v1="";  String v2="";
		v1 = Fator();
		res = res + v1; 
		while (la.kind == 41 || la.kind == 42) {
			if (la.kind == 41) {
				Get();
				v2 = Fator();
				res = res + v2; 
			} else {
				Get();
				v2 = Fator();
				res = res + v2; 
			}
		}
		return res;
	}

	String  Fator() {
		String  res;
		res = "";  String v1=""; String v2=""; 
		if (la.kind == 1) {
			Get();
			res = res + toBlackBold(t.val); 
			if (la.kind == 15 || la.kind == 37) {
				v1 = FatorVetor();
				res = res + v1; 
			}
		} else if (la.kind == 2) {
			Get();
			res = res + toBlackBold(t.val); 
		} else SynErr(52);
		return res;
	}

	String  TamanhoVet() {
		String  res;
		res = ""; String v1=""; 
		Expect(45);
		res = res + t.val; 
		Expect(28);
		res = res + t.val; 
		if (la.kind == 2) {
			Get();
		} else if (la.kind == 1 || la.kind == 2 || la.kind == 39) {
			v1 = Expr();
		} else SynErr(53);
		res = res + v1; 
		Expect(29);
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
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,x, x,x,T,x, x,x,x,x, x,T,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x}

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
			case 9: s = "para expected"; break;
			case 10: s = "repita expected"; break;
			case 11: s = "leia expected"; break;
			case 12: s = "escreva expected"; break;
			case 13: s = "retorne expected"; break;
			case 14: s = "constante expected"; break;
			case 15: s = "novo expected"; break;
			case 16: s = "algoritmo expected"; break;
			case 17: s = "se expected"; break;
			case 18: s = "senao expected"; break;
			case 19: s = "entao expected"; break;
			case 20: s = "fimse expected"; break;
			case 21: s = "maior expected"; break;
			case 22: s = "menor expected"; break;
			case 23: s = "maiorIgual expected"; break;
			case 24: s = "menorIgual expected"; break;
			case 25: s = "diferente expected"; break;
			case 26: s = "resto expected"; break;
			case 27: s = "\"procedimento\" expected"; break;
			case 28: s = "\"(\" expected"; break;
			case 29: s = "\")\" expected"; break;
			case 30: s = "\":\" expected"; break;
			case 31: s = "\"inteiro\" expected"; break;
			case 32: s = "\",\" expected"; break;
			case 33: s = "\"[]\" expected"; break;
			case 34: s = "\"fimse;\" expected"; break;
			case 35: s = "\";\" expected"; break;
			case 36: s = "\"=\" expected"; break;
			case 37: s = "\"[\" expected"; break;
			case 38: s = "\"]\" expected"; break;
			case 39: s = "\"-\" expected"; break;
			case 40: s = "\"+\" expected"; break;
			case 41: s = "\"*\" expected"; break;
			case 42: s = "\"/\" expected"; break;
			case 43: s = "\"{\" expected"; break;
			case 44: s = "\"}\" expected"; break;
			case 45: s = "\"tamanho\" expected"; break;
			case 46: s = "??? expected"; break;
			case 47: s = "invalid Instrucao"; break;
			case 48: s = "invalid DeclVar"; break;
			case 49: s = "invalid Condicao"; break;
			case 50: s = "invalid FatorVetor"; break;
			case 51: s = "invalid FatorVetor"; break;
			case 52: s = "invalid Fator"; break;
			case 53: s = "invalid TamanhoVet"; break;
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
