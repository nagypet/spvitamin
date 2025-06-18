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

package hu.perit.spvitamin.core.html;

import java.util.ArrayList;
import java.util.List;

public class HtmlTagContent
{

    private final String simpleContent;
    private List<HtmlTag> tags = null;


    public HtmlTagContent()
    {
        this.simpleContent = null;
    }


    public HtmlTagContent(String simpleContent)
    {
        this.simpleContent = simpleContent;
    }


    public void append(HtmlTag tag)
    {
        if (this.isSimpleContent())
        {
            throw new IllegalArgumentException(String.format("HtmlTag already initialized with simple content: '%s'!", this.simpleContent));
        }

        this.initContent();
        this.tags.add(tag);
    }


    public void print(StringBuilder sb, int level)
    {
        if (this.isSimpleContent())
        {
            sb.append(this.simpleContent);
        }
        else
        {
            if (this.tags == null)
            {
                return;
            }

            for (HtmlTag tag : this.tags)
            {
                sb.append(System.lineSeparator());
                tag.print(sb, level + 1);
            }
        }
    }


    public boolean isSimpleContent()
    {
        return simpleContent != null;
    }


    private void initContent()
    {
        if (this.tags == null)
        {
            this.tags = new ArrayList<>();
        }
    }
}
