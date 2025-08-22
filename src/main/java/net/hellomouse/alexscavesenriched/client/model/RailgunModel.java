package net.hellomouse.alexscavesenriched.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.world.entity.Entity;

public class RailgunModel extends AdvancedEntityModel<Entity> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox part2, part3, part4, part5, part6;
    private final AdvancedModelBox group2, group3;

    private void setPos(AdvancedModelBox box, float x, float y, float z) {
        box.defaultOffsetX = box.offsetX = x;
        box.defaultOffsetY = box.offsetY = y;
        box.defaultOffsetZ = box.offsetZ = z;
    }

    public RailgunModel() {
        this.texWidth = 128;
        this.texHeight = 128;

        this.root = new AdvancedModelBox(this);
        this.part2 = new AdvancedModelBox(this);
        this.part3 = new AdvancedModelBox(this);
        this.part4 = new AdvancedModelBox(this);
        this.part5 = new AdvancedModelBox(this);
        this.part6 = new AdvancedModelBox(this);
        this.group2 = new AdvancedModelBox(this);
        this.group3 = new AdvancedModelBox(this);

        this.root.addChild(this.part2);
        this.root.addChild(this.part3);
        this.root.addChild(this.group2);

        this.root.setTextureOffset(0, 0).addBox(-3.0F, -2.0F, 0.0F, 3.0F, 2.0F, 22.0F, false)
                .setTextureOffset(0, 24).addBox(-3.0F, 2.0F, 0.0F, 3.0F, 2.0F, 22.0F, false)
                .setTextureOffset(50, 0).addBox(-3.0F, -2.0F, 22.0F, 3.0F, 5.0F, 8.0F, false)
                .setTextureOffset(0, 48).addBox(-2.0F, 0.0F, 1.0F, 1.0F, 2.0F, 21.0F, false)
                .setTextureOffset(50, 13).addBox(-3.0F, -2.0F, 30.0F, 3.0F, 3.0F, 5.0F, false)
                .setTextureOffset(44, 48).addBox(-3.0F, -2.0F, 35.0F, 3.0F, 4.0F, 9.0F, false)
                .setTextureOffset(50, 31).addBox(-0.5F, -1.0F, 23.0F, 1.0F, 2.0F, 5.0F, false);
        this.root.setPos(-7.0F, 20.0F, -16.0F);

        this.part2.setTextureOffset(50, 38).addBox(-3.0F, -6.5F, 0.0F, 3.0F, 6.0F, 2.0F, false);
        this.part2.setPos(0F, 4F, 41F);
        this.part2.rotateAngleX = -0.3927F;

        this.part3.setTextureOffset(50, 21).addBox(-1.5F, -7.0F, 0.0F, 2.0F, 7.0F, 3.0F, false);
        this.part3.setPos(-1.0F, 7.0F, 32.0F);
        this.part3.rotateAngleX = 0.3927F;

        this.group2.setPos(-3.0F, 0.0F, 28.0F);
        this.part4
                .setTextureOffset(60, 42).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, false)
                .setTextureOffset(60, 38).addBox(-2.0F, -2.0F, 2.5F, 2.0F, 2.0F, 2.0F, false)
                .setTextureOffset(60, 25).addBox(-2.0F, -2.0F, -2.5F, 2.0F, 2.0F, 2.0F, false)
                .setTextureOffset(60, 21).addBox(-2.0F, -2.0F, 5.0F, 2.0F, 2.0F, 2.0F, false);
        this.part4.setPos(0F, 0F, -5F);
        this.part4.rotateAngleZ = 0.3927F;
        this.group2.addChild(this.part4);

        this.group2.addChild(this.group3);
        this.group3
                .setTextureOffset(50, 46).addBox(-1.0F, -1.0F, 18.0F, 1.0F, 1.0F, 1.0F, false)
                .setTextureOffset(54, 46).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, false);
        this.group3.setPos(2F, -3F, -28F);

        this.part5.setTextureOffset(50, 61).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, false);
        this.part5.setPos(1F, 2F, 19F);
        this.part5.rotateAngleZ = 0.3927F;

        this.part6.setTextureOffset(44, 61).addBox(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, false);
        this.part6.setPos(-2.0F, 2.0F, 19.0F);
        this.part6.rotateAngleZ = -0.3927F;

        this.group3.addChild(this.part5);
        this.group3.addChild(this.part6);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(this.root, this.group2, this.group3);
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(this.root);
    }

    @Override
    public void setupAnim(Entity entity, float useAmount, float ageInTicks, float unused, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
    }
}
