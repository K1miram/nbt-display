package kimiram.nbtdisplay;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.impl.client.screen.ScreenEventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
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
            "key.category.nbt-display"
    ));

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, texts) -> {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(showNbtKeybind).getCode())) {
                String raw = stack.toNbt(Objects.requireNonNull(context.getRegistryLookup())).toString();
                String line = "";
                int spaces = 0;

                for (int i = 0; i < raw.length(); i++) {
                    char chr = raw.charAt(i);

                    switch (chr) {
                        case ':':
                            line += ":";
                            if (!Character.isLetter(raw.charAt(i + 1))) {
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
                texts.add(Text.literal(line));
            }
		});
	}
}