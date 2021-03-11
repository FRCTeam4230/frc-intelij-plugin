/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem

import net.javaru.iip.frc.i18n.FrcBundle


val classicLazyInitializationHelp =
    """
<html>
<strong>${FrcBundle.message("frc.templates.options.singletonSubsystems.lazyOption.contextHelp.primaryLine")}</strong><br>
${FrcBundle.message("frc.templates.options.singletonSubsystems.lazyOption.contextHelp.secondaryLine")}
<pre style="font-size:small;">
class Subsystem
{
    private static Subsystem instance;
 
    private Subsystem() {}
 
    public static Subsystem getInstance()
    {
        if (instance==null) {
            instance = new Subsystem(); }
        return instance;
    }
}
<pre>
</html>"""

val eagerInitializationHelp =
    """
<html>
<strong>${FrcBundle.message("frc.templates.options.singletonSubsystems.eagerOption.contextHelp.primaryLine")}</strong><br>
${FrcBundle.message("frc.templates.options.singletonSubsystems.eagerOption.contextHelp.secondaryLine")}
<pre style="font-size:small;">
class Subsystem
{
    private static Subsystem instance = new Subsystem();
 
    private Subsystem() {}
 
    public static Subsystem getInstance()
    {
        return instance;
    }
}<pre></html>"""


val doubleCheckedLockingInitializationHelp =
    """
<html>
<strong>${FrcBundle.message("frc.templates.options.singletonSubsystems.doubleCheckedLockOption.contextHelp.primaryLine")}</strong><br>
${FrcBundle.message("frc.templates.options.singletonSubsystems.doubleCheckedLockOption.contextHelp.secondaryLine")}
<pre style="font-size:small;">
class Subsystem
{
    private static volatile Subsystem instance = null;
    private Subsystem() {}
    public static Subsystem getInstance()
    {
        if (instance == null)
        {
            // Make thread safe
            synchronized (Subsystem.class)
            {
                // check again as multiple threads
                // can reach above step
                if (instance==null) {
                    instance = new Subsystem(); }
            }
        }
        return instance;
    }
}<pre></html>"""
