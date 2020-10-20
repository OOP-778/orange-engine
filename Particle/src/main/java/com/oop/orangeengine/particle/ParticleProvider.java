package com.oop.orangeengine.particle;

import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.particle.versions.OParticle_V12;
import com.oop.orangeengine.particle.versions.OParticle_V13;
import com.oop.orangeengine.particle.versions.OParticle_V8;

public class ParticleProvider {
    private static OParticle provider;
    static {
        if (OVersion.isAfter(12))
            provider = new OParticle_V13();
        else if (OVersion.isAfter(8))
            provider = new OParticle_V12();
        else
            provider = new OParticle_V8();
    }

    public static OParticle getProvider() {
        return provider;
    }
}
