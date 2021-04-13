package hu.perit.spvitamin.core.typehelpers;

import java.math.BigInteger;

/**
 * LongUtils
 * @author Peter Nagy (xgxtpna)
 */
public class LongUtils
{
    /**
     * @param l1
     * @param l2
     * @return boolean
     */
    public static boolean equals(final Long l1, final Long l2)
    {
        if (l1 == l2) // NOSONAR
        {
            return true;
        }

        if (l1 == null || l2 == null)
        {
            return false;
        }

        return l1.equals(l2);
    }


    /**
     * @param bi
     * @param l
     * @return boolean
     */
    public static boolean equals(final BigInteger bi, final Long l)
    {
        if (bi == null || l == null)
        {
            return false;
        }

        return equals(bi.longValue(), l);
    }


    /**
     * @param l
     * @param bi
     * @return boolean
     */
    public static boolean equals(final Long l, final BigInteger bi)
    {
        if (bi == null || l == null)
        {
            return false;
        }

        return equals(bi.longValue(), l);
    }


    /**
     * @param l1
     * @param l2
     * @param nullIsLess whether consider {@code null} value less than non-{@code null} value
     * @return &lt; 0, 0, &gt; 0, if {@code l1} is respectively less, equal ou greater than {@code l2}
     */
    public static int compare(final Long l1, final Long l2, final boolean nullIsLess)
    {
        if (l1 == l2)
        {
            return 0;
        }
        if (l1 == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (l2 == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return l1.compareTo(l2);
    }


    /**
     * @param l1
     * @param l2
     * @return &lt; 0, 0, &gt; 0, if {@code l1} is respectively less, equal ou greater than {@code l2}
     */
    public static int compare(final Long l1, final Long l2)
    {
        return compare(l1, l2, true);
    }


    /**
     * @param l
     * @param bi
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code l} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Long l, final BigInteger bi, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? 1 : -1;
        }

        return compare(l, bi.longValue(), nullIsLess);
    }


    /**
     * @param l
     * @param bi
     * @return &lt; 0, 0, &gt; 0, if {@code l} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Long l, final BigInteger bi)
    {
        return compare(l, bi, true);
    }


    /**
     * @param bi
     * @param l
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code l}
     */
    public static int compare(final BigInteger bi, final Long l, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? -1 : 1;
        }

        return compare(bi.longValue(), l, nullIsLess);
    }


    /**
     * @param bi
     * @param l
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code l}
     */
    public static int compare(final BigInteger bi, final Long l)
    {
        return compare(bi, l, true);
    }


    /**
     * @param bi1
     * @param bi2
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code bi1} is respectively less, equal ou greater than {@code bi2}
     */
    public static int compare(final BigInteger bi1, final BigInteger bi2, final boolean nullIsLess)
    {
        if (bi1 == bi2)
        {
            return 0;
        }
        if (bi1 == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (bi2 == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return bi1.compareTo(bi2);
    }


    /**
     * @param bi1
     * @param bi2
     * @return &lt; 0, 0, &gt; 0, if {@code bi1} is respectively less, equal ou greater than {@code bi2}
     */
    public static int compare(final BigInteger bi1, final BigInteger bi2)
    {
        return compare(bi1, bi2, true);
    }


    private LongUtils()
    {
        // private constructor for the utility class
    }
}
