package net.serble.mcdnd.schemas;

public class HitResult {
    private boolean successfulHit;
    private boolean critical;

    public HitResult(boolean successfulHit, boolean critical) {
        this.successfulHit = successfulHit;
        this.critical = critical;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isSuccessfulHit() {
        return successfulHit;
    }
}
