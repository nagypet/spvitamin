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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class TeeServletOutputStream extends ServletOutputStream
{

    private final TeeOutputStream targetStream;

    public TeeServletOutputStream(OutputStream one, OutputStream two)
    {
        this.targetStream = new TeeOutputStream(one, two);
    }

    public void write(int arg0) throws IOException
    {
        this.targetStream.write(arg0);
    }

    public void flush() throws IOException
    {
        super.flush();
        this.targetStream.flush();
    }

    public void close() throws IOException
    {
        super.close();
        this.targetStream.close();
    }

    public boolean isReady()
    {
        return false;
    }

    public void setWriteListener(WriteListener writeListener)
    {
    }
}
