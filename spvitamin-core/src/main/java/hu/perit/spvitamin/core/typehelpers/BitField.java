package hu.perit.spvitamin.core.typehelpers;

import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;

@Getter
public class BitField
{
    private long bits;


    public static BitField of(Long bits)
    {
        return new BitField(bits);
    }


    public BitField(Long bits)
    {
        this.bits = LongUtils.get(bits);
    }


    public boolean getBit(Long mask)
    {
        if (mask == null)
        {
            return false;
        }
        return (this.bits & mask) != 0;
    }


    public BitField setBit(Long mask)
    {
        if (mask != null)
        {
            this.bits |= mask;
        }
        return this;
    }


    public BitField resetBit(Long mask)
    {
        if (mask != null)
        {
            this.bits &= ~mask;
        }
        return this;
    }


    public BitField setBitTo(Long mask, Boolean value)
    {
        if (mask != null)
        {
            if (BooleanUtils.isTrue(value))
            {
                return this.setBit(mask);
            }
            else
            {
                return this.resetBit(mask);
            }
        }
        return this;
    }
}
