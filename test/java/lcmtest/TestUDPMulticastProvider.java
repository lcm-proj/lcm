import org.junit.Test;

import lcm.lcm.LCM;

public class TestUDPMulticastProvider {
    @Test
    public void testClose() throws Exception {
        LCM lcm = new LCM();
        lcm.subscribe("", null);
        lcm.close();
    }
}
