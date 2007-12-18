package lcm.lc;

import java.io.*;

/** A message which can be easily sent using LC **/
public interface LCEncodable
{
    public void encode(DataOutputStream outs) throws IOException;
}
