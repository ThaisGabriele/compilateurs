

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
	public static final int maxT = 20;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	String ident(String s, int margin) {
    return "<div style='margin-left:" + margin +"px'>" + s + "</div>";
}

String beginDocument (String s) {
    return "<html><body>" + s + "</body></html>";
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
		while (la.kind == 11) {
			Bloco();
		}
	}

	void Bloco() {
		String res; String saidaFormatada  = "";
		res = DeclProc();
		saidaFormatada = saidaFormatada + res;  
		while (la.kind == 1 || la.kind == 6) {
			res = Instrucao();
			saidaFormatada = saidaFormatada + res; 
		}
		System.out.println(saidaFormatada); appendFile(beginDocument(saidaFormatada)); 
	}

	String  DeclProc() {
		String  res;
		res = ""; String v1=""; String v2 = "";String t1 = ""; 
		Expect(11);
		res = toBlueBold(t.val); 
		Expect(1);
		res = res + " " + toBlackBold(t.val); 
		Expect(12);
		res = res + t.val; 
		v1  = Parametros();
		res = res + v1; 
		Expect(13);
		res = res + t.val; 
		if (la.kind == 14) {
			Get();
			res = res +  t.val; 
			t1  = Tipo();
			res = res + t1; 
		}
		Expect(4);
		res = res + toBlackBold(ident(t.val,20)); 
		while (la.kind == 1 || la.kind == 6) {
			v2 = Instrucao();
			res = res +  ident(v2,40) ; 
		}
		Expect(5);
		res = res + toRedBold(t.val); 
		return res;
	}

	String  Instrucao() {
		String  res;
		res = "";  String v1=""; String v2 = ""; 
		if (la.kind == 1 || la.kind == 6) {
			v1 = DeclVar();
			res = res + v1; 
		} else if (la.kind == 1) {
			v2 = ChamaProc();
			res = res + v2; 
		} else SynErr(21);
		return res;
	}

	String  Parametros() {
		String  res;
		res = ""; 
		while (la.kind == 1) {
			Get();
			res = res + t.val; 
			Expect(14);
			res = res + " " + t.val; 
			Expect(15);
			res = res + toRedBold(t.val); 
			if (la.kind == 16 || la.kind == 17) {
				if (la.kind == 16) {
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
		Expect(15);
		res = res + " " + toBlackBold(t.val); 
		return res;
	}

	String  DeclVar() {
		String  res;
		res = ""; String v1=""; String v2 = ""; 
		if (la.kind == 1) {
			v1 = ComAtribuicao();
			res = res + v1; 
			Expect(18);
			res = res + t.val;
		} else if (la.kind == 6) {
			v2 = SemAtribuicao();
			res = res + v2;
			Expect(18);
			res = res + t.val;
		} else SynErr(22);
		return res;
	}

	String  ChamaProc() {
		String  res;
		res = ""; String v1=""; String v2 = ""; 
		v1 = Designator();
		res = res + v1;
		v2 = ParamsDeclarados();
		res = res + v2;
		Expect(18);
		res = res + t.val;
		return res;
	}

	String  ComAtribuicao() {
		String  res;
		res = ""; 
		Expect(1);
		res = res + toBlackBold(t.val);
		Expect(19);
		return res;
	}

	String  SemAtribuicao() {
		String  res;
		res = ""; 
		Expect(6);
		res = res + toBlackBold(t.val); 
		return res;
	}

	String  Designator() {
		String  res;
		res = "";  
		Expect(1);
		res = res + toBlackBold(t.val);
		return res;
	}

	String  ParamsDeclarados() {
		String  res;
		res = "";
		Expect(12);
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
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x}

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
			case 11: s = "\"procedimento\" expected"; break;
			case 12: s = "\"(\" expected"; break;
			case 13: s = "\")\" expected"; break;
			case 14: s = "\":\" expected"; break;
			case 15: s = "\"inteiro\" expected"; break;
			case 16: s = "\",\" expected"; break;
			case 17: s = "\"[]\" expected"; break;
			case 18: s = "\";\" expected"; break;
			case 19: s = "\"=\" expected"; break;
			case 20: s = "??? expected"; break;
			case 21: s = "invalid Instrucao"; break;
			case 22: s = "invalid DeclVar"; break;
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
