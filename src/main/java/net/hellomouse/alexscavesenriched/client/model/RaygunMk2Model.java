package net.hellomouse.alexscavesenriched.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaygunMk2Model extends AdvancedEntityModel<Entity> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox partA;
    private final AdvancedModelBox partB;
    private final AdvancedModelBox partC;
    private final AdvancedModelBox partD;
    private final AdvancedModelBox partE;
    private final AdvancedModelBox partF;
    private final AdvancedModelBox partG;
    private final AdvancedModelBox partH;
    private final AdvancedModelBox partI;
    private final AdvancedModelBox partJ;
    private final AdvancedModelBox partK;
    private final AdvancedModelBox partL;
    private final AdvancedModelBox partM;

    public RaygunMk2Model() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.root = new AdvancedModelBox(this);
        this.root.setRotationPoint(5F / 16, 0F, 5F / 16);

        this.partA = new AdvancedModelBox(this);
        this.root.addChild(this.partA);
        this.partB = new AdvancedModelBox(this);
        this.root.addChild(this.partB);
        this.partC = new AdvancedModelBox(this);
        this.root.addChild(this.partC);
        this.partD = new AdvancedModelBox(this);
        this.root.addChild(this.partD);
        this.partE = new AdvancedModelBox(this);
        this.root.addChild(this.partE);
        this.partF = new AdvancedModelBox(this);
        this.root.addChild(this.partF);
        this.partG = new AdvancedModelBox(this);
        this.root.addChild(this.partG);
        this.partH = new AdvancedModelBox(this);
        this.root.addChild(this.partH);
        this.partI = new AdvancedModelBox(this);
        this.root.addChild(this.partI);
        this.partJ = new AdvancedModelBox(this);
        this.root.addChild(this.partJ);
        this.partK = new AdvancedModelBox(this);
        this.root.addChild(this.partK);
        this.partL = new AdvancedModelBox(this);
        this.root.addChild(this.partL);
        this.partM = new AdvancedModelBox(this);
        this.root.addChild(this.partM);

        this.partA.setTextureOffset(0, 28).addBox(1.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, false);
        this.partB.setTextureOffset(34, 38).addBox(2.5F, -4.5F, -24F, 3F, 3F, 3F, false);
        this.partC.setTextureOffset(0, 0).addBox(2F, -5F, -16F, 4F, 4F, 13F, false);
        this.partD.setTextureOffset(24, 28).addBox(2.5F, -5F, 3F, 3F, 4F, 6F, false);
        this.partE.setTextureOffset(34, 0).addBox(3.0F, -4.0F, -21.0F, 2.0F, 2.0F, 5.0F, false);
        this.partF.setTextureOffset(28, 17).addBox(4.0F, -6.0F, -13.0F, 0.0F, 1.0F, 10.0F, false);
        this.partG.setTextureOffset(20, 40).addBox(4.0F, -8.0F, -16.0F, 0.0F, 3.0F, 1.0F, false);
        this.partH.setTextureOffset(34, 7).addBox(4.0F, -9.0F, -4.0F, 0.0F, 3.0F, 6.0F, false);
        this.partI.setTextureOffset(0, 17).addBox(1.5F, -2.0F, -14.0F, 5.0F, 2.0F, 9.0F, false);
        this.partJ.setTextureOffset(0, 40).addBox(1.5F, -5.5F, -18.0F, 5.0F, 5.0F, 0.0F, false);
        this.partK.setTextureOffset(42, 28).addBox(1.5F, -5.5F, -17.0F, 5.0F, 5.0F, 0.0F, false);
        this.partL.setTextureOffset(42, 33).addBox(1.5F, -5.5F, -19.0F, 5.0F, 5.0F, 0.0F, false);
        this.partM.setTextureOffset(24, 38).addBox(6.0F, -1.0F, -3.0F, 2.0F, 6.0F, 3.0F, false);
        this.partM.rotateAngleX = 0.3491F;
        this.partM.setRotationPoint(-3.0F, 0.0F, 5.0F);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(this.root, partA, partB, partC, partD, partE,
                partF, partG, partH, partI, partJ, partK, partL, partM);
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(this.root);
    }

    @Override
    public void setAngles(Entity entity, float useAmount, float ageInTicks, float unused, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
    }
}
