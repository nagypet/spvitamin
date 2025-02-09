
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

import hu.perit.spvitamin.core.InitParams;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

@Getter
@Slf4j
public class Options
{
    public enum Commands
    {
        NONE,
        ENCRYPT,
        DECRYPT,
        KEYGEN
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
            if (args[0].equalsIgnoreCase("--keygen"))
            {
                this.command = Commands.KEYGEN;
                return true;
            }

            int countOptions = args.length - 1;
            for (int i = 0; i < countOptions; i++)
            {
                String arg = args[i];
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

        if (EnumSet.of(Commands.ENCRYPT, Commands.DECRYPT).contains(this.command))
        {
            if (StringUtils.isBlank(this.text))
            {
                System.out.println("No text specified!");
                return false;
            }
            System.out.format("%s '%s' with secret key '%s'%n", this.command, this.text, this.key);
            return true;
        }

        if (EnumSet.of(Commands.KEYGEN).contains(this.command))
        {
            System.out.format("%s%n", this.command);
            return true;
        }

        System.out.println("Unknown command!");
        return false;
    }


    private void printUsage()
    {
        System.out.printf("Usage: pwdtool <options> text%n"
                + "\toptions:%n"
                + "\t\t--encrypt%n"
                + "\t\t--decrypt%n"
                + "\t\t--keygen%n"
                + "\t\t--key encryption_key: if no key is specified the default is '%s'%n"
                + "%n\te.g. pwdtool --encrypt --key changeit alma%n", this.key
        );
    }
}
