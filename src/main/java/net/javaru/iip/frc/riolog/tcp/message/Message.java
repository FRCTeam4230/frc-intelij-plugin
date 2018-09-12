/*
 * Copyright 2015-2018 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.riolog.tcp.message;

/* 
    "Based on" (well mostly directly ported over from) the Message interface
    https://github.com/wpilibsuite/riolog/blob/master/src/main/java/netconsole2/Message.java
    https://github.com/wpilibsuite/EclipsePlugins/blob/master/edu.wpi.first.wpilib.plugins.riolog/src/netconsole2/Message.java
*/
public interface Message
{
    enum Type
    {
        kDummy, kError, kWarning, kPrint
    }

    static class RenderOptions
    {
        public boolean showTimestamp = false;
        public String timestampFormat = "%.3f";
        public boolean showErrorCode = true;
        public boolean showErrorLocation = true;
        public boolean showErrorCallStack = true;
        public boolean showWarningLocation = true;
        public boolean showWarningCallStack = false;
    }

    Type getType();

    int kStyleNone = 0;
    int kStyleTimestamp = 1;
    int kStylePrint = 2;
    int kStyleError = 3;
    int kStyleWarning = 4;
    int kStyleErrorCode = 5;
    int kStyleDetails = 6;
    int kStyleLocation = 7;
    int kStyleCallStack = 8;

    interface StyledAppendable extends Appendable
    {
        @Override
        StyledAppendable append(char c);

        @Override
        StyledAppendable append(CharSequence csq);

        @Override
        StyledAppendable append(CharSequence csq, int start, int end);

        // starts new style; overrides previous call to startStyle
        StyledAppendable startStyle(int style);

        // not necessarily called for every call to startStyle
        StyledAppendable endStyle();
    }

    void render(StyledAppendable output, RenderOptions options);

    static class IndentAppendable implements StyledAppendable
    {
        private StyledAppendable out;
        private String indent;


        public IndentAppendable(StyledAppendable out, String indent)
        {
            this.out = out;
            this.indent = indent;
        }


        @Override
        public StyledAppendable append(char c)
        {
            out.append(c);
            if (c == '\n')
            {
                out.append(indent);
            }
            return this;
        }


        @Override
        public StyledAppendable append(CharSequence csq)
        {
            out.append(csq.toString().replace("\n", "\n" + indent));
            return this;
        }


        @Override
        public StyledAppendable append(CharSequence csq, int start, int end)
        {
            out.append(csq.subSequence(start, end).toString().replace("\n", "\n" + indent));
            return this;
        }


        // starts new style; overrides previous call to startStyle
        public StyledAppendable startStyle(int style)
        {
            out.startStyle(style);
            return this;
        }


        // not necessarily called for every call to startStyle
        public StyledAppendable endStyle()
        {
            out.endStyle();
            return this;
        }
    }
}