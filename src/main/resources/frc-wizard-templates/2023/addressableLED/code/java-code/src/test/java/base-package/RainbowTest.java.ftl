<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage};

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.AddressableLEDSim;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

class RainbowTest
{
    @Test
    void rainbowPatternTest()
    {
        HAL.initialize(500, 0);
        try (var robot = new Robot())
        {
            robot.robotInit();
            AddressableLEDSim ledSim = AddressableLEDSim.createForChannel(9);
            assertTrue(ledSim.getRunning());
            assertEquals(60, ledSim.getLength());

            var rainbowFirstPixelHue = 0;
            for (int iteration = 0; iteration < 100; iteration++)
            {
                robot.robotPeriodic();
                var data = ledSim.getData();
                for (int i = 0; i < 60; i++)
                {
                    final var hue = (rainbowFirstPixelHue + (i * 180 / 60)) % 180;
                    assertIndexColor(data, i, hue, 255, 128);
                }
                rainbowFirstPixelHue += 3;
                rainbowFirstPixelHue %= 180;
            }
        }
    }


    private void assertIndexColor(byte[] data, int index, int hue, int saturation, int value)
    {
        var color = new Color8Bit(Color.fromHSV(hue, saturation, value));
        int b = data[index * 4];
        int g = data[(index * 4) + 1];
        int r = data[(index * 4) + 2];
        int z = data[(index * 4) + 3];

        assertAll(
                () -> assertEquals(0, z),
                () -> assertEquals(color.red, r & 0xFF),
                () -> assertEquals(color.green, g & 0xFF),
                () -> assertEquals(color.blue, b & 0xFF));
    }
}
