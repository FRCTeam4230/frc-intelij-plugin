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
    "Based on" (well mostly directly ported over from) the PrintMessage class
    https://github.com/wpilibsuite/riolog/blob/master/src/main/java/netconsole2/PrintMessage.java
    https://github.com/wpilibsuite/EclipsePlugins/blob/master/edu.wpi.first.wpilib.plugins.riolog/src/netconsole2/PrintMessage.java
*/
public class InfoMessage extends AbstractMessage 
{
    
    public final String line;


    public InfoMessage(ByteBuffer data)
    {
        // data.rewind is called in the AbstractMessage constructor
        super(data);
        line = StandardCharsets.UTF_8.decode(data).toString();
    }


    public Type getType()
    {
        return Type.kPrint;
    }


    public void render(StyledAppendable output, RenderOptions options)
    {
        renderTimestamp(output, options);

        // line
        output.startStyle(kStylePrint);
        output.append(line);
        output.endStyle();
    }
}