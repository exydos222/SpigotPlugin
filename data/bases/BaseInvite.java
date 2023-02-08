package data.bases;

import java.util.UUID;

import objects.bases.Base;

public class BaseInvite {

    public Base base;
    public UUID player;
    
    public BaseInvite(final Base base, final UUID player) {
        this.base = base;
        this.player = player;
    }
    
}
