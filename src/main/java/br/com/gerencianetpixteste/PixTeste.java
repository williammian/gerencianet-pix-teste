package br.com.gerencianetpixteste;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.gerencianet.gnsdk.Gerencianet;
import br.com.gerencianet.gnsdk.exceptions.GerencianetException;

public class PixTeste {
	
	private static String chavePixRecebedor = "96r321h7-8547-6k20-7425-9rg7jb69n963"; //chave pix criada na Gerencianet
	
	public static void main(String[] args) {
		
		try {
			
			Credentials credentials = new Credentials();
	
			JSONObject options = new JSONObject();
			options.put("client_id", credentials.getClientId());
			options.put("client_secret", credentials.getClientSecret());
			options.put("pix_cert", credentials.getCertificadoPix());
			options.put("sandbox", credentials.isSandbox());
	
			//Gera Cobrança PIX dinamico
			JSONObject body = gerarJSONObject(3600, "94271564656", "Gorbadoc Oldbuck", new BigDecimal("0.01"));
				
			Gerencianet gn = new Gerencianet(options);
			JSONObject response = gn.call("pixCreateImmediateCharge", new HashMap<String,String>(), body);
			System.out.println(response);
			
			//Obtendo id da cobrança
			JSONObject obj = response.getJSONObject("loc");
			Long id = obj.getLong("id");
			System.out.println(id);
			
			//QrCode
			HashMap<String, String> params = new HashMap<String, String>();
		    params.put("id", id.toString());
		    
		    response = gn.call("pixGenerateQRCode", params, new JSONObject());
		    System.out.println(response);
          
		    File outputfile = new File("qrCodeImage.png");
		    ImageIO.write(ImageIO.read(new ByteArrayInputStream(javax.xml.bind.DatatypeConverter.parseBase64Binary(((String) response.get("imagemQrcode")).split(",")[1]))), "png", outputfile);
		    Desktop desktop = Desktop.getDesktop();
		    desktop.open(outputfile);
			
		    System.out.println("ok");

		}catch (GerencianetException e){
			System.out.println(e.getError());
			System.out.println(e.getErrorDescription());
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 	
		
	}
	
	private static JSONObject gerarJSONObject(int expiracao, String cpfDevedor, String nomeDevedor, BigDecimal valor) {
		JSONObject body = new JSONObject();
        body.put("calendario", new JSONObject().put("expiracao", expiracao));
        body.put("devedor", new JSONObject().put("cpf", cpfDevedor).put("nome", nomeDevedor));
        body.put("valor", new JSONObject().put("original", valor.toString()));
        body.put("chave", chavePixRecebedor);
        body.put("solicitacaoPagador", "Serviço realizado.");

        JSONArray infoAdicionais = new JSONArray();
        infoAdicionais.put(new JSONObject().put("nome", "Campo 1").put("valor", "Informação Adicional1 do PSP-Recebedor"));
        infoAdicionais.put(new JSONObject().put("nome", "Campo 2").put("valor", "Informação Adicional2 do PSP-Recebedor"));
        body.put("infoAdicionais", infoAdicionais);
        
        return body;
	}

}
