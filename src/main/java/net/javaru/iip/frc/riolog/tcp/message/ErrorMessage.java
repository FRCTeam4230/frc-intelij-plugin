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
import java.nio.charset.StandardCharsets;



/* 
    "Based on" (well mostly directly ported over from) the ErrorMessage class
    https://github.com/wpilibsuite/riolog/blob/master/src/main/java/netconsole2/ErrorMessage.java
    https://github.com/wpilibsuite/EclipsePlugins/blob/master/edu.wpi.first.wpilib.plugins.riolog/src/netconsole2/ErrorMessage.java
*/
public class ErrorMessage extends AbstractMessage
{
    public final int numOccur;
    public final int errorCode;
    public final byte flags;
    public final String details;
    public final String location;
    public final String callStack;


    public ErrorMessage(ByteBuffer data)
    {
        // data.rewind is called in the AbstractMessage constructor
        super(data);
        numOccur = data.getShort() & 0xffff;
        errorCode = data.getInt();
        flags = data.get();
        details = getSizedString(data);
        location = getSizedString(data);
        callStack = getSizedString(data);
    }


    private String getSizedString(ByteBuffer data)
    {
        int size = data.getShort() & 0xffff;
        ByteBuffer buf = data.slice();
        buf.limit(size);
        data.position(data.position() + size);
        return StandardCharsets.UTF_8.decode(buf).toString();
    }


    public Type getType()
    {
        if ((flags & 1) != 0)
        {
            return Type.kError;
        }
        else
        {
            return Type.kWarning;
        }
    }


    public boolean isError()
    {
        return getType() == Type.kError;
    }


    public boolean isWarning()
    {
        return getType() == Type.kWarning;
    }


    public void render(StyledAppendable output, RenderOptions options)
    {
        renderTimestamp(output, options);

        // error/warning label
        if (isError())
        {
            output.startStyle(kStyleError);
            output.append("ERROR");
        }
        else
        {
            output.startStyle(kStyleWarning);
            output.append("WARNING");
        }
        output.endStyle();

        // error code
        if (options.showErrorCode)
        {
            output.append("  ");
            output.startStyle(kStyleErrorCode);
            output.append(String.valueOf(errorCode));
            output.endStyle();
        }

        // error details
        output.append("  ");
        output.startStyle(kStyleDetails);
        output.append(details);
        output.endStyle();

        // error location
        if ((isError() && options.showErrorLocation) || (isWarning() && options.showWarningLocation))
        {
            output.append("  ");
            output.startStyle(kStyleLocation);
            output.append(location);
            output.endStyle();
        }

        // call stack
        if ((isError() && options.showErrorCallStack) || (isWarning() && options.showWarningCallStack))
        {
            output.append('\n');
            output.startStyle(kStyleCallStack);
            output.append(callStack);
            output.endStyle();
        }
    }
}
