
/*
 * Copyright header
 * The ultimate spring based webservice template project.
 * Author Peter Nagy <nagy.peter.home@gmail.com>
 */

package hu.perit.crypto;


import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.log4j.Log4j;


@Log4j
public class Application
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
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