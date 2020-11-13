package org.egov.integration.util;
import java.io.*;

public class AESEncryption {
	public final String AesIVName = "Adfhj#$@56677745";
	

	/*public final String Encrypt(String InputText)
	{


			ByteArrayOutputStream memoryStream = null;
			cryptoStream cryptoStream = null;
			try
			{
				try (RijndaelManaged AES = new RijndaelManaged())
				{
					AES.KeySize = 128;
					AES.BlockSize = 128;
					byte[] PlainText = InputText.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
					PasswordDeriveBytes SecretKey = new PasswordDeriveBytes(AesIVName,AesIVName.length().toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
					try (ICryptoTransform Encryptor = AES.CreateEncryptor(SecretKey.GetBytes(16),SecretKey.GetBytes(16)))
					{
						try (memoryStream = new ByteArrayOutputStream())
						{
							try (cryptoStream = new CryptoStream(memoryStream, Encryptor, CryptoStreamMode.Write))
							{
								cryptoStream.Write(PlainText, 0, PlainText.length);
								cryptoStream.FlushFinalBlock();
								return Convert.ToBase64String(memoryStream.ToArray());
							}
						}
					}
				}

			}
			catch (java.lang.Exception e)
			{
				throw e;
			}
			finally
			{
				if (memoryStream != null)
				{
					memoryStream.close();
				}
				if (cryptoStream != null)
				{
					cryptoStream.Close();
				}
			}
	}*/

}
