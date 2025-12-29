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
