package kimiram.nbtdisplay;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NBTDisplay implements ClientModInitializer {
	public static final String MOD_ID = "nbt-display";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyMapping showNbtKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.nbt-display.show_nbt",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "keybindings"))
    ));

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, texts) -> {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), KeyMappingHelper.getBoundKeyOf(showNbtKeybind).getValue())) {
                DataComponentPatch patch = stack.getComponentsPatch();


                DataResult<Tag> result = DataComponentPatch.CODEC.encodeStart(Objects.requireNonNull(context.registries()).createSerializationContext(NbtOps.INSTANCE), patch);
                String raw = result.result().get().toString();
                if (Objects.equals(raw, "{}")) {
                    raw = "";
                }
                String line = "";
                int spaces = 0;

                for (int i = 0; i < raw.length(); i++) {
                    char chr = raw.charAt(i);

                    switch (chr) {
                        case ':':
                            line += ":";
                            if (raw.charAt(i + 1) == '{' || raw.charAt(i + 1) == '[') {
                                line += " ";
                            }
                            break;
                        case ',':
                            line += ",";
                            texts.add(Component.literal(line));
                            line = " ".repeat(spaces);
                            break;
                        case '{':
                            if (raw.charAt(i + 1) == '}') {
                                line += "{}";
                                i++;
                                break;
                            }

                            line += "{";
                            spaces += 2;
                            texts.add(Component.literal(line));
                            line = " ".repeat(spaces);
                            break;
                        case '[':
                            if (raw.charAt(i + 1) == ']') {
                                line += "[]";
                                i++;
                                break;
                            }

                            line += "[";
                            spaces += 2;
                            texts.add(Component.literal(line));
                            line = " ".repeat(spaces);
                            break;
                        case '}':
                            texts.add(Component.literal(line));
                            spaces -= 2;
                            line = " ".repeat(spaces) + "}";
                            break;
                        case ']':
                            texts.add(Component.literal(line));
                            spaces -= 2;
                            line = " ".repeat(spaces) + "]";
                            break;
                        default:
                            line += chr;
                    }
                }
                if (!raw.isEmpty()) {
                    texts.add(Component.literal(line));
                }
            }
		});
	}
}