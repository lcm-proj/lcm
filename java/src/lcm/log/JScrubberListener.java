package lcm.logging;

public interface JScrubberListener
{
    public void scrubberMovedByUser(JScrubber js, double x);

    public void scrubberPassedRepeat(JScrubber js, double from_pos, double to_pos);

    public void scrubberExportRegion(JScrubber js, double p0, double p1);
}
