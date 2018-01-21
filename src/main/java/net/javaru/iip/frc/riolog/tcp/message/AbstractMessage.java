/*
 * Copyright 2015-2018 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.riolog.tcp.message;

import java.nio.ByteBuffer;
import java.util.Formatter;
import java.util.Locale;



public abstract class AbstractMessage implements Message
{
    public final float timestamp;
    public final int seqNumber;


    protected AbstractMessage(ByteBuffer data)
    {
        data.rewind();
        timestamp = data.getFloat();
        seqNumber = data.getShort() & 0xffff;
    }


    protected void renderTimestamp(StyledAppendable output, RenderOptions options)
    {
        if (options.showTimestamp)
        {
            output.startStyle(kStyleTimestamp);
            Formatter formatter = new Formatter(output, Locale.US);
            formatter.format(options.timestampFormat, timestamp);
            output.endStyle();
            output.append('\t');
            output = new IndentAppendable(output, "\t\t");
        }
    }
}
