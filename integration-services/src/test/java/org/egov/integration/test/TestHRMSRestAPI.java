package org.egov.integration.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.egov.integration.util.AES128Bit;
import org.springframework.util.Base64Utils;

public class TestHRMSRestAPI {
	
//	public static void main(String[] args) {
//		String secretKey = "Adfhj#$@56677745";
//		AES128Bit aes = new AES128Bit();
//		
//		String encryptedEmpCode = aes.doEncryptedAES("1975010001Z", secretKey);
//		System.out.println("encryptedEmpCode :: " + encryptedEmpCode);
//		String encryptedMonth = aes.doEncryptedAES("03", secretKey);
//		System.out.println("encryptedMonth :: " + encryptedMonth);
//		String encryptedYear = aes.doEncryptedAES("2018", secretKey);
//		System.out.println("encryptedYear :: " + encryptedYear);
//		
//		String dycriptedEmpCode = aes.doDecryptedAES(encryptedEmpCode, secretKey);
//		System.out.println("dycriptedEmpCode :: " + dycriptedEmpCode);
//		String dycriptedMonth = aes.doDecryptedAES(encryptedMonth, secretKey);
//		System.out.println("dycriptedMonth :: " + dycriptedMonth);
//		String dycriptedYear = aes.doDecryptedAES(encryptedYear, secretKey);
//		System.out.println("dycriptedYear :: " + dycriptedYear);
//		
//		//String dycriptedEmpCode = aes.doDecryptedAES("BNDrqEza8Iu7cGURFHEXF4EOKtqHtuB+xXnVSMa0HTw=", secretKey);
//		//System.out.println("dycriptedEmpCode :: " + dycriptedEmpCode);
//	}
	
	//	public String Encrypt(String InputText) {
	//		MemoryStream memoryStream = null;
	//        CryptoStream cryptoStream = null;
	//        
	//        try
	//        {
	//            using (RijndaelManaged AES = new RijndaelManaged())
	//            {
	//                AES.KeySize = 128;
	//                AES.BlockSize = 128;
	//                byte[] PlainText = System.Text.Encoding.Unicode.GetBytes(InputText);
	//                PasswordDeriveBytes SecretKey = new PasswordDeriveBytes(AesIVName,Encoding.ASCII.GetBytes(AesIVName.Length.ToString()));
	//                using (ICryptoTransform Encryptor = AES.CreateEncryptor(SecretKey.GetBytes(16),SecretKey.GetBytes(16)))
	//                {
	//                    using (memoryStream = new MemoryStream())
	//                    {
	//                        using (cryptoStream = new CryptoStream(memoryStream, Encryptor, CryptoStreamMode.Write))
	//                        {
	//                            cryptoStream.Write(PlainText, 0, PlainText.Length);
	//                            cryptoStream.FlushFinalBlock();
	//                            return Convert.ToBase64String(memoryStream.ToArray());
	//                        }
	//                    }
	//                }
	//            }
	//
	//        }
	//        catch
	//        {
	//            throw;
	//        }
	//        finally
	//        {
	//            if (memoryStream != null)
	//                memoryStream.Close();
	//            if (cryptoStream != null)
	//                cryptoStream.Close();
	//        }
	//
	//	}
	
	public static void main(String[] args) {
		
		 try {
	        DefaultHttpClient Client = new DefaultHttpClient();

	        //HttpGet httpGet = new HttpGet("http://ehrms.nic.in/API/Values/StatesList");	        
	        HttpGet httpGet = new HttpGet("http://ehrms.nic.in/API/Values/GetEmployeeBacklogLeaveDetails/CH?EmpCode=A7865345TG");
	        //HttpGet httpGet = new HttpGet("http://ehrms.nic.in/API/Values/GetEmployeeNotificationDetails/CH?EmpCode=A7865345TG");	        
	        
	        String authStr = "sourav.bhowmik@pwc.com" + ":" + "A386A63D28D6468DAFC4990E474B1851D56B26CD3C8A47CC9E731FAE7C576197";
	        String authEncoded = Base64Utils.encodeToString(authStr.getBytes());
	        
	        System.out.println("authEncoded = " + authEncoded);
	        httpGet.setHeader("Authorization", "Basic " + authEncoded);

	        HttpResponse response = Client.execute(httpGet);

	        System.out.println("response = " + response);

	        BufferedReader breader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        StringBuilder responseString = new StringBuilder();
	        String line = "";
	        while ((line = breader.readLine()) != null) {
	            responseString.append(line);
	        }
	        breader.close();
	        String repsonseStr = responseString.toString();

	        System.out.println("repsonseStr = " + repsonseStr);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
