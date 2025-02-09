
/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.crypto;


import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.crypto.CryptoUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;


public class PwdToolApplication
{
    private static final int AES_KEY_SIZE = 256;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception // NOSONAR
    {
        Options options = new Options(args);
        if (!options.parse())
        {
            return;
        }

        if (!options.validate())
        {
            return;
        }

        try
        {
            String result = null;
            CryptoUtil crypto = new CryptoUtil();
            if (options.getCommand() == Options.Commands.ENCRYPT)
            {
                result = crypto.encrypt(options.getKey(), options.getText());
            }
            else if (options.getCommand() == Options.Commands.DECRYPT)
            {
                result = crypto.decrypt(options.getKey(), options.getText());
            }
            else if (options.getCommand() == Options.Commands.KEYGEN)
            {
                SecretKey key = generateKey();
                result = Base64.getEncoder().encodeToString(key.getEncoded());
            }

            System.out.format("RESULT: '%s'\n", result);
        }
        catch (Exception e)
        {
            System.out.println(StackTracer.toString(e));
        }
    }

    private static SecretKey generateKey() throws Exception
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }
}
