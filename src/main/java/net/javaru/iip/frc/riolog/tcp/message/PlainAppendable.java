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

import java.io.IOException;

import com.intellij.openapi.diagnostic.Logger;



public class PlainAppendable implements Message.StyledAppendable
{
    private static final Logger LOG = Logger.getInstance(PlainAppendable.class);


    private Appendable out;

    
    public PlainAppendable(Appendable out)
    {
        this.out = out;
    }


    @Override
    public Message.StyledAppendable append(char c)
    {
        try
        {
            out.append(c);
        }
        catch (IOException e)
        {
            LOG.debug("[FRC] IOException during append operation: " + e,toString());
        }
        return this;
    }


    @Override
    public Message.StyledAppendable append(CharSequence csq)
    {
        try
        {
            out.append(csq);
        }
        catch (IOException e)
        {
            LOG.debug("[FRC] IOException during append operation: " + e, toString());
        }
        return this;
    }


    @Override
    public Message.StyledAppendable append(CharSequence csq, int start, int end)
    {
        try
        {
            out.append(csq, start, end);
        }
        catch (IOException e)
        {
            LOG.debug("[FRC] IOException during append operation: " + e, toString());
        }
        return this;
    }


    @Override
    public Message.StyledAppendable startStyle(int style)
    {
        return this;
    }


    @Override
    public Message.StyledAppendable endStyle()
    {
        return this;
    }

}
