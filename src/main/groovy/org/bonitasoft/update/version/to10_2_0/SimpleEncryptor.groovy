/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.update.version.to10_2_0

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec

/**
 * Warning: this class is a copy of the Engine version
 *
 * @author Emmanuel Duchastenier
 */
class SimpleEncryptor {

    private static final byte[] SALT = "GoodJob!".bytes
    private static final char[] PASSPHRASE = "H4v3FunW17h80n174".toCharArray()

    private static final SecretKey SECRET_KEY = generateKey()

    private SimpleEncryptor() {
    }

    static SecretKey generateKey() {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            KeySpec spec = new PBEKeySpec(PASSPHRASE, SALT, 1000, 256)
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES")
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e)
        }
    }

    static String encrypt(byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY)
        byte[] encryptedBytes = cipher.doFinal(data)
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    static byte[] decrypt(String encryptedData) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY)
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData)
        return cipher.doFinal(decodedBytes)
    }
}
