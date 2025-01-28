
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
import hu.perit.spvitamin.core.crypto.CryptoException;
import hu.perit.spvitamin.core.crypto.CryptoUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Application
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) // NOSONAR
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

            System.out.format("RESULT: '%s'\n", result);
        }
        catch (CryptoException e)
        {
            log.debug(StackTracer.toString(e));
        }
    }
}
