/*
 * Copyright 2015 Mark Vedder
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

package net.javaru.iip.frc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;



/**
 * A class that can be used to send periodic and continuous simulated log data to an InputStream for use in testing.
 */
public class TestLogDataGenerator implements AutoCloseable
{

    private final PipedInputStream in;
    private final PipedOutputStream out;
    private final PrintWriter writer;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public TestLogDataGenerator() throws IllegalStateException
    {
        this(3, TimeUnit.SECONDS);
    }


    public TestLogDataGenerator(long frequency,  TimeUnit timeUnit) throws IllegalStateException
    {
        try
        {
            in = new PipedInputStream();
            out = new PipedOutputStream(in);
            writer = new PrintWriter(new OutputStreamWriter(out));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not create necessary io streams.", e);
        }


        scheduler.scheduleAtFixedRate(new Generator(), 0, frequency, timeUnit);
    }



    public InputStream getInputStream() { return in; }
    public OutputStream getOutputStream() { return out; }


    @Override
    public void close() throws Exception
    {
        try
        {
            scheduler.shutdown();
            scheduler.awaitTermination(3, TimeUnit.SECONDS);
        } catch (Exception ignore) {}
        try { in.close(); } catch (Exception ignore) {}
        try { writer.close(); } catch (Exception ignore) {}

    }


    private class Generator implements Runnable
    {
        private boolean isFirstRun = true;
        private Iterator<String> iterator = lines.iterator();

        @Override
        public void run()
        {
            if (isFirstRun)
            {
                firstRun();
            }

            if (!iterator.hasNext())
            {
                iterator = lines.iterator();
                firstRun();
            }

            String line = iterator.next();
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Foo       - " + line);
            writer.flush();
        }

        private void firstRun()
        {
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Foo       - Example log messages of application Initialization");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Bar       - initializing Bar");
            writer.println(new Date() + " + |WARN  [main     ]  com.example.Bar       - Bar names file not explicitly set. Using default names file of C:\\data\\names.txt");
            writer.flush();
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Bar       - Bar names file set to C:\\data\\names.txt");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Bar       - Opening bar names file C:\\data\\names.txt");
            writer.flush();
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Bar       - Reading in bar names file C:\\data\\names.txt");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Bar       - 628 names read in from the names file for Bar");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Bar       - Bar initialization completed.");
            writer.flush();
            writer.println(new Date() + " + |INFO  [main     ]  com.example.BazSystem - Initializing BazSystem");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Zork      - Initializing Zork");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Zork      - Zork Initialization completed");
            writer.flush();
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Flux      - Initialization the Flux Capacitor");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor is charging up ");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor is charging up.. ");
            writer.flush();
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor is charging up.... ");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor is charging up....... ");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor is charging up.......... ");
            writer.flush();
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor has charged");
            writer.println(new Date() + " + |DEBUG [main     ]  com.example.Flux      - Flux Capacitor completing Initialization");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Flux      - Flux Capacitor Initialization Completed. Ready to hit 88 MPH!!");
            writer.println(new Date() + " + |INFO  [main     ]  com.example.Foo       - Application Initialization Has completed");
            writer.flush();
            isFirstRun = false;
        }
    }


    private static ImmutableList<String> lines = ImmutableList.copyOf(
        new String[] {
            "Life is wonderful. Without it we'd all be dead.",
            "Daddy, why doesn't this magnet pick up this floppy disk?",
            "Give me ambiguity or give me something else.",
            "I.R.S.: We've got what it takes to take what you've got!",
            "We are born naked, wet and hungry. Then things get worse.",
            "Make it idiot proof and someone will make a better idiot.",
            "He who laughs last thinks slowest!",
            "Always remember you're unique, just like everyone else.",
            "A flashlight is a case for holding dead batteries.",
            "Lottery: A tax on people who are bad at math.",
            "Error, no keyboard - press F1 to continue.",
            "There's too much blood in my caffeine system.",
            "Artificial Intelligence usually beats real stupidity.",
            "Hard work has a future payoff. Laziness pays off now.",
            "\"Very funny, Scotty. Now beam down my clothes.\"",
            "Puritanism: The haunting fear that someone, somewhere may be happy.",
            "Consciousness: that annoying time between naps.",
            "Don't take life too seriously, you won't get out alive.",
            "I don't suffer from insanity. I enjoy every minute of it.",
            "Better to understand a little than to misunderstand a lot.",
            "The gene pool could use a little chlorine.",
            "When there's a will, I want to be in it.",
            "Okay, who put a \"stop payment\" on my reality check?",
            "We have enough youth, how about a fountain of SMART?",
            "Programming is an art form that fights back.",
            "\"Daddy, what does FORMATTING DRIVE C mean?\"",
            "All wiyht. Rho sritched mg kegtops awound?",
            "My mail reader can beat up your mail reader.",
            "Never forget: 2 + 2 = 5 for extremely large values of 2.",
            "Nobody has ever, ever, EVER learned all of WordPerfect.",
            "To define recursion, we must first define recursion.",
            "Good programming is 99% sweat and 1% coffee.",
            "Home is where you hang your @",
            "The E-mail of the species is more deadly than the mail.",
            "A journey of a thousand sites begins with a single click.",
            "You can't teach a new mouse old clicks.",
            "Great groups from little icons grow.",
            "Speak softly and carry a cellular phone.",
            "C:\\ is the root of all directories.",
            "Don't put all your hypes in one home page.",
            "Pentium wise; pen and paper foolish.",
            "The modem is the message.",
            "Too many clicks spoil the browse.",
            "The geek shall inherit the earth.",
            "A chat has nine lives.",
            "Don't byte off more than you can view.",
            "Fax is stranger than fiction.",
            "What boots up must come down.",
            "Windows will never cease.   (ed. oh sure...)",
            "In Gates we trust.    (ed.  yeah right....)",
            "Virtual reality is its own reward.",
            "Modulation in all things.",
            "A user and his leisure time are soon parted.",
            "There's no place like http://www.home.com",
            "Know what to expect before you connect.",
            "Oh, what a tangled website we weave when first we practice.",
            "Speed thrills.",
            "Give a man a fish and you feed him for a day; teach him to use the Net and he won't bother you for weeks."
        }
    );

}
