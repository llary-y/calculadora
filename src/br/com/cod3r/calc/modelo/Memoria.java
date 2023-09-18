package br.com.cod3r.calc.modelo;

import java.util.ArrayList;
import java.util.List;

public class Memoria {

	private enum TipoComando {
		ZERAR, IGUAL, MUDANCA_SINAL, SOMA, MULT,
			DIV, SUB, VIRGULA, NUMERO;
	}
	
	private static final Memoria instancia = new Memoria();
	
	private final List<MemoriaObservador> observadores = 
			new ArrayList<>();
	
	private TipoComando ultimaOperacao = null;
	private boolean substituir = false;
	private String textoAtual = "";
	private String textoBuffer = ""; // texto armazenado
	
	private Memoria() {
		
	}

	public static Memoria getInstancia() {
		return instancia;
	}
	
	public void adicionarObservador(MemoriaObservador observador) {
		observadores.add(observador);
	}

	public String getTextoAtual() {
		return textoAtual.isEmpty() ? "0" : textoAtual;
	}
	
	public void processarComando (String texto) {
		
		TipoComando tipoComando = detectarTipoComando(texto);

		if (tipoComando == null) {
			return;
		} else if (tipoComando == TipoComando.ZERAR) {
			textoAtual = "";
			textoBuffer = "";
			substituir = false;
			ultimaOperacao = null;
			
		} else if (tipoComando == TipoComando.MUDANCA_SINAL && textoAtual.contains("-")) {
			textoAtual = textoAtual.substring(1);
			
		} else if (tipoComando == TipoComando.MUDANCA_SINAL && !textoAtual.contains("-")) {
			textoAtual = "-" + textoAtual;

		} else if (tipoComando == TipoComando.NUMERO 
				|| tipoComando == TipoComando.VIRGULA) {
			textoAtual = substituir ? texto : textoAtual + texto;
			substituir = false;
			
		} else {
			substituir = true;
			textoAtual = obterResultadoOperacao();
			textoBuffer = textoAtual;
			ultimaOperacao = tipoComando;
		}
	
		observadores.forEach(o -> o.valorAlterado(getTextoAtual()));
	}
	
	private String obterResultadoOperacao() {
		
		if (ultimaOperacao == null 
				|| ultimaOperacao == TipoComando.IGUAL) {
			return textoAtual;
		} 
		
		double numeroBuffer = 
				Double.parseDouble(textoBuffer.replace(",", "."));
		double numeroAtual = 
				Double.parseDouble(textoAtual.replace(",", "."));
		
		double resultado = 0;
		
		if (ultimaOperacao == TipoComando.SOMA) {
			resultado = numeroBuffer + numeroAtual;
		} else if (ultimaOperacao == TipoComando.SUB) {
			resultado =  numeroBuffer - numeroAtual;
		} else if (ultimaOperacao == TipoComando.MULT) {
			resultado =  numeroBuffer * numeroAtual;
		} else if (ultimaOperacao == TipoComando.DIV) {
			resultado =  numeroBuffer / numeroAtual;
		} 
		String resultadoTexto = 
				Double.toString(resultado).replace(".", ",");
		boolean inteiro = resultadoTexto.endsWith(",0");
		return inteiro ? resultadoTexto.replace(",0" , "") 
					: resultadoTexto;
	}

	private TipoComando detectarTipoComando (String texto) {
		
		if (textoAtual.isEmpty() && texto == "0") {
			return null;
		}
		
		try {
			Integer.parseInt(texto);
			return TipoComando.NUMERO;
		} catch (NumberFormatException e) {
			// Quando não for numero...
			if ("AC".equals(texto)) {
				return TipoComando.ZERAR;
			} else if ("÷".equals(texto)) {
				return TipoComando.DIV;
			} else if ("x".equals(texto)) {
				return TipoComando.MULT;
			} else if ("-".equals(texto)) {
				return TipoComando.SUB;
			} else if ("+".equals(texto)) {
				return TipoComando.SOMA;
			} else if ("=".equals(texto)) {
				return TipoComando.IGUAL;
			} else if ("±".equals(texto)) {
				return TipoComando.MUDANCA_SINAL;
			} else if (",".equals(texto) && 
					!textoAtual.contains(",")) {
				return TipoComando.VIRGULA;
			}
		}
		return null;
	}
}
