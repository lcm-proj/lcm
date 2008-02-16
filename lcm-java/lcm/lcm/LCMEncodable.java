package lcm.lcm;

import java.io.*;

/** A message which can be easily sent using LC **/
public interface LCMEncodable
{
    public void encode(DataOutputStream outs) throws IOException;
}
