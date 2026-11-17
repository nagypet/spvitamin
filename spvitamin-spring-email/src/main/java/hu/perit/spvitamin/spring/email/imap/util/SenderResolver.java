package hu.perit.spvitamin.spring.email.imap.util;

import hu.perit.spvitamin.spring.email.imap.model.SenderInfo;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class SenderResolver
{
    private record ForwardConfig(String marker, Pattern fromPattern)
    {
    }


    private static ForwardConfig forwardConfig(String marker, String fromLabel)
    {
        Pattern pattern = Pattern.compile(
                Pattern.quote(fromLabel) + "[^\n]*?([\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,})",
                Pattern.CASE_INSENSITIVE
        );
        return new ForwardConfig(marker, pattern);
    }


    /**
     * Gmail automatikus továbbítás "ujjlenyomata" a Return-Path headerben.
     * Formátum: eredeti_cimzett+caf_=eredeti_cimzett=celldomain@gmail_vagy_workspace_domain.com
     * Példa (@gmail.com):        mebilszamla+caf_=Peter.Nagy=innodox.com@gmail.com
     * Példa (Google Workspace):  szamlazas+caf_=szamla=mebil.io@marischka.hu
     * A domain tetszőleges lehet — Google Workspace fiókok is generálják a +caf_= mintát.
     */
    private static final Pattern GMAIL_AUTO_FORWARD_PATTERN =
            Pattern.compile("^<?([^+@\\s]+)\\+caf_=.+@([\\w.\\-]+)>?$", Pattern.CASE_INSENSITIVE);


    private static final List<ForwardConfig> FORWARD_CONFIGS = List.of(
            // English
            forwardConfig("---------- Forwarded message", "From:"),
            forwardConfig("-----Original Message-----", "From:"),
            forwardConfig("Begin forwarded message:", "From:"),
            forwardConfig("-------- Forwarded Message --------", "From:"),
            forwardConfig("-------- Original Message --------", "From:"),
            // Hungarian
            forwardConfig("Továbbított levél kezdete:", "Feladó:"),
            forwardConfig("-------- Továbbított üzenet --------", "Feladó:"),
            // German
            forwardConfig("---------- Weitergeleitete Nachricht", "Von:"),
            forwardConfig("-----Ursprüngliche Nachricht-----", "Von:"),
            forwardConfig("Beginn der weitergeleiteten E-Mail:", "Von:"),
            forwardConfig("-------- Weitergeleitete Nachricht --------", "Von:")
    );


    /**
     * Meghatározza a levél tényleges feladóját (senderEmail/senderName), illetve
     * - ha alkalmazható - az eredeti feladót (originalSender).
     * <p>
     * Két eset lehetséges:
     * <ul>
     *     <li><b>Gmail automatikus továbbítás (+caf_=)</b>: a Return-Path header "+caf_=" mintát
     *     tartalmaz. Ilyenkor a From header (amit a Gmail NEM módosít) valójában
     *     az eredeti feladót tartalmazza, a tényleges senderEmail pedig a
     *     Gmail fiók címe, ami a levelet ténylegesen továbbküldte.</li>
     *     <li><b>Gmail automatikus továbbítás (X-Forwarded-For)</b>: ha a Return-Path nem
     *     tartalmaz +caf_= mintát (pl. az eredeti küldő saját Return-Path-et használ),
     *     az X-Forwarded-For header alapján azonosítjuk a továbbító Gmail/Workspace fiókot.</li>
     *     <li><b>Kézi (manuális) továbbítás</b>: a From header a továbbító személyt
     *     tartalmazza, az eredeti feladót a levél tartalmából próbáljuk kinyerni
     *     (lásd {@link #extractOriginalSenderFromContent(String)}).</li>
     * </ul>
     */
    public static SenderInfo resolveSenderInfo(Message message, String content) throws MessagingException
    {
        Pair<String, String> fromAddress = getEmailAddress(message.getFrom()[0]);
        String gmailAutoForwardSender = extractGmailAutoForwardSender(message);

        if (gmailAutoForwardSender != null)
        {
            return new SenderInfo(gmailAutoForwardSender, null, fromAddress.getLeft());
        }

        String xForwardedSender = extractXForwardedSender(message);
        if (xForwardedSender != null)
        {
            return new SenderInfo(xForwardedSender, null, fromAddress.getLeft());
        }

        String originalSender = extractOriginalSenderFromContent(content);
        return new SenderInfo(fromAddress.getLeft(), fromAddress.getRight(), originalSender);
    }


    private static String extractGmailAutoForwardSender(Message message) throws MessagingException
    {
        String[] returnPathHeaders = message.getHeader("Return-Path");
        if (returnPathHeaders == null)
        {
            return null;
        }

        for (String returnPath : returnPathHeaders)
        {
            if (StringUtils.isBlank(returnPath))
            {
                continue;
            }

            Matcher matcher = GMAIL_AUTO_FORWARD_PATTERN.matcher(returnPath.trim());
            if (matcher.matches())
            {
                String localPart = matcher.group(1);
                String domain = matcher.group(2);
                return StringUtils.toRootLowerCase(localPart + "@" + domain);
            }
        }

        return null;
    }


    /**
     * Gmail automatikus továbbítás felismerése az X-Forwarded-For header alapján.
     * Azokban az esetekben, amikor a Gmail nem írja át a Return-Path headert (nincs +caf_= minta),
     * az X-Forwarded-For header szóközzel elválasztott email címeket tartalmaz:
     * az első a továbbító Gmail/Workspace fiók, az utolsó a végcél.
     * Példa: "szamlazas@marischka.hu szamla@mebil.io"
     */
    private static String extractXForwardedSender(Message message) throws MessagingException
    {
        String[] xForwardedFor = message.getHeader("X-Forwarded-For");
        if (xForwardedFor == null)
        {
            return null;
        }

        for (String header : xForwardedFor)
        {
            if (StringUtils.isBlank(header))
            {
                continue;
            }

            String[] addresses = header.trim().split("\\s+");
            if (addresses.length >= 2 && addresses[0].contains("@"))
            {
                return StringUtils.toRootLowerCase(addresses[0]);
            }
        }

        return null;
    }


    private static String extractOriginalSenderFromContent(String content)
    {
        if (content == null)
        {
            return null;
        }

        for (ForwardConfig config : FORWARD_CONFIGS)
        {
            int markerIndex = content.indexOf(config.marker());
            if (markerIndex != -1)
            {
                String afterMarker = content.substring(markerIndex);
                Matcher matcher = config.fromPattern().matcher(afterMarker);
                if (matcher.find())
                {
                    return StringUtils.toRootLowerCase(matcher.group(1));
                }
            }
        }

        return null;
    }


    private static Pair<String, String> getEmailAddress(Address address)
    {
        if (address instanceof InternetAddress internetAddress)
        {
            return new ImmutablePair<>(StringUtils.toRootLowerCase(internetAddress.getAddress()), internetAddress.getPersonal());
        }
        else
        {
            return new ImmutablePair<>(StringUtils.toRootLowerCase(address.toString()), null);
        }
    }
}
