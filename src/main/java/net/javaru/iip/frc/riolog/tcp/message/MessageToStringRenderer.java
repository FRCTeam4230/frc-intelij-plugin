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

import java.io.StringWriter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.javaru.iip.frc.riolog.tcp.message.Message.StyledAppendable;



public class MessageToStringRenderer
{
    private static final Message.RenderOptions defaultRenderOptions =new Message.RenderOptions();
    
    @Contract("null -> null; !null -> !null")
    public static String renderToString(@Nullable Message message)
    {
        return renderToString(message, defaultRenderOptions);
    }

    @Contract(value = "null,_ -> null; !null,_ -> !null")
    public static String renderToString(@Nullable Message message, @NotNull Message.RenderOptions renderOptions)
    {
        // Based on the handling of a message in the main of the RioLog class
        //     https://github.com/wpilibsuite/riolog/blob/master/src/main/java/netconsole2/RioLog.java
        //     https://github.com/wpilibsuite/EclipsePlugins/blob/master/edu.wpi.first.wpilib.plugins.riolog/src/netconsole2/RioLog.java
        if (message == null)
        {
            return null;
        }
        else
        {
            final StringWriter stringWriter = new StringWriter();
            final StyledAppendable appendable = new PlainAppendable(stringWriter);
            message.render(appendable, renderOptions);
            return stringWriter.toString();
        }
    }           
}
