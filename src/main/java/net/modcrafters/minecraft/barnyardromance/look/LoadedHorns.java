package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

import java.util.List;

// A horn file with its parts baked into model parts, ready to draw.
public record LoadedHorns(HornFile file, Identifier texture, List<Baked> male, List<Baked> female) {
    private static final String ROOT = "horn";
    private static final float DEGREES = (float) (Math.PI / 180.0);

    public LoadedHorns {
        male = List.copyOf(male);
        female = List.copyOf(female);
    }

    public static LoadedHorns bake(HornFile file) {
        return new LoadedHorns(
                file,
                Identifier.parse(file.texture()),
                file.male().stream().map(part -> bake(file, part)).toList(),
                file.female().stream().map(part -> bake(file, part)).toList()
        );
    }

    // The top part's own pose is left out of the model, so each animal can grow it from its pivot (see Baked.pose).
    private static Baked bake(HornFile file, HornPart part) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition top = mesh.getRoot().addOrReplaceChild(ROOT, cubes(part), PartPose.ZERO);
        addChildren(top, part);
        ModelPart model = LayerDefinition.create(mesh, file.textureWidth(), file.textureHeight()).bakeRoot().getChild(ROOT);
        return new Baked(part, model);
    }

    private static void addChildren(PartDefinition parent, HornPart part) {
        for (int index = 0; index < part.children().size(); index++) {
            HornPart child = part.children().get(index);
            PartDefinition definition = parent.addOrReplaceChild(
                    "child" + index,
                    cubes(child),
                    PartPose.offsetAndRotation(
                            child.pivot().x(),
                            child.pivot().y(),
                            child.pivot().z(),
                            child.rotation().x() * DEGREES,
                            child.rotation().y() * DEGREES,
                            child.rotation().z() * DEGREES
                    )
            );
            addChildren(definition, child);
        }
    }

    private static CubeListBuilder cubes(HornPart part) {
        CubeListBuilder builder = CubeListBuilder.create();
        for (HornCube cube : part.cubes()) {
            builder.texOffs(cube.u(), cube.v())
                    .mirror(cube.mirror())
                    .addBox(
                            cube.from().x(),
                            cube.from().y(),
                            cube.from().z(),
                            cube.size().x(),
                            cube.size().y(),
                            cube.size().z(),
                            new CubeDeformation(cube.inflate())
                    );
        }
        return builder;
    }

    public record Baked(HornPart part, ModelPart model) {
        // Moves to the part's pivot, turns it like ModelPart.translateAndRotate does, and grows it from there.
        public void pose(PoseStack poseStack, float growth) {
            poseStack.translate(part.pivot().x() / 16.0f, part.pivot().y() / 16.0f, part.pivot().z() / 16.0f);
            Xyz rotation = part.rotation();
            if (rotation.x() != 0.0f || rotation.y() != 0.0f || rotation.z() != 0.0f) {
                Quaternionf turn = new Quaternionf().rotationZYX(rotation.z() * DEGREES, rotation.y() * DEGREES, rotation.x() * DEGREES);
                //? if >=26.1 {
                poseStack.rotate(turn);
                //?} else
                //poseStack.mulPose(turn);
            }
            poseStack.scale(growth, growth, growth);
        }
    }
}
