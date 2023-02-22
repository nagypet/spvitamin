
/*
 * Copyright 2020-2023 the original author or authors.
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

import hu.perit.spvitamin.core.InitParams;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
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
        this.args = args; // NOSONAR
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
