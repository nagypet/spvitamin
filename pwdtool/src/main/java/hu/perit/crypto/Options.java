
/*
 * Copyright header
 * The ultimate spring based webservice template project.
 * Author Peter Nagy <nagy.peter.home@gmail.com>
 */

package hu.perit.crypto;

import hu.perit.spvitamin.core.InitParams;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Getter
@Log4j
public class Options
{
    public enum Commands
    {
        NONE,
        ENCRYPT,
        DECRYPT
    }

    private String[] args;
    private Commands command = Commands.NONE;
    private String key = "secret";
    private String text = null;

    public Options(String[] args)
    {
        InitParams ini = InitParams.getInstance();
        this.key = ini.getParam("crypto.secret", "secret");
        this.args = args;
    }

    public boolean parse()
    {
        if (args == null
                || args.length == 0
                || args[0].equals("/?")
                || args[0].equals("-?")
                || args[0].equals("/h")
                || args[0].equals("-h")
                || args[0].equals("--help")
        )
        {
            this.printUsage();
            return false;
        }
        else
        {
            int countOptions = args.length - 1;
            for (int i = 0; i < countOptions; i++)
            {
                String arg = args[i];
                //log.debug(arg);
                switch (arg)
                {
                    case "--encrypt":
                    {
                        this.command = Commands.ENCRYPT;
                        break;
                    }

                    case "--decrypt":
                    {
                        this.command = Commands.DECRYPT;
                        break;
                    }

                    case "--key":
                    {
                        if (i + 1 >= countOptions)
                        {
                            System.out.println("Invalid command line parameters! No key is specified!");
                            return false;
                        }
                        else
                        {
                            this.key = args[++i];
                        }
                        break;
                    }

                    default:
                    {
                        System.out.format("Invalid option '%s'!", arg);
                        return false;
                    }
                }
            }
            this.text = args[countOptions];
        }
        return true;
    }


    public boolean validate()
    {
        if (this.command == Commands.NONE)
        {
            System.out.println("No command specified!");
            return false;
        }

        if (this.text == null)
        {
            System.out.println("No text specified!");
            return false;
        }

        System.out.format("%s '%s' with secret key '%s'%n", this.command, this.text, this.key);
        return true;
    }


    private void printUsage()
    {
        System.out.printf("Usage: pwdtool <options> text%n"
                + "\toptions:%n"
                + "\t\t--encrypt%n"
                + "\t\t--decrypt%n"
                + "\t\t--key encryption_key: if no key is specified the default is '%s'%n"
                + "%n\te.g. pwdtool --encrypt --key changeit alma%n", this.key
        );
    }
}
