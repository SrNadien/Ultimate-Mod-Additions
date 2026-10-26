package nadiendev.ultimatemodadditions.ban;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class Legacy {

    private Legacy() {
    }

    public static MutableComponent parse(String text) {
        MutableComponent result = Component.empty();
        StringBuilder current = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < text.length()) {
                ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(text.charAt(i + 1)));
                if (format != null) {
                    if (current.length() > 0) {
                        result.append(Component.literal(current.toString()).withStyle(style));
                        current.setLength(0);
                    }
                    style = format == ChatFormatting.RESET ? Style.EMPTY : style.applyFormat(format);
                    i++;
                    continue;
                }
            }
            current.append(c);
        }
        if (current.length() > 0) {
            result.append(Component.literal(current.toString()).withStyle(style));
        }
        return result;
    }
}
