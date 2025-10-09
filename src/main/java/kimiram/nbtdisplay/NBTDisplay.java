package kimiram.nbtdisplay;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentChanges;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NBTDisplay implements ClientModInitializer {
	public static final String MOD_ID = "nbt-display";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyBinding showNbtKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nbt-display.show_nbt",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            new KeyBinding.Category(Identifier.of(MOD_ID, "keybindings"))
    ));

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, texts) -> {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), KeyBindingHelper.getBoundKeyOf(showNbtKeybind).getCode())) {
                ComponentChanges changes = stack.getComponentChanges();
                DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(Objects.requireNonNull(context.getRegistryLookup()).getOps(NbtOps.INSTANCE), changes);
                String raw = result.result().orElseThrow().toString();
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
                            texts.add(Text.literal(line));
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
                            texts.add(Text.literal(line));
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
                            texts.add(Text.literal(line));
                            line = " ".repeat(spaces);
                            break;
                        case '}':
                            texts.add(Text.literal(line));
                            spaces -= 2;
                            line = " ".repeat(spaces) + "}";
                            break;
                        case ']':
                            texts.add(Text.literal(line));
                            spaces -= 2;
                            line = " ".repeat(spaces) + "]";
                            break;
                        default:
                            line += chr;
                    }
                }
                if (!raw.isEmpty()) {
                    texts.add(Text.literal(line));
                }
            }
		});
	}
}