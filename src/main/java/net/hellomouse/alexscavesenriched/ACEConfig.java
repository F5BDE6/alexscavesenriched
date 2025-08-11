package net.hellomouse.alexscavesenriched;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = AlexsCavesEnriched.MODID)
public class ACEConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    private static final long REASONABLE_MAX_SMALL = 1024; // Integer.MAX_VALUE makes the GUI slider unusable for any int config
    @ConfigEntry.Gui.Excluded
    private static final int REASONABLE_DURATION_MAX = 24 * 60 * 60 * 20;

    @ConfigEntry.Gui.CollapsibleObject
    public ClientConfig client = new ClientConfig();

    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    public double enrichedRodFuelMultiplier = 12.0;
    @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
    public int raygunCooldownTicks = 15;
    public boolean irradiationMutatesMobs = false;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int miniNukeRadius = 2;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 30)
    public float nuclearFurnaceLeakRadius = 5.0F;

    @ConfigEntry.Gui.CollapsibleObject
    public RailgunConfig railgun = new RailgunConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public DemonCoreConfig demonCore = new DemonCoreConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public RocketConfig rocket = new RocketConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public RocketLauncherConfig rocketLauncher = new RocketLauncherConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public NuclearExplosionConfig nuclear = new NuclearExplosionConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public NeutronExplosionConfig neutron = new NeutronExplosionConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public BlackHoleConfig blackHole = new BlackHoleConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public UraniumArrowConfig uraniumArrow = new UraniumArrowConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public GammaFlashlightConfig gammaFlashlightConfig = new GammaFlashlightConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public CentrifugeConfig centrifuge = new CentrifugeConfig();

    public static class ClientConfig {
        public boolean overrideSkyColor = true;
        public boolean nukeParticleEffects = true;

        @ConfigEntry.Gui.CollapsibleObject
        public DemonCoreConfig.Sprite demonCoreSprite = new DemonCoreConfig.Sprite();
    }

    public static class RailgunConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4096)
        public int damage = 60;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int chargeRate = 7;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 512)
        public int range = 256;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 512)
        public int blockBreakRange = 64;

        // Can these enchants work?
        public boolean infinity = true;
        public boolean multishot = true;
        public boolean quickCharge = true;
    }

    public static class RocketConfig {
        public boolean reliableWithPortals = true;
        @ConfigEntry.Gui.CollapsibleObject
        public NonNuclearRocketConfig nonNuclear = new NonNuclearRocketConfig();

        @ConfigEntry.Gui.CollapsibleObject
        public NuclearRocketConfig nuclear = new NuclearRocketConfig();

        public static class NonNuclearRocketConfig {
            @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
            public double dispenserPower = 4;
            @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
            public double dispenserUncertainty = 6;
            @ConfigEntry.Gui.CollapsibleObject
            public NormalRocketConfig normal = new NormalRocketConfig();

            @ConfigEntry.Gui.CollapsibleObject
            public UraniumRocketConfig uranium = new UraniumRocketConfig();

            public static class NormalRocketConfig {
                @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
                public double explosionPower = 4;
            }

            public static class UraniumRocketConfig {
                @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
                public int irradiationTime = 100;
                @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
                public double irradiationRadius = 5;
                @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
                public double explosionPower = 5;
                @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
                public double irradiationPotionTime = 300;
            }
        }

        public static class NuclearRocketConfig {
            @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
            public double dispenserPower = 7;
        }
    }


    public static class RocketLauncherConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 256)
        public double baseSpeed = 4;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
        public int cooldown = 4 * 20;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 64)
        public double powerSpeed = 1;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 256)
        public double backblastRange = 15;
        @ConfigEntry.BoundedDiscrete(min = 10, max = 90)
        public double backblastAngle = 30;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
        public double backblastDirectDamage = 20;
    }

    public static class NuclearExplosionConfig {
        public boolean letNukeKillWither = true;
        public boolean useNewNuke = true;
        public boolean smeltBlock = true;
        public boolean irradiateAir = true;

        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public int irradiationTime = 36000;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public int irradiationPotionTime = 3000;
    }

    public static class NeutronExplosionConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public int irradiationPotionTime = 48000;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 9)
        public int irradiationPotionPower = 9;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 64)
        public int radius = 8;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public int burstDamage = 200;
    }

    public static class BlackHoleConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 64)
        public int radius = 8;
    }

    public static class UraniumArrowConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public int irradiationTime = 40;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
        public double baseDamage = 4;
    }

    public static class GammaFlashlightConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public double range = 20D;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_DURATION_MAX)
        public double spread = 0.6;
    }

    public static class DemonCoreConfig {
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL)
        public int diameter = 7;
        @ConfigEntry.BoundedDiscrete(min = 0, max = REASONABLE_MAX_SMALL * 3)
        public int damage = 10;
        @ConfigEntry.BoundedDiscrete(min = 1, max = REASONABLE_MAX_SMALL)
        public int maxSize = 128;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double boundingBoxFillProportion = 0.7;

        public static class Sprite {
            @ConfigEntry.BoundedDiscrete(min = 32, max = 1440)
            @ConfigEntry.Gui.Tooltip
            public int resolution = 720;
            @ConfigEntry.BoundedDiscrete(min = 1, max = 22)
            @ConfigEntry.Gui.Tooltip
            public int animationFrames = 5;

            public int getSpriteWidth() {
                return resolution;
            }
            public int getSpriteHeight() {
                return resolution * animationFrames;
            }

            public boolean equals(Sprite other) {
                return other.resolution == resolution && other.animationFrames == animationFrames;
            }

            public Sprite copy() {
                Sprite out = new Sprite();
                out.resolution = resolution;
                out.animationFrames = animationFrames;
                return out;
            }
        }
    }

    public static class CentrifugeConfig {
        public boolean cantInteractWithActive = true;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5000)
        public int maxSpeed = 100;
    }
}