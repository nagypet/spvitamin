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

package hu.perit.spvitamin.spring.httplogging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;

final class BufferedServletInputStream extends ServletInputStream
{

    private final ByteArrayInputStream bais;

    public BufferedServletInputStream(ByteArrayInputStream bais)
    {
        this.bais = bais;
    }

    public int available()
    {
        return this.bais.available();
    }

    public int read()
    {
        return this.bais.read();
    }

    public int read(byte[] buf, int off, int len)
    {
        return this.bais.read(buf, off, len);
    }

    public boolean isFinished()
    {
        return false;
    }

    public boolean isReady()
    {
        return true;
    }

    public void setReadListener(ReadListener readListener)
    {
    }
}
