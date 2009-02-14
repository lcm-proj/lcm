package lcm.lcm;

import java.io.*;

/** A message which can be easily sent using LCM.
 **/
public interface LCMEncodable
{
    /**
     * Invoked by LCM.
     * @param outs Any data to be sent should be written to this output stream.
     */
    public void encode(DataOutputStream outs) throws IOException;
}
